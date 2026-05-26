package com.j4x.iris_ids.ui.screens.enroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.domain.model.LivenessChallenge
import com.j4x.iris_ids.domain.usecase.EnrollInspectorUseCase
import com.j4x.iris_ids.ui.camera.FaceData
import com.j4x.iris_ids.ui.camera.LivenessStateMachine
import com.j4x.iris_ids.ui.components.SilhouettePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EnrollStep { Instructions, Capture, Form, Success }

data class EnrollUiState(
    val step: EnrollStep = EnrollStep.Instructions,
    val capturePhase: SilhouettePhase = SilhouettePhase.Framing,
    val challengeInstruction: String = "",
    val shouldCapture: Boolean = false,
    /** Acumula base64 de cada foto capturada: frontal, HEAD_LEFT, HEAD_RIGHT */
    val capturedImages: List<String> = emptyList(),
    val name: String = "",
    val code: String = "",
    val role: String = "",
    val nameError: String? = null,
    val codeError: String? = null,
    val isSubmitting: Boolean = false,
    val enrollError: String? = null,
)

@HiltViewModel
class EnrollViewModel @Inject constructor(
    private val enrollInspectorUseCase: EnrollInspectorUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollUiState())
    val uiState: StateFlow<EnrollUiState> = _uiState.asStateFlow()

    private var stateMachine = LivenessStateMachine(emptyList())
    // true mientras se espera la foto frontal; false durante los desafíos de pose
    private var isFrontCapture = true

    fun goToCapture() = _uiState.update { it.copy(step = EnrollStep.Capture) }

    // ── ShutterButton tap: captura la foto frontal de inmediato ──────────────

    fun startCapture() {
        if (_uiState.value.capturePhase != SilhouettePhase.Framing) return
        isFrontCapture = true
        _uiState.update {
            it.copy(
                capturePhase         = SilhouettePhase.Scanning,
                challengeInstruction = "",
                shouldCapture        = true,
            )
        }
    }

    // ── Callback desde FaceAnalyzer (hilo de análisis, thread-safe) ──────────

    fun onFaceData(data: FaceData?) {
        if (_uiState.value.capturePhase != SilhouettePhase.Scanning) return
        if (isFrontCapture) return  // aún esperando la foto frontal
        if (data == null) return

        val challengeCompleted = stateMachine.process(data)

        if (challengeCompleted) {
            if (stateMachine.isComplete) {
                // Última pose (HEAD_RIGHT): marcar como Matched + capturar foto final
                _uiState.update { it.copy(capturePhase = SilhouettePhase.Matched, shouldCapture = true) }
            } else {
                // Pose intermedia (HEAD_LEFT): capturar foto en esta pose
                _uiState.update { it.copy(shouldCapture = true) }
            }
            return
        }

        val instruction = stateMachine.currentChallenge?.instruction ?: ""
        if (_uiState.value.challengeInstruction != instruction) {
            _uiState.update { it.copy(challengeInstruction = instruction) }
        }
    }

    // ── Callback desde Screen tras capturar cada JPEG ────────────────────────

    fun onImageCaptured(base64: String) {
        if (isFrontCapture) {
            // Foto frontal recibida → iniciar secuencia de poses
            isFrontCapture = false
            stateMachine = LivenessStateMachine(
                listOf(LivenessChallenge.HEAD_LEFT, LivenessChallenge.HEAD_RIGHT)
            )
            _uiState.update {
                it.copy(
                    shouldCapture        = false,
                    capturedImages       = listOf(base64),
                    challengeInstruction = stateMachine.currentChallenge?.instruction ?: "",
                )
            }
        } else {
            // Foto de pose (HEAD_LEFT o HEAD_RIGHT): acumular
            val images = _uiState.value.capturedImages + base64
            val allDone = _uiState.value.capturePhase == SilhouettePhase.Matched
            _uiState.update {
                it.copy(
                    shouldCapture  = false,
                    capturedImages = images,
                    step           = if (allDone) EnrollStep.Form else it.step,
                )
            }
        }
    }

    // ── Formulario ────────────────────────────────────────────────────────────

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null, enrollError = null) }
    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value, codeError = null, enrollError = null) }
    fun onRoleChange(value: String) = _uiState.update { it.copy(role = value) }

    fun submitForm() {
        val s = _uiState.value
        val nameErr = if (s.name.isBlank()) "Requerido" else null
        val codeErr = if (s.code.isBlank()) "Requerido" else null
        if (nameErr != null || codeErr != null) {
            _uiState.update { it.copy(nameError = nameErr, codeError = codeErr) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, enrollError = null) }
            enrollInspectorUseCase(
                name         = s.name,
                documentId   = s.code,
                role         = s.role,
                imagesBase64 = s.capturedImages,
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, step = EnrollStep.Success) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isSubmitting = false, enrollError = "Error al registrar. Intente nuevamente.")
                    }
                }
        }
    }

    fun reset() = _uiState.update { EnrollUiState() }

    fun goBack(): Boolean = when (_uiState.value.step) {
        EnrollStep.Instructions -> false
        EnrollStep.Capture -> {
            isFrontCapture = true
            _uiState.update {
                it.copy(
                    step                 = EnrollStep.Instructions,
                    capturePhase         = SilhouettePhase.Framing,
                    challengeInstruction = "",
                    shouldCapture        = false,
                    capturedImages       = emptyList(),
                )
            }
            true
        }
        EnrollStep.Form -> {
            isFrontCapture = true
            _uiState.update {
                it.copy(
                    step                 = EnrollStep.Capture,
                    capturePhase         = SilhouettePhase.Framing,
                    challengeInstruction = "",
                    shouldCapture        = false,
                    capturedImages       = emptyList(),
                )
            }
            true
        }
        EnrollStep.Success -> false
    }
}
