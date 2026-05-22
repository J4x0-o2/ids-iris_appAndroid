package com.j4x.iris_ids.ui.screens.choose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.j4x.iris_ids.navigation.Screen
import com.j4x.iris_ids.ui.components.ActionRow
import com.j4x.iris_ids.ui.components.ScreenHeader
import com.j4x.iris_ids.ui.theme.IrisBg
import com.j4x.iris_ids.ui.theme.IrisInkFaint
import com.j4x.iris_ids.ui.theme.IrisSurface

@Composable
fun ChooseScreen(
    navController: NavController,
    viewModel: ChooseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBg),
    ) {
        Box(modifier = Modifier.background(IrisSurface)) {
            ScreenHeader(
                title = "Registrar evento",
                subtitle = uiState.inspectorCode,
                onBack = { navController.navigateUp() },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Selecciona el evento a registrar",
                style = MaterialTheme.typography.bodySmall,
                color = IrisInkFaint,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )

            uiState.actions.forEach { action ->
                ActionRow(
                    name = action.name,
                    description = action.description,
                    isEntrada = action.isEntrada,
                    isNext = action.state == ActionState.Next,
                    isCompleted = action.state == ActionState.Done,
                    onClick = { navController.navigate(Screen.Capture.buildRoute(action.id)) },
                    icon = { ActionIcon(actionId = action.id) },
                )
            }
        }
    }
}

@Composable
private fun ActionIcon(actionId: String) {
    val icon = when (actionId) {
        "IN"        -> Icons.AutoMirrored.Filled.Login
        "BREAK_OUT" -> Icons.Filled.Coffee
        "BREAK_IN"  -> Icons.Filled.Replay
        "FOOD_OUT"  -> Icons.Filled.Restaurant
        "FOOD_IN"   -> Icons.Filled.Replay
        "OUT"       -> Icons.AutoMirrored.Filled.Logout
        else        -> Icons.Filled.Replay
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = IrisSurface,
    )
}
