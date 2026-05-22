package com.j4x.iris_ids.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.j4x.iris_ids.ui.theme.IrisCameraBg
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun NoCameraPermission(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier.background(IrisCameraBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.VideocamOff,
                contentDescription = null,
                tint = IrisSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(52.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Acceso a cámara requerido",
                style = MaterialTheme.typography.titleMedium,
                color = IrisSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Esta función requiere permiso de cámara para la verificación biométrica. Otorgue el permiso en los ajustes del dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = IrisSurface.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                },
                border = BorderStroke(1.dp, IrisSurface.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IrisSurface),
            ) {
                Text("Abrir ajustes del sistema")
            }
        }
    }
}
