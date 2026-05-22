package com.j4x.iris_ids.ui.screens.success

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisDoneSoft
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisPrimarySoft
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSalidaSoft
import com.j4x.iris_ids.ui.theme.IrisSurface
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

private const val AUTO_CLOSE_MS = 3200

@Composable
fun SuccessScreen(
    navController: NavController,
    eventType: String,
    workerName: String,
    workerId: String,
) {
    val isEntrada = eventType == "IN" || eventType == "BREAK_IN" || eventType == "FOOD_IN"
    val accentColor = if (isEntrada) IrisPrimary else IrisSalida
    val accentSoft  = if (isEntrada) IrisPrimarySoft else IrisSalidaSoft

    val eventLabel = when (eventType) {
        "IN"        -> "¡Entrada registrada!"
        "BREAK_OUT" -> "¡Descanso registrado!"
        "BREAK_IN"  -> "¡Regreso registrado!"
        "FOOD_OUT"  -> "¡Almuerzo registrado!"
        "FOOD_IN"   -> "¡Regreso registrado!"
        "OUT"       -> "¡Salida registrada!"
        else        -> "¡Evento registrado!"
    }
    val badgeLabel = when (eventType) {
        "IN"        -> "ENTRADA"
        "BREAK_OUT" -> "DESCANSO"
        "BREAK_IN"  -> "REGRESO"
        "FOOD_OUT"  -> "ALMUERZO"
        "FOOD_IN"   -> "REGRESO"
        "OUT"       -> "SALIDA"
        else        -> "EVENTO"
    }

    val timeLabel = remember {
        LocalTime.now().let { String.format("%02d:%02d", it.hour, it.minute) }
    }
    val dateLabel = remember {
        val locale = Locale("es", "ES")
        val today = LocalDate.now()
        val day = today.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
        "$day ${today.dayOfMonth}"
    }

    // ── Animaciones ──────────────────────────────────────────────────────────
    var appear by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (appear) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "check_scale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (appear) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 300),
        label = "content_alpha",
    )
    var progressTarget by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = AUTO_CLOSE_MS, easing = LinearEasing),
        label = "auto_close_progress",
    )

    LaunchedEffect(Unit) {
        appear = true
        progressTarget = 1f
        delay(AUTO_CLOSE_MS.toLong())
        if (eventType == "OUT") {
            navController.navigate(Screen.Splash.route) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.popBackStack()
        }
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        // Contenido scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))

            // Checkmark animado
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(checkScale)
                    .background(IrisDoneSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = IrisDone,
                    modifier = Modifier.size(64.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Título + subtítulo
            Column(
                modifier = Modifier.graphicsLayer { alpha = contentAlpha },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = eventLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = IrisInk,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Tu registro ha sido guardado correctamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IrisInkSoft,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(32.dp))

            // Card de detalles
            Column(
                modifier = Modifier
                    .graphicsLayer { alpha = contentAlpha }
                    .fillMaxWidth()
                    .background(IrisSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, IrisLine, RoundedCornerShape(16.dp))
                    .padding(20.dp),
            ) {
                // Badge tipo de evento
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    modifier = Modifier
                        .background(accentSoft, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = workerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = IrisInk,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = workerId,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = IrisInkSoft,
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = IrisLine)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                        color = IrisInk,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = IrisInkFaint,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // Barra de cuenta regresiva sticky al fondo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Cerrando automáticamente…",
                style = MaterialTheme.typography.bodySmall,
                color = IrisInkFaint,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(IrisLine),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(accentColor),
                )
            }
        }
    }
}
