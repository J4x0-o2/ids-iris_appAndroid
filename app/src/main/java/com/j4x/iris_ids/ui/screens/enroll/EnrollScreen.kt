package com.j4x.iris_ids.ui.screens.enroll

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.camera.CameraPreview
import com.j4x.iris_ids.ui.camera.CaptureController
import com.j4x.iris_ids.ui.camera.FaceData
import com.j4x.iris_ids.ui.components.CornerBrackets
import com.j4x.iris_ids.ui.components.FaceSilhouette
import com.j4x.iris_ids.ui.components.IrisTextField
import com.j4x.iris_ids.ui.components.NoCameraPermission
import com.j4x.iris_ids.ui.components.PrimaryButton
import com.j4x.iris_ids.ui.components.ScreenHeader
import com.j4x.iris_ids.ui.components.SilhouettePhase
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisCameraBg
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisDoneSoft
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisPrimarySoft
import com.j4x.iris_ids.ui.theme.IrisScanMatch
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun EnrollScreen(
    navController: NavController,
    viewModel: EnrollViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.step != EnrollStep.Success) {
        if (!viewModel.goBack()) navController.popBackStack()
    }

    AnimatedContent(
        targetState = uiState.step,
        transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            if (forward) {
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "enroll_step",
    ) { step ->
        when (step) {
            EnrollStep.Instructions -> InstructionsStep(
                onNext = viewModel::goToCapture,
                onBack = { navController.popBackStack() },
            )
            EnrollStep.Capture -> CaptureStep(
                uiState        = uiState,
                onShutter      = viewModel::startCapture,
                onFaceData     = viewModel::onFaceData,
                onImageCaptured = viewModel::onImageCaptured,
                onBack         = { viewModel.goBack() },
            )
            EnrollStep.Form -> FormStep(
                uiState       = uiState,
                onNameChange  = viewModel::onNameChange,
                onCodeChange  = viewModel::onCodeChange,
                onRoleChange  = viewModel::onRoleChange,
                onSubmit      = viewModel::submitForm,
                onBack        = { viewModel.goBack() },
            )
            EnrollStep.Success -> SuccessStep(
                uiState   = uiState,
                onFinish  = {
                    viewModel.reset()
                    navController.popBackStack()
                },
            )
        }
    }
}

// ── Paso 1: Instrucciones ─────────────────────────────────────────────────────

