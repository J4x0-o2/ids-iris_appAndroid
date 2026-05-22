package com.j4x.iris_ids.ui.screens.facelogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.prefs.DeviceIdManager
import com.j4x.iris_ids.domain.model.EventType
import com.j4x.iris_ids.domain.model.LivenessChallenge
import com.j4x.iris_ids.domain.model.Session
import com.j4x.iris_ids.domain.repository.SessionRepository
import com.j4x.iris_ids.domain.usecase.RegisterEventUseCase
import com.j4x.iris_ids.domain.usecase.VerifyFaceUseCase
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

data class FaceLoginUiState(
    val phase: SilhouettePhase = SilhouettePhase.Framing,
    val challengeInstruction: String = "",
    val matchedName: String? = null,
    val matchedCode: String? = null,
    val navigateToHome: Boolean = false,
    /** true cuando liveness completo → Screen captura JPEG y llama onImageCaptured */
    val shouldCapture: Boolean = false,
)

@HiltViewModel
class FaceLoginViewModel @Inject constructor(
    private val verifyFaceUseCase: VerifyFaceUseCase,
    private val sessionRepository: SessionRepository,
    private val deviceIdManager: DeviceIdManager,
    private val registerEventUseCase: RegisterEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaceLoginUiState())
    val uiState: StateFlow<FaceLoginUiState> = _uiState.asStateFlow()

    private var stateMachine = LivenessStateMachine(LivenessChallenge.randomSequence())

    // ── ShutterButton tap (solo en Framing) ──────────────────────────────────

    fun startVerification() {
        if (_uiState.value.phase != SilhouettePhase.Framing) return
        stateMachine = LivenessStateMachine(LivenessChallenge.randomSequence())
        _uiState.update {
            it.copy(
                phase                = SilhouettePhase.Scanning,
                challengeInstruction = stateMachine.currentChallenge?.instruction ?: "",
            )
        }
    }

    // ── Callback desde FaceAnalyzer (hilo de análisis, thread-safe) ──────────

    fun onFaceData(data: FaceData?) {
        if (_uiState.value.phase != SilhouettePhase.Scanning) return
        if (data == null) return

        stateMachine.process(data)

        if (stateMachine.isComplete) {
            _uiState.update { it.copy(phase = SilhouettePhase.Matched, shouldCapture = true) }
            return
        }

        val instruction = stateMachine.currentChallenge?.instruction ?: ""
        if (_uiState.value.challengeInstruction != instruction) {
            _uiState.update { it.copy(challengeInstruction = instruction) }
        }
    }

    // ── Callback desde Screen tras capturar el JPEG ───────────────────────────

    fun onImageCaptured(base64: String) {
        _uiState.update { it.copy(shouldCapture = false) }
        viewModelScope.launch {
            val deviceId = deviceIdManager.getOrCreate()
            verifyFaceUseCase(base64, deviceId)
                .onSuccess { inspector ->
                    sessionRepository.saveSession(
                        Session(
                            inspectorId   = inspector.id,
                            inspectorName = inspector.name,
                            documentId    = inspector.documentId,
                            role          = inspector.role,
                        )
                    )
                    registerEventUseCase(inspector.id, EventType.IN, deviceId)
                    _uiState.update {
                        it.copy(
                            matchedName    = inspector.name,
                            matchedCode    = inspector.documentId,
                            navigateToHome = true,
                        )
                    }
                }
                .onFailure {
                    // Rostro no reconocido o error de red — reiniciar para nuevo intento
                    stateMachine = LivenessStateMachine(LivenessChallenge.randomSequence())
                    _uiState.update {
                        it.copy(phase = SilhouettePhase.Framing, challengeInstruction = "")
                    }
                }
        }
    }

    fun onNavigatedToHome() {
        _uiState.update { FaceLoginUiState() }
        stateMachine = LivenessStateMachine(LivenessChallenge.randomSequence())
    }
}
