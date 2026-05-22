package com.j4x.iris_ids.ui.screens.capture

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EventAccent { Entrada, Salida }

data class CaptureUiState(
    val eventType: String = "",
    val eventLabel: String = "",
    val accent: EventAccent = EventAccent.Entrada,
    val requiresLiveness: Boolean = true,
    val phase: SilhouettePhase = SilhouettePhase.Framing,
    val challengeInstruction: String = "",
    val matchedName: String? = null,
    val matchedCode: String? = null,
    val navigateToSuccess: Boolean = false,
    /** true cuando liveness completo → Screen captura JPEG y llama onImageCaptured */
    val shouldCapture: Boolean = false,
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verifyFaceUseCase: VerifyFaceUseCase,
    private val registerEventUseCase: RegisterEventUseCase,
    private val sessionRepository: SessionRepository,
    private val deviceIdManager: DeviceIdManager,
) : ViewModel() {

    private val eventType: String = savedStateHandle["eventType"] ?: "IN"

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private var stateMachine = LivenessStateMachine(LivenessChallenge.randomSequence())

    init {
        val label = when (eventType) {
            "IN"        -> "Registrar entrada"
            "BREAK_OUT" -> "Registrar descanso"
            "BREAK_IN"  -> "Registrar regreso"
            "FOOD_OUT"  -> "Registrar almuerzo"
            "FOOD_IN"   -> "Registrar regreso"
            "OUT"       -> "Registrar salida"
            else        -> "Registrar evento"
        }
        _uiState.update {
            it.copy(
                eventType        = eventType,
                eventLabel       = label,
                accent           = when (eventType) {
                    "IN", "BREAK_IN", "FOOD_IN" -> EventAccent.Entrada
                    else -> EventAccent.Salida
                },
                requiresLiveness = eventType != "IN",
            )
        }
    }

    // ── ShutterButton tap (solo en Framing) ──────────────────────────────────

    fun startCapture() {
        if (_uiState.value.phase != SilhouettePhase.Framing) return

        if (eventType == "IN") {
            // Solo identificación facial, sin pruebas de liveness
            _uiState.update { it.copy(phase = SilhouettePhase.Scanning, shouldCapture = true) }
            return
        }

        // BREAK / FOOD / OUT: liveness obligatorio
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
        if (eventType == "IN") return  // IN: solo espera la captura directa, sin desafíos

        stateMachine.process(data)

        if (stateMachine.isComplete) {
            if (eventType == "OUT") {
                // OUT: reconocimiento facial tras liveness
                _uiState.update { it.copy(phase = SilhouettePhase.Matched, shouldCapture = true) }
            } else {
                // BREAK / FOOD: actualizar fase ANTES del coroutine para bloquear re-entradas
                // del hilo de análisis de cámara antes de que la corrutina la actualice
                _uiState.update { it.copy(phase = SilhouettePhase.Matched) }
                viewModelScope.launch {
                    val session = sessionRepository.getActiveSession().first()
                    if (session == null) {
                        stateMachine = LivenessStateMachine(LivenessChallenge.randomSequence())
                        _uiState.update { it.copy(phase = SilhouettePhase.Framing, challengeInstruction = "") }
                        return@launch
                    }
                    val deviceId = deviceIdManager.getOrCreate()
                    registerEventUseCase(session.inspectorId, EventType.from(eventType), deviceId)
                    _uiState.update {
                        it.copy(
                            matchedName = session.inspectorName,
                            matchedCode = session.documentId,
                        )
                    }
                    delay(1400)
                    _uiState.update { it.copy(navigateToSuccess = true) }
                }
            }
            return
        }

        val instruction = stateMachine.currentChallenge?.instruction ?: ""
        if (_uiState.value.challengeInstruction != instruction) {
            _uiState.update { it.copy(challengeInstruction = instruction) }
        }
    }

    // ── Callback desde Screen tras capturar el JPEG (solo IN / OUT) ──────────

    fun onImageCaptured(base64: String) {
        _uiState.update { it.copy(shouldCapture = false) }
        viewModelScope.launch {
            val deviceId = deviceIdManager.getOrCreate()
            val evType = EventType.from(eventType)
            verifyFaceUseCase(base64, deviceId)
                .onSuccess { inspector ->
                    registerEventUseCase(inspector.id, evType, deviceId)
                    when (evType) {
                        EventType.IN -> sessionRepository.saveSession(
                            Session(
                                inspectorId   = inspector.id,
                                inspectorName = inspector.name,
                                documentId    = inspector.documentId,
                                role          = inspector.role,
                            )
                        )
                        EventType.OUT -> sessionRepository.clearSession()
                        else -> Unit
                    }
                    _uiState.update {
                        it.copy(
                            phase       = SilhouettePhase.Matched,
                            matchedName = inspector.name,
                            matchedCode = inspector.documentId,
                        )
                    }
                    delay(1400)
                    _uiState.update { it.copy(navigateToSuccess = true) }
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

    fun onNavigated() {
        _uiState.update { it.copy(navigateToSuccess = false) }
    }
}