@Composable
private fun InstructionsStep(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        ScreenHeader(title = "Nuevo inspector", subtitle = "Registro biométrico", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            StepDots(current = EnrollStep.Instructions)
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(IrisPrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = IrisPrimary,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Registra tu identidad",
                style = MaterialTheme.typography.headlineSmall,
                color = IrisInk,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Capturaremos tu rostro para que el sistema te reconozca en futuros accesos. El proceso toma menos de un minuto.",
                style = MaterialTheme.typography.bodyMedium,
                color = IrisInkSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            listOf(
                "1" to "Posiciona tu rostro dentro del marco y presiona el botón",
                "2" to "Gira lentamente hacia la izquierda, luego hacia la derecha",
                "3" to "Completa tus datos de inspector",
            ).forEach { (num, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(IrisPrimary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = num, style = MaterialTheme.typography.labelMedium, color = IrisSurface)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IrisInk,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
            PrimaryButton(text = "Comenzar captura", onClick = onNext)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Paso 2: Captura biométrica ────────────────────────────────────────────────

@Composable
private fun CaptureStep(
    uiState: EnrollUiState,
    onShutter: () -> Unit,
    onFaceData: (FaceData?) -> Unit,
    onImageCaptured: (String) -> Unit,
    onBack: () -> Unit,
) {
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
            onImageCaptured(base64)
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
                    onFaceData        = onFaceData,
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
                        phase       = uiState.capturePhase,
                        accentColor = IrisSurface,
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

        ScreenHeader(
            title    = "Captura biométrica",
            subtitle = "Paso 2 de 4",
            darkMode = true,
            onBack   = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bottomGradient)
                .navigationBarsPadding()
                .padding(top = 72.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val (statusText, statusColor) = when (uiState.capturePhase) {
                SilhouettePhase.Framing  -> "Centre su rostro dentro del marco" to IrisSurface.copy(alpha = 0.75f)
                SilhouettePhase.Scanning -> uiState.challengeInstruction.ifEmpty { "Iniciando…" } to IrisSurface.copy(alpha = 0.85f)
                SilhouettePhase.Matched  -> "Captura completada" to IrisScanMatch
            }
            Text(
                text      = statusText,
                style     = MaterialTheme.typography.bodyMedium,
                color     = statusColor,
                textAlign = TextAlign.Center,
            )
            EnrollShutterButton(phase = uiState.capturePhase, onClick = onShutter)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EnrollShutterButton(phase: SilhouettePhase, onClick: () -> Unit) {
    val ringColor  = if (phase == SilhouettePhase.Matched) IrisDone else IrisSurface.copy(alpha = 0.6f)
    val innerColor = when (phase) {
        SilhouettePhase.Framing  -> IrisSurface.copy(alpha = 0.9f)
        SilhouettePhase.Scanning -> IrisSurface.copy(alpha = 0.35f)
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

// ── Paso 3: Formulario ────────────────────────────────────────────────────────

@Composable
private fun FormStep(
    uiState: EnrollUiState,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg)
            .imePadding(),
    ) {
        ScreenHeader(title = "Datos del inspector", subtitle = "Paso 3 de 4", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            StepDots(current = EnrollStep.Form)
            Spacer(Modifier.height(24.dp))

            IrisTextField(
                value         = uiState.name,
                onValueChange = onNameChange,
                label         = "Nombre completo",
                placeholder   = "Ej. María González",
                isError       = uiState.nameError != null,
                errorMessage  = uiState.nameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Next,
                ),
            )
            Spacer(Modifier.height(16.dp))
            IrisTextField(
                value         = uiState.code,
                onValueChange = onCodeChange,
                label         = "Número de documento",
                placeholder   = "Número de documento",
                isMonospace   = true,
                isError       = uiState.codeError != null,
                errorMessage  = uiState.codeError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction    = ImeAction.Next,
                ),
            )
            Spacer(Modifier.height(16.dp))
            IrisTextField(
                value         = uiState.role,
                onValueChange = onRoleChange,
                label         = "Cargo (opcional)",
                placeholder   = "Ej. Inspector de campo",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Done,
                ),
            )
            Spacer(Modifier.height(32.dp))
            PrimaryButton(
                text    = if (uiState.isSubmitting) "Registrando…" else "Registrar inspector",
                onClick = onSubmit,
                enabled = !uiState.isSubmitting,
            )
            if (uiState.isSubmitting) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier    = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally),
                    color       = IrisPrimary,
                    strokeWidth = 2.dp,
                )
            }
            if (uiState.enrollError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = uiState.enrollError,
                    color = IrisDanger,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Paso 4: Éxito ─────────────────────────────────────────────────────────────

@Composable
private fun SuccessStep(uiState: EnrollUiState, onFinish: () -> Unit) {
    var appear by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue  = if (appear) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "check_scale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue  = if (appear) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 300),
        label        = "content_alpha",
    )
    LaunchedEffect(Unit) { appear = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(checkScale)
                .background(IrisDoneSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = IrisDone,
                modifier = Modifier.size(64.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text      = "¡Registro exitoso!",
                style     = MaterialTheme.typography.headlineSmall,
                color     = IrisInk,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "El inspector ha sido registrado correctamente en el sistema.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = IrisInkSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IrisSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, IrisLine, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text  = uiState.name.ifBlank { "Inspector" },
                    style = MaterialTheme.typography.titleMedium,
                    color = IrisInk,
                )
                if (uiState.code.isNotBlank()) {
                    Text(
                        text  = uiState.code,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = IrisInkSoft,
                    )
                }
                if (uiState.role.isNotBlank()) {
                    Text(
                        text  = uiState.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = IrisInkFaint,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
            PrimaryButton(text = "Finalizar", onClick = onFinish)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Indicador de pasos ────────────────────────────────────────────────────────

@Composable
private fun StepDots(current: EnrollStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        EnrollStep.values().forEach { step ->
            val isActive    = step == current
            val isCompleted = step.ordinal < current.ordinal
            val width by animateDpAsState(
                targetValue  = if (isActive) 24.dp else 8.dp,
                label        = "dot_${step.name}",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .background(
                        color = if (isActive || isCompleted) IrisPrimary else IrisLine,
                        shape = RoundedCornerShape(4.dp),
                    )
            )
        }
    }
}
