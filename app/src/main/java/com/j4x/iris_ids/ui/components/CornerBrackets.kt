package com.j4x.iris_ids.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun CornerBrackets(
    modifier: Modifier = Modifier,
    color: Color = IrisSurface,
    strokeWidth: Dp = 2.dp,
    bracketSize: Dp = 24.dp,
) {
    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val bracket = bracketSize.toPx()
        val w = size.width
        val h = size.height

        fun line(start: Offset, end: Offset) = drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = stroke,
            cap = StrokeCap.Square,
        )

        // Top-left
        line(Offset(0f, bracket), Offset(0f, 0f))
        line(Offset(0f, 0f), Offset(bracket, 0f))

        // Top-right
        line(Offset(w - bracket, 0f), Offset(w, 0f))
        line(Offset(w, 0f), Offset(w, bracket))

        // Bottom-left
        line(Offset(0f, h - bracket), Offset(0f, h))
        line(Offset(0f, h), Offset(bracket, h))

        // Bottom-right
        line(Offset(w - bracket, h), Offset(w, h))
        line(Offset(w, h - bracket), Offset(w, h))
    }
}
