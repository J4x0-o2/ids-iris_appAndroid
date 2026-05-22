package com.j4x.iris_ids.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisDangerSoft
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSalidaSoft
import com.j4x.iris_ids.ui.theme.IrisSurface

enum class ReminderTone { Upcoming, Now, Overdue }

@Composable
fun ReminderBanner(
    actionName: String,
    message: String,
    tone: ReminderTone,
    modifier: Modifier = Modifier,
) {
    val (bg, border, tint) = when (tone) {
        ReminderTone.Overdue -> Triple(IrisDangerSoft, IrisDanger, IrisDanger)
        ReminderTone.Now -> Triple(IrisSalidaSoft, IrisSalida, IrisSalida)
        ReminderTone.Upcoming -> Triple(IrisSurface, IrisLine, IrisInkSoft)
    }

    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "Recordatorio",
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
            Text(
                text = "$actionName · $message",
                style = MaterialTheme.typography.bodyMedium,
                color = tint,
            )
        }
    }
}
