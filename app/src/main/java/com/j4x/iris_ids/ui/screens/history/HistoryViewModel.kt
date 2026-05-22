package com.j4x.iris_ids.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.db.dao.PendingEventDao
import com.j4x.iris_ids.data.local.db.entity.PendingEventEntity
import com.j4x.iris_ids.domain.model.AttendanceEvent
import com.j4x.iris_ids.domain.repository.SessionRepository
import com.j4x.iris_ids.domain.usecase.GetAttendanceHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class HistoryEvent(
    val type: String,
    val description: String,
    val time: String,
    val isEntrada: Boolean,
)

data class DayRecord(
    val dateLabel: String,
    val dayShort: String,
    val isToday: Boolean,
    val totalTime: String,
    val isComplete: Boolean,
    val events: List<HistoryEvent>,
)

data class HistoryUiState(
    val inspectorName: String = "",
    val inspectorCode: String = "",
    val days: List<DayRecord> = emptyList(),
    val isOffline: Boolean = false,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getHistory: GetAttendanceHistoryUseCase,
    private val pendingEventDao: PendingEventDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val zone = ZoneId.systemDefault()
    private val locale = Locale("es", "ES")

    init {
        viewModelScope.launch {
            sessionRepository.getActiveSession().collectLatest { session ->
                if (session == null) return@collectLatest
                _uiState.update { it.copy(inspectorName = session.inspectorName, inspectorCode = session.documentId) }
                loadHistory(session.inspectorId)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val session = sessionRepository.getActiveSession().first() ?: return@launch
            loadHistory(session.inspectorId)
        }
    }

    private suspend fun loadHistory(inspectorId: String) {
        getHistory(inspectorId)
            .onSuccess { events ->
                _uiState.update {
                    it.copy(days = buildDaysFromApi(events), isOffline = false)
                }
            }
            .onFailure {
                // Sin conexión — muestra eventos pendientes de Room
                val pending = pendingEventDao.getByInspector(inspectorId).first()
                _uiState.update {
                    it.copy(days = buildDaysFromRoom(pending), isOffline = true)
                }
            }
    }

    // ── Construye DayRecords desde la respuesta del servidor ─────────────────

    private fun buildDaysFromApi(events: List<AttendanceEvent>): List<DayRecord> {
        val today = LocalDate.now()
        val byDate = events
            .mapNotNull { e ->
                runCatching {
                    Instant.parse(e.timestamp).atZone(zone).toLocalDate() to e
                }.getOrNull()
            }
            .groupBy({ it.first }, { it.second })
            .entries
            .sortedByDescending { it.key }

        return byDate.map { (date, dayEvents) ->
            val sortedEvents = dayEvents.sortedBy { it.timestamp }
            DayRecord(
                dateLabel  = buildDateLabel(date),
                dayShort   = buildDayShort(date, today),
                isToday    = date == today,
                totalTime  = "--",
                isComplete = dayEvents.any { it.eventType.id == "OUT" },
                events     = sortedEvents.map { it.toHistoryEvent() },
            )
        }
    }

    // ── Construye DayRecords desde Room (modo offline) ────────────────────────

    private fun buildDaysFromRoom(entities: List<PendingEventEntity>): List<DayRecord> {
        val today = LocalDate.now()
        val byDate = entities
            .mapNotNull { e ->
                runCatching {
                    Instant.parse(e.timestamp).atZone(zone).toLocalDate() to e
                }.getOrNull()
            }
            .groupBy({ it.first }, { it.second })
            .entries
            .sortedByDescending { it.key }

        return byDate.map { (date, dayEntities) ->
            DayRecord(
                dateLabel  = buildDateLabel(date),
                dayShort   = buildDayShort(date, today),
                isToday    = date == today,
                totalTime  = "--",
                isComplete = dayEntities.any { it.eventType == "OUT" },
                events     = dayEntities.map { it.toHistoryEvent() },
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildDateLabel(date: LocalDate): String {
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
        val month = date.month.getDisplayName(TextStyle.FULL, locale)
        return "$dayName, ${date.dayOfMonth} de $month"
    }

    private fun buildDayShort(date: LocalDate, today: LocalDate): String = when {
        date == today              -> "HOY"
        date == today.minusDays(1) -> "AYER"
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
    }

    private fun AttendanceEvent.toHistoryEvent(): HistoryEvent {
        val time = runCatching {
            Instant.parse(timestamp).atZone(zone).format(timeFormatter)
        }.getOrDefault("--:--")
        val isEntrada = eventType.isEntrada
        val (typeName, desc) = eventType.toDisplay()
        return HistoryEvent(typeName, desc, time, isEntrada)
    }

    private fun PendingEventEntity.toHistoryEvent(): HistoryEvent {
        val time = runCatching {
            Instant.parse(timestamp).atZone(zone).format(timeFormatter)
        }.getOrDefault("--:--")
        val isEntrada = eventType == "IN" || eventType == "BREAK_IN" || eventType == "FOOD_IN"
        val (typeName, desc) = eventTypeToDisplay(eventType)
        return HistoryEvent(typeName, desc, time, isEntrada)
    }

    private fun com.j4x.iris_ids.domain.model.EventType.toDisplay(): Pair<String, String> =
        eventTypeToDisplay(id)

    private fun eventTypeToDisplay(eventType: String): Pair<String, String> = when (eventType) {
        "IN"        -> "Entrada"  to "Inicio de jornada"
        "BREAK_OUT" -> "Descanso" to "Pausa breve"
        "BREAK_IN"  -> "Regreso"  to "Fin de descanso"
        "FOOD_OUT"  -> "Almuerzo" to "Pausa de almuerzo"
        "FOOD_IN"   -> "Regreso"  to "Fin de almuerzo"
        "OUT"       -> "Salida"   to "Fin de jornada"
        else        -> "Evento"   to "Evento registrado"
    }
}
