package com.j4x.iris_ids.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisPrimarySoft
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSalidaSoft
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun TimelineEntry(
    actionName: String,
    description: String,
    time: String,
    isEntrada: Boolean,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (isEntrada) IrisPrimary else IrisSalida
    val accentSoft = if (isEntrada) IrisPrimarySoft else IrisSalidaSoft
    val directionChar = if (isEntrada) "→" else "←"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(IrisSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = directionChar, color = accentColor, fontSize = 16.sp)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = actionName,
                style = MaterialTheme.typography.titleMedium,
                color = IrisInk,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = IrisInkSoft,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = IrisInkSoft,
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(IrisDone),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = IrisSurface,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}
