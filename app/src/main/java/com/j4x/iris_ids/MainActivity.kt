package com.j4x.iris_ids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.j4x.iris_ids.navigation.AppNavigation
import com.j4x.iris_ids.ui.theme.IrisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IrisTheme {
                AppNavigation()
            }
        }
    }
}
