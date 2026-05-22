package com.j4x.iris_ids.ui.screens.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.components.AvatarInitials
import com.j4x.iris_ids.ui.components.ButtonVariant
import com.j4x.iris_ids.ui.components.PrimaryButton
import com.j4x.iris_ids.ui.components.ReminderBanner
import com.j4x.iris_ids.ui.components.StatCard
import com.j4x.iris_ids.ui.components.TimelineEntry
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        HomeTopBar(uiState = uiState)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            DateStrip(label = uiState.dateLabel)
            Spacer(Modifier.height(16.dp))
            HeroCard(uiState = uiState)
            Spacer(Modifier.height(12.dp))
            StatsRow(uiState = uiState)

            uiState.reminder?.let { reminder ->
                Spacer(Modifier.height(12.dp))
                ReminderBanner(
                    actionName = reminder.actionName,
                    message = reminder.message,
                    tone = reminder.tone,
                )
            }

            if (uiState.events.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                TimelineSection(events = uiState.events)
                TextButton(
                    onClick = { navController.navigate(Screen.History.route) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Ver historial completo",
                        style = MaterialTheme.typography.bodySmall,
                        color = IrisPrimary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        CtaBar(
            workStatus = uiState.workStatus,
            onRegister = { navController.navigate(Screen.Choose.route) },
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(uiState: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(IrisPrimary, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "IDS",
                color = IrisSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Inspector Detection",
                style = MaterialTheme.typography.titleSmall,
                color = IrisInk,
            )
            if (uiState.inspectorCode.isNotBlank()) {
                Text(
                    text = uiState.inspectorCode,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = IrisInkFaint,
                )
            }
        }
        AvatarInitials(name = uiState.inspectorName, size = 40.dp)
    }
}

// ── Date strip ────────────────────────────────────────────────────────────────

@Composable
private fun DateStrip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = IrisInkSoft,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(uiState: HomeUiState) {
    val isActive = uiState.workStatus == WorkStatus.In
    val isBreak = uiState.workStatus == WorkStatus.OnBreak || uiState.workStatus == WorkStatus.OnFood

    val bgColor = when (uiState.workStatus) {
        WorkStatus.In -> IrisPrimary
        WorkStatus.Out -> IrisSurface
        WorkStatus.OnBreak, WorkStatus.OnFood -> IrisSalida
    }
    val textColor = when (uiState.workStatus) {
        WorkStatus.Out -> IrisInk
        else -> IrisSurface
    }
    val subtitleColor = when (uiState.workStatus) {
        WorkStatus.Out -> IrisInkSoft
        else -> IrisSurface.copy(alpha = 0.70f)
    }
    val statusLabel = when (uiState.workStatus) {
        WorkStatus.In -> "TRABAJANDO"
        WorkStatus.Out -> "FUERA DE TURNO"
        WorkStatus.OnBreak -> "EN DESCANSO"
        WorkStatus.OnFood -> "EN ALMUERZO"
    }

    val borderMod = if (uiState.workStatus == WorkStatus.Out) {
        Modifier.border(1.dp, IrisLine, RoundedCornerShape(20.dp))
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusDot(label = statusLabel, active = isActive || isBreak, color = textColor)
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.inspectorName,
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = uiState.inspectorCode,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = subtitleColor,
            )

            if (uiState.workStatus != WorkStatus.Out) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = IrisSurface.copy(alpha = 0.15f))
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Text(
                            text = uiState.hoursToday,
                            style = MaterialTheme.typography.headlineMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "horas activas",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Desde las",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor,
                        )
                        Text(
                            text = uiState.statusSince,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(label: String, active: Boolean, color: androidx.compose.ui.graphics.Color) {
    val pulse = rememberInfiniteTransition(label = "status_dot")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (active) color.copy(alpha = dotAlpha) else color.copy(alpha = 0.4f),
                    shape = CircleShape,
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.85f),
            letterSpacing = 1.sp,
        )
    }
}

// ── Stats row ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(uiState: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = "Tiempo activo",
            value = uiState.hoursToday,
            accentColor = IrisDone,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Eventos hoy",
            value = "${uiState.eventsCount}",
            accentColor = IrisPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Timeline ──────────────────────────────────────────────────────────────────

@Composable
private fun TimelineSection(events: List<AttendanceEvent>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Actividad de hoy",
            style = MaterialTheme.typography.titleSmall,
            color = IrisInkSoft,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        events.forEach { event ->
            TimelineEntry(
                actionName = event.type,
                description = event.description,
                time = event.time,
                isEntrada = event.isEntrada,
            )
        }
    }
}

// ── CTA sticky ────────────────────────────────────────────────────────────────

@Composable
private fun CtaBar(workStatus: WorkStatus, onRegister: () -> Unit) {
    val (label, variant) = when (workStatus) {
        WorkStatus.Out -> "Registrar evento" to ButtonVariant.Entrada
        WorkStatus.In -> "Registrar evento" to ButtonVariant.Salida
        WorkStatus.OnBreak, WorkStatus.OnFood -> "Registrar regreso" to ButtonVariant.Entrada
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .background(IrisBg)
            .navigationBarsPadding()
            .padding(PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)),
    ) {
        PrimaryButton(text = label, onClick = onRegister, variant = variant)
    }
}
