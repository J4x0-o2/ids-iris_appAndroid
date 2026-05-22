package com.j4x.iris_ids.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSurface

enum class ButtonVariant { Entrada, Salida, Danger }

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Entrada,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val containerColor = when {
        !enabled -> IrisLine
        variant == ButtonVariant.Salida -> IrisSalida
        variant == ButtonVariant.Danger -> IrisDanger
        else -> IrisPrimary
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = IrisSurface,
            disabledContainerColor = IrisLine,
            disabledContentColor = IrisInkFaint,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
