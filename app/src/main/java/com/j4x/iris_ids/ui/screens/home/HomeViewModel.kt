package com.j4x.iris_ids.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.db.dao.PendingEventDao
import com.j4x.iris_ids.data.local.db.entity.PendingEventEntity
import com.j4x.iris_ids.domain.repository.SessionRepository
import com.j4x.iris_ids.ui.components.ReminderTone
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
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

enum class WorkStatus { Out, In, OnBreak, OnFood }

data class AttendanceEvent(
    val type: String,
    val description: String,
    val time: String,
    val isEntrada: Boolean,
)

data class ReminderItem(
    val actionName: String,
    val message: String,
    val tone: ReminderTone,
)

data class HomeUiState(
    val inspectorName: String = "",
    val inspectorCode: String = "",
    val dateLabel: String = "",
    val workStatus: WorkStatus = WorkStatus.Out,
    val statusSince: String = "--:--",
    val hoursToday: String = "--",
    val eventsCount: Int = 0,
    val reminder: ReminderItem? = null,
    val events: List<AttendanceEvent> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val pendingEventDao: PendingEventDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val zone = ZoneId.systemDefault()

    init {
        val locale = Locale("es", "ES")
        val today = LocalDate.now()
        val dayName = today.dayOfWeek
            .getDisplayName(TextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() }
        val month = today.month.getDisplayName(TextStyle.FULL, locale)
        val dateLabel = "$dayName · ${today.dayOfMonth} de $month de ${today.year}"
        _uiState.update { it.copy(dateLabel = dateLabel) }

        viewModelScope.launch {
            sessionRepository.getActiveSession().collectLatest { session ->
                if (session == null) return@collectLatest
                pendingEventDao.getByInspector(session.inspectorId).collect { entities ->
                    val today2 = LocalDate.now()
                    val todayEntities = entities.filter { e ->
                        runCatching {
                            Instant.parse(e.timestamp).atZone(zone).toLocalDate() == today2
                        }.getOrDefault(false)
                    }
                    val attendanceEvents = todayEntities.map { it.toDisplay() }
                    val lastEvent = todayEntities.lastOrNull()
                    val workStatus = deriveWorkStatus(lastEvent?.eventType)
                    val statusSince = lastEvent?.let { e ->
                        runCatching {
                            Instant.parse(e.timestamp).atZone(zone).format(timeFormatter)
                        }.getOrDefault("--:--")
                    } ?: "--:--"
                    _uiState.update {
                        it.copy(
                            inspectorName = session.inspectorName,
                            inspectorCode = session.documentId,
                            workStatus    = workStatus,
                            statusSince   = statusSince,
                            eventsCount   = attendanceEvents.size,
                            events        = attendanceEvents,
                        )
                    }
                }
            }
        }
    }

    private fun PendingEventEntity.toDisplay(): AttendanceEvent {
        val time = runCatching {
            Instant.parse(timestamp).atZone(zone).format(timeFormatter)
        }.getOrDefault("--:--")
        val isEntrada = eventType == "IN" || eventType == "BREAK_IN" || eventType == "FOOD_IN"
        val (typeName, desc) = when (eventType) {
            "IN"        -> "Entrada"  to "Inicio de jornada"
            "BREAK_OUT" -> "Descanso" to "Pausa breve"
            "BREAK_IN"  -> "Regreso"  to "Fin de descanso"
            "FOOD_OUT"  -> "Almuerzo" to "Pausa de almuerzo"
            "FOOD_IN"   -> "Regreso"  to "Fin de almuerzo"
            "OUT"       -> "Salida"   to "Fin de jornada"
            else        -> "Evento"   to "Evento registrado"
        }
        return AttendanceEvent(typeName, desc, time, isEntrada)
    }

    private fun deriveWorkStatus(lastEventType: String?): WorkStatus = when (lastEventType) {
        "IN", "BREAK_IN", "FOOD_IN" -> WorkStatus.In
        "BREAK_OUT"                 -> WorkStatus.OnBreak
        "FOOD_OUT"                  -> WorkStatus.OnFood
        else                        -> WorkStatus.Out
    }
}
