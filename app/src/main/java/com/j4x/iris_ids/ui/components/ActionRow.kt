package com.j4x.iris_ids.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisPrimarySoft
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSalidaSoft
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun ActionRow(
    name: String,
    description: String,
    isEntrada: Boolean,
    isNext: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val accentColor = if (isEntrada) IrisPrimary else IrisSalida
    val accentSoft = if (isEntrada) IrisPrimarySoft else IrisSalidaSoft
    val labelText = if (isEntrada) "Entrada" else "Salida"

    val shape = RoundedCornerShape(16.dp)
    val borderModifier = if (isNext) {
        Modifier.border(1.5.dp, IrisPrimary, shape)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isCompleted) 0.5f else 1f)
            .then(borderModifier)
            .clip(shape)
            .background(IrisSurface)
            .clickable(enabled = !isCompleted, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = IrisInk,
                )
                if (isNext) {
                    Text(
                        text = "SIGUIENTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = IrisPrimary,
                        modifier = Modifier
                            .background(IrisPrimarySoft, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                text = if (isCompleted) "Ya registrado" else description,
                style = MaterialTheme.typography.bodySmall,
                color = IrisInkSoft,
            )
        }

        Text(
            text = labelText,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            modifier = Modifier
                .background(accentSoft, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
