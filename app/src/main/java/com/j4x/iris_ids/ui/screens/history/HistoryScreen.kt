package com.j4x.iris_ids.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.ui.components.ScreenHeader
import com.j4x.iris_ids.ui.components.TimelineEntry
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisPrimarySoft
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header sticky dentro del scroll
            item {
                Box(modifier = Modifier.background(IrisSurface)) {
                    ScreenHeader(
                        title = "Historial",
                        subtitle = uiState.inspectorCode,
                        onBack = { navController.navigateUp() },
                    )
                }
            }

            // Banner offline
            if (uiState.isOffline) {
                item {
                    Text(
                        text = "Sin conexión — mostrando eventos pendientes de sincronización",
                        style = MaterialTheme.typography.labelSmall,
                        color = IrisDanger,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IrisDanger.copy(alpha = 0.08f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Estado vacío
            if (uiState.days.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Sin eventos registrados",
                            style = MaterialTheme.typography.titleSmall,
                            color = IrisInkFaint,
                        )
                        Text(
                            text = "Los registros aparecerán aquí cuando se sincronicen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = IrisInkFaint,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            }

            // Grupos por día
            uiState.days.forEach { record ->
                item(key = "header_${record.dateLabel}") {
                    DayHeader(record = record)
                }
                items(
                    items = record.events,
                    key = { "${record.dateLabel}_${it.time}_${it.type}" },
                ) { event ->
                    TimelineEntry(
                        actionName  = event.type,
                        description = event.description,
                        time        = event.time,
                        isEntrada   = event.isEntrada,
                        modifier    = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                }
                item(key = "gap_${record.dateLabel}") {
                    Spacer(Modifier.height(16.dp))
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Cabecera de día ───────────────────────────────────────────────────────────

@Composable
private fun DayHeader(record: DayRecord) {
    val badgeColor     = if (record.isToday) IrisPrimary else IrisInkFaint
    val badgeBg        = if (record.isToday) IrisPrimarySoft else IrisBg
    val timeColor      = when {
        record.isToday && !record.isComplete -> IrisDone
        record.isComplete                    -> IrisInkFaint
        else                                 -> IrisInkFaint
    }
    val timeSuffix     = if (record.isToday && !record.isComplete) " activo" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = record.dayShort,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
            Text(
                text = record.dateLabel,
                style = MaterialTheme.typography.titleSmall,
                color = IrisInk,
            )
        }
        Text(
            text = record.totalTime + timeSuffix,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = timeColor,
        )
    }
}
