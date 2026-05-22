package com.j4x.iris_ids.ui.screens.adminlogin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.components.IrisTextField
import com.j4x.iris_ids.ui.components.PrimaryButton
import com.j4x.iris_ids.ui.components.ScreenHeader
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisDangerSoft
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun AdminLoginScreen(
    navController: NavController,
    viewModel: AdminLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateToDashboard) {
        if (uiState.navigateToDashboard) {
            navController.navigate(Screen.AdminDashboard.route) {
                popUpTo(Screen.AdminLogin.route) { inclusive = true }
            }
            viewModel.onNavigated()
        }
    }

    // ── Diálogo de configuración de URL ──────────────────────────────────────
    if (uiState.showUrlDialog) {
        UrlConfigDialog(
            urlInput       = uiState.urlInput,
            checkStatus    = uiState.urlCheckStatus,
            onUrlChange    = viewModel::onUrlInput,
            onTest         = viewModel::testUrl,
            onSave         = viewModel::saveUrl,
            onDismiss      = viewModel::closeUrlDialog,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        // ── Header con ícono de configuración ─────────────────────────────────
        Box(modifier = Modifier.background(IrisSurface)) {
            ScreenHeader(
                title = "Acceso administrador",
                onBack = { navController.navigateUp() },
            )
            IconButton(
                onClick  = viewModel::openUrlDialog,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.Settings,
                    contentDescription = "Configurar URL del servidor",
                    tint               = IrisInkFaint,
                    modifier           = Modifier.size(22.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(IrisPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = IrisSurface,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Administrador",
                style = MaterialTheme.typography.headlineSmall,
                color = IrisInk,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ingresa tus credenciales para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = IrisInkSoft,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .background(IrisSurface, RoundedCornerShape(20.dp))
                    .border(1.dp, IrisLine, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IrisTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Usuario",
                    placeholder = "admin",
                    isError = uiState.usernameError != null,
                    errorMessage = uiState.usernameError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    enabled = !uiState.isLoading,
                )

                IrisTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Contraseña",
                    placeholder = "••••••••",
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                tint = IrisInkFaint,
                            )
                        }
                    },
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.login()
                        }
                    ),
                    enabled = !uiState.isLoading,
                )

                uiState.authError?.let { error ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IrisDangerSoft, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = IrisDanger,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = IrisDanger,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                PrimaryButton(
                    text = if (uiState.isLoading) "Verificando…" else "Iniciar sesión",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.login()
                    },
                    enabled = !uiState.isLoading,
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterHorizontally),
                        color = IrisPrimary,
                        strokeWidth = 2.dp,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Diálogo de configuración de URL del servidor ──────────────────────────────

@Composable
private fun UrlConfigDialog(
    urlInput: String,
    checkStatus: UrlCheckStatus,
    onUrlChange: (String) -> Unit,
    onTest: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = IrisSurface,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(
                text  = "Servidor",
                style = MaterialTheme.typography.titleLarge,
                color = IrisInk,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text  = "Ingresa la dirección IP y puerto del servidor en tu red Wi-Fi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IrisInkSoft,
                )

                IrisTextField(
                    value         = urlInput,
                    onValueChange = onUrlChange,
                    label         = "URL base",
                    placeholder   = "http://192.168.x.x:8090/",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction    = ImeAction.Done,
                    ),
                )

                // Botón probar
                OutlinedButton(
                    onClick  = onTest,
                    enabled  = urlInput.isNotBlank() && checkStatus != UrlCheckStatus.Checking,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = IrisPrimary),
                ) {
                    if (checkStatus == UrlCheckStatus.Checking) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(14.dp),
                            color       = IrisPrimary,
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(
                        text     = if (checkStatus == UrlCheckStatus.Checking) "  Verificando…" else "Probar conexión",
                        modifier = Modifier.padding(start = if (checkStatus == UrlCheckStatus.Checking) 6.dp else 0.dp),
                    )
                }

                // Resultado de la prueba
                when (checkStatus) {
                    UrlCheckStatus.Valid -> StatusRow(
                        color = IrisDone,
                        label = "Servidor alcanzable",
                        icon  = { Icon(Icons.Default.CheckCircle, null, tint = IrisDone, modifier = Modifier.size(14.dp)) },
                    )
                    UrlCheckStatus.Invalid -> StatusRow(
                        color = IrisDanger,
                        label = "No se pudo conectar",
                        icon  = { Icon(Icons.Default.Warning, null, tint = IrisDanger, modifier = Modifier.size(14.dp)) },
                    )
                    else -> Unit
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = urlInput.isNotBlank(),
                colors  = ButtonDefaults.buttonColors(containerColor = IrisPrimary),
                shape   = RoundedCornerShape(12.dp),
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = IrisInkSoft)
            }
        },
    )
}

@Composable
private fun StatusRow(color: Color, label: String, icon: @Composable () -> Unit) {
    Row(
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
