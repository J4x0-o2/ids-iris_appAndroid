package com.j4x.iris_ids.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisPrimary

enum class SilhouettePhase { Framing, Scanning, Matched }

@Composable
fun FaceSilhouette(
    phase: SilhouettePhase,
    modifier: Modifier = Modifier,
    accentColor: Color = IrisPrimary,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "silhouette")

    // Pulso suave para la fase Framing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    // Alpha de los landmarks
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_alpha",
    )

    // Línea de escaneo (ciclo continuo en fase Scanning)
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scan_y",
    )

    val silhouetteColor = if (phase == SilhouettePhase.Matched) IrisDone else accentColor
    val scale = if (phase == SilhouettePhase.Framing) pulseScale else 1f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.40f

        val faceW = w * 0.52f * scale
        val faceH = h * 0.56f * scale

        val faceTop = cy - faceH / 2f
        val faceBottom = cy + faceH / 2f

        val silhouetteAlpha = when (phase) {
            SilhouettePhase.Framing -> 0.55f
            SilhouettePhase.Scanning -> 0.75f
            SilhouettePhase.Matched -> 1f
        }

        // --- Óvalo del rostro ---
        drawOval(
            color = silhouetteColor.copy(alpha = silhouetteAlpha),
            topLeft = Offset(cx - faceW / 2f, faceTop),
            size = Size(faceW, faceH),
            style = Stroke(width = 2.dp.toPx()),
        )

        // --- Hombros ---
        val shoulderY = faceBottom + h * 0.05f
        val shoulderPath = Path().apply {
            moveTo(cx - w * 0.38f, h)
            quadraticTo(cx - w * 0.26f, shoulderY, cx, shoulderY + h * 0.012f)
            quadraticTo(cx + w * 0.26f, shoulderY, cx + w * 0.38f, h)
        }
        drawPath(
            path = shoulderPath,
            color = silhouetteColor.copy(alpha = silhouetteAlpha * 0.45f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )

        // --- Landmarks (12 puntos) ---
        val landmarks = listOf(
            Offset(cx, faceTop + faceH * 0.06f),               // frente
            Offset(cx - faceW * 0.36f, cy - faceH * 0.18f),    // sien izq
            Offset(cx + faceW * 0.36f, cy - faceH * 0.18f),    // sien der
            Offset(cx - faceW * 0.22f, cy - faceH * 0.08f),    // ojo izq
            Offset(cx + faceW * 0.22f, cy - faceH * 0.08f),    // ojo der
            Offset(cx - faceW * 0.09f, cy + faceH * 0.02f),    // nariz izq
            Offset(cx + faceW * 0.09f, cy + faceH * 0.02f),    // nariz der
            Offset(cx, cy + faceH * 0.12f),                     // labio sup
            Offset(cx - faceW * 0.14f, cy + faceH * 0.22f),    // boca izq
            Offset(cx + faceW * 0.14f, cy + faceH * 0.22f),    // boca der
            Offset(cx - faceW * 0.40f, cy + faceH * 0.08f),    // mejilla izq
            Offset(cx + faceW * 0.40f, cy + faceH * 0.08f),    // mejilla der
        )

        val landmarkAlpha = when (phase) {
            SilhouettePhase.Framing -> dotAlpha * 0.5f
            SilhouettePhase.Scanning -> 0.5f + scanY * 0.3f
            SilhouettePhase.Matched -> 1f
        }

        val dotRadius = 3.dp.toPx()
        landmarks.forEach { point ->
            drawCircle(
                color = silhouetteColor.copy(alpha = landmarkAlpha),
                radius = dotRadius,
                center = point,
            )
        }

        // --- Líneas de conexión en Matched ---
        if (phase == SilhouettePhase.Matched) {
            val connections = listOf(
                landmarks[3] to landmarks[4],  // ojos
                landmarks[5] to landmarks[6],  // nariz
                landmarks[8] to landmarks[7],  // boca izq → centro
                landmarks[7] to landmarks[9],  // centro → boca der
            )
            connections.forEach { (a, b) ->
                drawLine(
                    color = silhouetteColor.copy(alpha = 0.35f),
                    start = a,
                    end = b,
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        // --- Línea de escaneo en Scanning ---
        if (phase == SilhouettePhase.Scanning) {
            val currentY = faceTop + faceH * scanY
            val lineHalfW = faceW / 2f * 0.9f

            // Glow difuso
            drawLine(
                color = silhouetteColor.copy(alpha = 0.18f),
                start = Offset(cx - lineHalfW, currentY),
                end = Offset(cx + lineHalfW, currentY),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round,
            )
            // Línea principal
            drawLine(
                color = silhouetteColor.copy(alpha = 0.85f),
                start = Offset(cx - lineHalfW, currentY),
                end = Offset(cx + lineHalfW, currentY),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}
