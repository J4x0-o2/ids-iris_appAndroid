package com.j4x.iris_ids.ui.screens.facelogin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.j4x.iris_ids.ui.theme.IrisScanMatch
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun FaceLoginScreen(
    navController: NavController,
    viewModel: FaceLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    // ── Captura JPEG cuando liveness completa ────────────────────────────────
    LaunchedEffect(uiState.shouldCapture) {
        if (uiState.shouldCapture) {
            val base64 = captureController.takePicture()
            viewModel.onImageCaptured(base64)
        }
    }

    // ── Navegación a Home tras match ─────────────────────────────────────────
    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.FaceLogin.route) { inclusive = true }
            }
            viewModel.onNavigatedToHome()
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
                // Viñeta radial sobre el preview
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
                // Silueta + brackets encima del preview
                Box(
                    modifier = Modifier.size(width = ovalWidth, height = ovalHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    FaceSilhouette(
                        phase       = uiState.phase,
                        accentColor = IrisDone,
                        modifier    = Modifier.fillMaxSize(),
                    )
                    CornerBrackets(
                        modifier    = Modifier.fillMaxSize(),
                        color       = IrisSurface.copy(alpha = 0.45f),
                        bracketSize = 20.dp,
                    )
                }
            } else {
                NoCameraPermission(modifier = Modifier.fillMaxSize())
            }
        }

        // ── Header ────────────────────────────────────────────────────────────
        ScreenHeader(
            title    = "Iniciar sesión",
            subtitle = "Verificación facial",
            darkMode = true,
            onBack   = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // ── Panel inferior ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bottomGradient)
                .padding(top = 72.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StatusMessage(phase = uiState.phase, instruction = uiState.challengeInstruction)

            if (uiState.phase == SilhouettePhase.Matched && uiState.matchedName != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text      = uiState.matchedName!!,
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

            ShutterButton(phase = uiState.phase, onClick = viewModel::startVerification)

            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun StatusMessage(phase: SilhouettePhase, instruction: String) {
    val (text, color) = when (phase) {
        SilhouettePhase.Framing  -> "Centre su rostro dentro del marco" to IrisSurface.copy(alpha = 0.75f)
        SilhouettePhase.Scanning -> instruction.ifEmpty { "Iniciando…" } to IrisSurface.copy(alpha = 0.90f)
        SilhouettePhase.Matched  -> "Identidad verificada" to IrisScanMatch
    }
    Text(
        text      = text,
        style     = MaterialTheme.typography.bodyMedium,
        color     = color,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ShutterButton(phase: SilhouettePhase, onClick: () -> Unit) {
    val ringColor  = if (phase == SilhouettePhase.Matched) IrisDone else IrisSurface.copy(alpha = 0.6f)
    val innerColor = when (phase) {
        SilhouettePhase.Framing  -> IrisDone
        SilhouettePhase.Scanning -> IrisDone.copy(alpha = 0.4f)
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
