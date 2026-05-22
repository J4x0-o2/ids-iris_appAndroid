package com.j4x.iris_ids.ui.screens.capture

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.camera.CameraPreview
import com.j4x.iris_ids.ui.camera.CaptureController
import com.j4x.iris_ids.ui.components.CornerBrackets
import com.j4x.iris_ids.ui.components.FaceSilhouette
import com.j4x.iris_ids.ui.components.NoCameraPermission
import com.j4x.iris_ids.ui.components.ScreenHeader
import com.j4x.iris_ids.ui.components.SilhouettePhase
import com.j4x.iris_ids.ui.theme.IrisCameraBg
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisScanMatch
import com.j4x.iris_ids.ui.theme.IrisSurface
import java.net.URLEncoder

@Composable
fun CaptureScreen(
    navController: NavController,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val accentColor = when (uiState.accent) {
        EventAccent.Entrada -> IrisPrimary
        EventAccent.Salida  -> IrisSalida
    }

    // ── Permiso de cámara ────────────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Re-chequea el permiso al volver de Ajustes del sistema
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── CaptureController ────────────────────────────────────────────────────
    val captureController = remember { CaptureController() }

    // ── Captura JPEG cuando liveness completa (solo IN/OUT) ──────────────────
    LaunchedEffect(uiState.shouldCapture) {
        if (uiState.shouldCapture) {
            val base64 = captureController.takePicture()
            viewModel.onImageCaptured(base64)
        }
    }

    // ── Navegación a Success ─────────────────────────────────────────────────
    LaunchedEffect(uiState.navigateToSuccess) {
        if (uiState.navigateToSuccess) {
            uiState.matchedName?.let { name ->
                val encodedName = URLEncoder.encode(name, "UTF-8").replace("+", "%20")
                navController.navigate(
                    Screen.Success.buildRoute(
                        eventType  = uiState.eventType,
                        workerName = encodedName,
                        workerId   = uiState.matchedCode ?: "",
                    )
                ) {
                    popUpTo(Screen.Choose.route) { inclusive = true }
                }
            }
            viewModel.onNavigated()
        }
    }

    val configuration = LocalConfiguration.current
    val cameraAreaHeight = (configuration.screenHeightDp * 0.67f).coerceAtLeast(560f).dp
    val ovalWidth = (configuration.screenWidthDp * 0.60f).coerceAtLeast(260f).dp
    val ovalHeight = ovalWidth * (360f / 260f)

    val bottomGradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, IrisCameraBg.copy(alpha = 0.85f), IrisCameraBg),
        startY = 0f,
        endY   = 400f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisCameraBg),
    ) {
        // ── Área de cámara ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cameraAreaHeight)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    captureController = captureController,
                    onFaceData        = viewModel::onFaceData,
                    modifier          = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    IrisCameraBg.copy(alpha = 0.25f),
                                    IrisCameraBg.copy(alpha = 0.55f),
                                ),
                            )
                        )
                )
                Box(
                    modifier = Modifier.size(width = ovalWidth, height = ovalHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    FaceSilhouette(
                        phase       = uiState.phase,
                        accentColor = accentColor,
                        modifier    = Modifier.fillMaxSize(),
                    )
                    CornerBrackets(
                        modifier    = Modifier.fillMaxSize(),
                        color       = accentColor.copy(alpha = 0.45f),
                        bracketSize = 20.dp,
                    )
                }
            } else {
                NoCameraPermission(modifier = Modifier.fillMaxSize())
            }
        }

        // ── Header ────────────────────────────────────────────────────────────
        ScreenHeader(
            title    = uiState.eventLabel,
            subtitle = if (uiState.requiresLiveness) "Verificación biométrica" else "Confirmación de identidad",
            darkMode = true,
            onBack   = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // ── Panel inferior ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bottomGradient)
                .navigationBarsPadding()
                .padding(top = 72.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (uiState.phase == SilhouettePhase.Framing) {
                Text(
                    text     = uiState.eventLabel.uppercase(),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = accentColor,
                    modifier = Modifier
                        .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            CaptureStatusMessage(uiState = uiState, accentColor = accentColor)

            uiState.matchedName?.let { name ->
                if (uiState.phase == SilhouettePhase.Matched) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text      = name,
                            style     = MaterialTheme.typography.titleLarge,
                            color     = IrisScanMatch,
                            textAlign = TextAlign.Center,
                        )
                        uiState.matchedCode?.let {
                            Text(
                                text  = it,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = IrisScanMatch.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }

            CaptureShutterButton(
                phase       = uiState.phase,
                accentColor = accentColor,
                onClick     = viewModel::startCapture,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CaptureStatusMessage(uiState: CaptureUiState, accentColor: androidx.compose.ui.graphics.Color) {
    val (text, color) = when (uiState.phase) {
        SilhouettePhase.Framing  -> {
            val msg = if (uiState.requiresLiveness) "Centre su rostro dentro del marco"
                      else "Confirme su identidad"
            msg to IrisSurface.copy(alpha = 0.75f)
        }
        SilhouettePhase.Scanning -> {
            val msg = if (uiState.requiresLiveness) uiState.challengeInstruction.ifEmpty { "Iniciando…" }
                      else "Verificando…"
            msg to IrisSurface.copy(alpha = 0.90f)
        }
        SilhouettePhase.Matched  -> "Identidad confirmada" to IrisScanMatch
    }
    Text(
        text      = text,
        style     = MaterialTheme.typography.bodyMedium,
        color     = color,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CaptureShutterButton(
    phase: SilhouettePhase,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val ringColor  = if (phase == SilhouettePhase.Matched) IrisDone else IrisSurface.copy(alpha = 0.6f)
    val innerColor = when (phase) {
        SilhouettePhase.Framing  -> accentColor
        SilhouettePhase.Scanning -> accentColor.copy(alpha = 0.4f)
        SilhouettePhase.Matched  -> IrisDone
    }
    val enabled = phase == SilhouettePhase.Framing

    Box(
        modifier = Modifier
            .size(76.dp)
            .border(2.dp, ringColor, CircleShape)
            .padding(7.dp)
            .clip(CircleShape)
            .background(innerColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    )
}
