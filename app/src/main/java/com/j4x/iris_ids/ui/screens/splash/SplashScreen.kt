package com.j4x.iris_ids.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.R
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.components.ButtonVariant
import com.j4x.iris_ids.ui.components.PrimaryButton
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination  by viewModel.destination.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()
    val currentUrl   by viewModel.currentUrl.collectAsStateWithLifecycle()

    // Re-verifica el servidor al volver de otra pantalla (ej: AdminLogin tras configurar URL)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.recheck()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(destination) {
        when (destination) {
            SplashViewModel.Destination.Home -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
                viewModel.onNavigated()
            }
            SplashViewModel.Destination.ShowButtons, null -> Unit
        }
    }

    var visible by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue  = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label        = "splash_alpha",
    )
    LaunchedEffect(Unit) { visible = true }

    val logoTransition = rememberInfiniteTransition(label = "logo_pulse")
    val logoScale by logoTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.04f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg)
            .graphicsLayer { alpha = contentAlpha },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter         = painterResource(R.drawable.logo_ids),
                contentDescription = "IRIS IDS",
                contentScale    = ContentScale.Fit,
                modifier        = Modifier
                    .size(150.dp)
                    .scale(logoScale),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text          = "INSPECTOR DETECTION SYSTEM",
                style         = MaterialTheme.typography.labelSmall,
                color         = IrisInkFaint,
                letterSpacing = 2.sp,
                textAlign     = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text      = "Bienvenido",
                style     = MaterialTheme.typography.headlineLarge,
                color     = IrisInk,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Seleccione cómo desea ingresar al sistema",
                style     = MaterialTheme.typography.bodyMedium,
                color     = IrisInkSoft,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(44.dp))

            AnimatedVisibility(
                visible = destination == SplashViewModel.Destination.ShowButtons,
                enter   = fadeIn(tween(300)),
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PrimaryButton(
                        text    = "Inicio",
                        onClick = { navController.navigate(Screen.FaceLogin.route) },
                        variant = ButtonVariant.Entrada,
                        leadingIcon = {
                            Icon(
                                imageVector        = Icons.Default.Face,
                                contentDescription = null,
                                tint               = IrisSurface,
                                modifier           = Modifier.size(20.dp),
                            )
                        },
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick  = { navController.navigate(Screen.AdminLogin.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, IrisPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = IrisSurface,
                            contentColor   = IrisPrimary,
                        ),
                    ) {
                        Row(
                            verticalAlignment      = Alignment.CenterVertically,
                            horizontalArrangement  = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Person,
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp),
                                tint               = IrisPrimary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = "Inicio admin",
                                style = MaterialTheme.typography.titleMedium,
                                color = IrisPrimary,
                            )
                        }
                    }
                }
            }

            if (destination != SplashViewModel.Destination.ShowButtons) {
                Spacer(Modifier.height(116.dp))
            }
        }

        // ── Indicador de conexión al servidor ────────────────────────────────
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.Center,
            ) {
                when (serverStatus) {
                    ServerStatus.Checking -> {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(8.dp),
                            color       = IrisInkFaint,
                            strokeWidth = 1.5.dp,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = "Verificando conexión…",
                            style = MaterialTheme.typography.labelSmall,
                            color = IrisInkFaint,
                        )
                    }
                    ServerStatus.Online -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IrisDone),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = "Servidor conectado",
                            style = MaterialTheme.typography.labelSmall,
                            color = IrisDone,
                        )
                    }
                    ServerStatus.Offline -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IrisDanger),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = "Sin conexión al servidor",
                            style = MaterialTheme.typography.labelSmall,
                            color = IrisDanger,
                        )
                    }
                }
            }

            // URL actual siendo verificada
            Text(
                text  = currentUrl.removePrefix("http://").trimEnd('/'),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 10.sp,
                ),
                color = when (serverStatus) {
                    ServerStatus.Online  -> IrisDone.copy(alpha = 0.7f)
                    ServerStatus.Offline -> IrisDanger.copy(alpha = 0.7f)
                    ServerStatus.Checking -> IrisInkFaint
                },
            )
        }

        Text(
            text      = "Al iniciar sesión, su rostro será reconocido automáticamente",
            style     = MaterialTheme.typography.bodySmall,
            color     = IrisInkFaint,
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 20.dp),
        )
    }
}
