package com.j4x.iris_ids.ui.screens.choose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.db.dao.PendingEventDao
import com.j4x.iris_ids.domain.repository.SessionRepository
import com.j4x.iris_ids.ui.screens.home.WorkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class ActionState { Pending, Next, Done }

data class ChooseAction(
    val id: String,
    val name: String,
    val description: String,
    val isEntrada: Boolean,
    val state: ActionState,
)

data class ChooseUiState(
    val inspectorName: String = "",
    val inspectorCode: String = "",
    val actions: List<ChooseAction> = emptyList(),
)

@HiltViewModel
class ChooseViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val pendingEventDao: PendingEventDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseUiState())
    val uiState: StateFlow<ChooseUiState> = _uiState.asStateFlow()

    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            sessionRepository.getActiveSession().collectLatest { session ->
                if (session == null) return@collectLatest
                pendingEventDao.getByInspector(session.inspectorId).collect { entities ->
                    val today = LocalDate.now()
                    val todayEntities = entities.filter { e ->
                        runCatching {
                            Instant.parse(e.timestamp).atZone(zone).toLocalDate() == today
                        }.getOrDefault(false)
                    }
                    val lastTodayEvent = todayEntities.lastOrNull()
                    val workStatus = when (lastTodayEvent?.eventType) {
                        "IN", "BREAK_IN", "FOOD_IN" -> WorkStatus.In
                        "BREAK_OUT"                 -> WorkStatus.OnBreak
                        "FOOD_OUT"                  -> WorkStatus.OnFood
                        else                        -> WorkStatus.Out
                    }
                    val doneEvents = todayEntities.map { it.eventType }.toSet()

                    _uiState.update {
                        it.copy(
                            inspectorName = session.inspectorName,
                            inspectorCode = session.documentId,
                            actions       = actionsForStatus(workStatus, doneEvents),
                        )
                    }
                }
            }
        }
    }

    private fun actionsForStatus(
        status: WorkStatus,
        doneEvents: Set<String>,
    ): List<ChooseAction> {
        fun stateFor(id: String, isNextWhen: Boolean): ActionState = when {
            id in doneEvents -> ActionState.Done
            isNextWhen       -> ActionState.Next
            else             -> ActionState.Pending
        }
        return listOf(
            action("IN",        "Entrada",          "Inicio de jornada",   true,  stateFor("IN",        status == WorkStatus.Out)),
            action("BREAK_OUT", "Descanso",         "Pausa breve",         false, stateFor("BREAK_OUT", status == WorkStatus.In)),
            action("BREAK_IN",  "Regreso descanso", "Retorno de pausa",    true,  stateFor("BREAK_IN",  status == WorkStatus.OnBreak)),
            action("FOOD_OUT",  "Almuerzo",         "Pausa de almuerzo",   false, stateFor("FOOD_OUT",  status == WorkStatus.In)),
            action("FOOD_IN",   "Regreso almuerzo", "Retorno de almuerzo", true,  stateFor("FOOD_IN",   status == WorkStatus.OnFood)),
            action("OUT",       "Salida",           "Fin de jornada",      false, stateFor("OUT",       status == WorkStatus.In)),
        )
    }

    private fun action(id: String, name: String, desc: String, isEntrada: Boolean, state: ActionState) =
        ChooseAction(id, name, desc, isEntrada, state)
}
