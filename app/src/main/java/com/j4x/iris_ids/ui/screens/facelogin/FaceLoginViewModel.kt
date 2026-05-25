package com.j4x.iris_ids.ui.screens.facelogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.prefs.DeviceIdManager
import com.j4x.iris_ids.domain.model.EventType
import com.j4x.iris_ids.domain.model.Session
import com.j4x.iris_ids.domain.repository.SessionRepository
import com.j4x.iris_ids.domain.usecase.RegisterEventUseCase
import com.j4x.iris_ids.domain.usecase.VerifyFaceUseCase
import com.j4x.iris_ids.ui.camera.FaceData
import com.j4x.iris_ids.ui.camera.FaceQuality
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
    val matchedName: String? = null,
    val matchedCode: String? = null,
    val navigateToHome: Boolean = false,
    val shouldCapture: Boolean = false,
    val quality: FaceQuality = FaceQuality(),
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

    // ── ShutterButton tap (solo en Framing) ──────────────────────────────────

    fun startVerification() {
        if (_uiState.value.phase != SilhouettePhase.Framing) return
        if (!_uiState.value.quality.isReady) return
        _uiState.update { it.copy(phase = SilhouettePhase.Scanning, shouldCapture = true) }
    }

    // ── Callback desde FaceAnalyzer — actualiza calidad en tiempo real ────────

    fun onFaceData(data: FaceData?) {
        if (_uiState.value.phase != SilhouettePhase.Framing) return
        val quality = if (data == null) FaceQuality() else FaceQuality.from(data)
        _uiState.update { it.copy(quality = quality) }
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
                    _uiState.update { it.copy(phase = SilhouettePhase.Framing) }
                }
        }
    }

    fun onNavigatedToHome() {
        _uiState.update { FaceLoginUiState() }
    }
}
