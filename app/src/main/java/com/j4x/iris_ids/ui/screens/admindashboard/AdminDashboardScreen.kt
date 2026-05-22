package com.j4x.iris_ids.ui.screens.admindashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.components.AvatarInitials
import com.j4x.iris_ids.ui.components.StatCard
import com.j4x.iris_ids.ui.screens.home.WorkStatus
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisDanger
import com.j4x.iris_ids.ui.theme.IrisDone
import com.j4x.iris_ids.ui.theme.IrisDoneSoft
import com.j4x.iris_ids.ui.theme.IrisInk
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisInkSoft
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisSalida
import com.j4x.iris_ids.ui.theme.IrisSalidaSoft
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadInspectors()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        Box(
            modifier = Modifier
                .background(IrisSurface)
                .fillMaxWidth(),
        ) {
            AdminTopBar(onLogout = { navController.popBackStack() })
        }

        TabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor   = IrisSurface,
            contentColor     = IrisPrimary,
        ) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick  = { viewModel.selectTab(0) },
                text     = { Text("Tiempo real") },
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick  = { viewModel.selectTab(1) },
                text     = { Text("Inspectores") },
            )
        }

        Crossfade(
            targetState = uiState.selectedTab,
            modifier    = Modifier.weight(1f),
            label       = "dashboard_tab",
        ) { tab ->
            when (tab) {
                0 -> LiveFeedTab(
                    activeCount  = uiState.activeCount,
                    onPauseCount = uiState.onPauseCount,
                    outCount     = uiState.outCount,
                    events       = uiState.liveEvents,
                    modifier     = Modifier.fillMaxSize(),
                )
                else -> InspectorsTab(
                    inspectors    = uiState.inspectors,
                    isLoading     = uiState.isLoading,
                    loadError     = uiState.loadError,
                    onCreateClick = { navController.navigate(Screen.Enroll.route) },
                    onRetry       = viewModel::loadInspectors,
                    modifier      = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .background(IrisPrimary, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text       = "IDS",
                color      = IrisSurface,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize   = 11.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text     = "Panel de administración",
            style    = MaterialTheme.typography.titleMedium,
            color    = IrisInk,
            modifier = Modifier.weight(1f),
        )

        TextButton(
            onClick = onLogout,
            colors  = ButtonDefaults.textButtonColors(contentColor = IrisDanger),
        ) {
            Text(
                text       = "Salir",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ── Live feed tab ─────────────────────────────────────────────────────────────

@Composable
private fun LiveFeedTab(
    activeCount: Int,
    onPauseCount: Int,
    outCount: Int,
    events: List<LiveEvent>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier       = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    label       = "Activos",
                    value       = activeCount.toString(),
                    accentColor = IrisDone,
                    modifier    = Modifier.weight(1f),
                )
                StatCard(
                    label       = "En pausa",
                    value       = onPauseCount.toString(),
                    accentColor = IrisSalida,
                    modifier    = Modifier.weight(1f),
                )
                StatCard(
                    label       = "Fuera",
                    value       = outCount.toString(),
                    accentColor = IrisInkFaint,
                    modifier    = Modifier.weight(1f),
                )
            }
        }

        item {
            Text(
                text     = "Actividad reciente",
                style    = MaterialTheme.typography.labelLarge,
                color    = IrisInkSoft,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "Sin actividad reciente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IrisInkFaint,
                    )
                }
            }
        } else {
            items(events) { event ->
                LiveEventRow(event = event)
            }
        }
    }
}

@Composable
private fun LiveEventRow(event: LiveEvent) {
    val dotColor = if (event.isEntrada) IrisDone else IrisSalida

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurface, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = event.inspectorName,
                style      = MaterialTheme.typography.bodyMedium,
                color      = IrisInk,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text       = "${event.eventType} · ${event.inspectorCode}",
                style      = MaterialTheme.typography.bodySmall,
                color      = IrisInkSoft,
                fontFamily = FontFamily.Monospace,
            )
        }

        Text(
            text       = event.time,
            style      = MaterialTheme.typography.bodySmall,
            color      = IrisInkFaint,
            fontFamily = FontFamily.Monospace,
        )
    }
}

// ── Inspectors tab ────────────────────────────────────────────────────────────

@Composable
private fun InspectorsTab(
    inspectors: List<InspectorStatus>,
    isLoading: Boolean,
    loadError: String?,
    onCreateClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            isLoading && inspectors.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = IrisPrimary,
                )
            }
            loadError != null && inspectors.isEmpty() -> {
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text  = loadError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IrisDanger,
                    )
                    TextButton(onClick = onRetry) {
                        Text(text = "Reintentar", color = IrisPrimary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 16.dp,
                        bottom = 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (inspectors.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text  = "No hay inspectores registrados.\nToca + para agregar el primero.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IrisInkFaint,
                                )
                            }
                        }
                    } else {
                        items(inspectors) { inspector ->
                            InspectorCard(inspector = inspector)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick        = onCreateClick,
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = IrisPrimary,
            contentColor   = IrisSurface,
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "Agregar inspector",
            )
        }
    }
}

@Composable
private fun InspectorCard(inspector: InspectorStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurface, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AvatarInitials(name = inspector.name, size = 44.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = inspector.name,
                style      = MaterialTheme.typography.bodyMedium,
                color      = IrisInk,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text       = inspector.code,
                style      = MaterialTheme.typography.bodySmall,
                color      = IrisInkSoft,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text  = inspector.role,
                style = MaterialTheme.typography.bodySmall,
                color = IrisInkFaint,
            )
        }

        StatusBadge(workStatus = inspector.workStatus)
    }
}

@Composable
private fun StatusBadge(workStatus: WorkStatus) {
    val (label, bg, fg) = when (workStatus) {
        WorkStatus.In      -> Triple("Activo",   IrisDoneSoft,   IrisDone)
        WorkStatus.OnBreak -> Triple("Descanso", IrisSalidaSoft, IrisSalida)
        WorkStatus.OnFood  -> Triple("Almuerzo", IrisSalidaSoft, IrisSalida)
        WorkStatus.Out     -> Triple("Fuera",    IrisBg,         IrisInkFaint)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = fg,
            fontWeight = FontWeight.Medium,
        )
    }
}
