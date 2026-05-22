package com.j4x.iris_ids.ui.screens.admindashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.domain.usecase.GetInspectorsUseCase
import com.j4x.iris_ids.ui.screens.home.WorkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveEvent(
    val inspectorName: String,
    val inspectorCode: String,
    val eventType: String,
    val time: String,
    val isEntrada: Boolean,
)

data class InspectorStatus(
    val name: String,
    val code: String,
    val role: String,
    val workStatus: WorkStatus,
)

data class AdminDashboardUiState(
    val selectedTab: Int = 0,
    // ── Tiempo real ───────────────────────────────────────────────────────────
    val activeCount: Int = 0,
    val onPauseCount: Int = 0,
    val outCount: Int = 0,
    val liveEvents: List<LiveEvent> = emptyList(),
    // ── Inspectores ───────────────────────────────────────────────────────────
    val inspectors: List<InspectorStatus> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getInspectorsUseCase: GetInspectorsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadInspectors()
    }

    fun selectTab(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    fun loadInspectors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            getInspectorsUseCase()
                .onSuccess { list ->
                    val inspectors = list.map { inspector ->
                        InspectorStatus(
                            name       = inspector.name,
                            code       = inspector.documentId,
                            role       = inspector.role.ifBlank { "Inspector de campo" },
                            workStatus = WorkStatus.Out,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            inspectors   = inspectors,
                            activeCount  = 0,
                            onPauseCount = 0,
                            outCount     = inspectors.size,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, loadError = "No se pudo cargar la lista de inspectores")
                    }
                }
        }
    }
}
