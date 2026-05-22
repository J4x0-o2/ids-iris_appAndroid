package com.j4x.iris_ids.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.j4x.iris_ids.ui.screens.adminlogin.AdminLoginScreen
import com.j4x.iris_ids.ui.screens.admindashboard.AdminDashboardScreen
import com.j4x.iris_ids.ui.screens.capture.CaptureScreen
import com.j4x.iris_ids.ui.screens.choose.ChooseScreen
import com.j4x.iris_ids.ui.screens.history.HistoryScreen
import com.j4x.iris_ids.ui.screens.success.SuccessScreen
import com.j4x.iris_ids.ui.screens.enroll.EnrollScreen
import com.j4x.iris_ids.ui.screens.facelogin.FaceLoginScreen
import com.j4x.iris_ids.ui.screens.home.HomeScreen
import com.j4x.iris_ids.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object FaceLogin : Screen("face_login")
    object Enroll : Screen("enroll")
    object Home : Screen("home")
    object Choose : Screen("choose")
    object History : Screen("history")
    object AdminLogin : Screen("admin_login")
    object AdminDashboard : Screen("admin_dashboard")

    object Capture : Screen("capture/{eventType}") {
        fun buildRoute(eventType: String) = "capture/$eventType"
        const val ARG_EVENT_TYPE = "eventType"
    }

    object Success : Screen("success/{eventType}/{workerName}/{workerId}") {
        fun buildRoute(eventType: String, workerName: String, workerId: String) =
            "success/$eventType/$workerName/$workerId"
        const val ARG_EVENT_TYPE = "eventType"
        const val ARG_WORKER_NAME = "workerName"
        const val ARG_WORKER_ID = "workerId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.FaceLogin.route) {
            FaceLoginScreen(navController = navController)
        }

        composable(Screen.Enroll.route) {
            EnrollScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Choose.route) {
            ChooseScreen(navController = navController)
        }

        composable(
            route = Screen.Capture.route,
            arguments = listOf(
                navArgument(Screen.Capture.ARG_EVENT_TYPE) { type = NavType.StringType }
            )
        ) {
            CaptureScreen(navController = navController)
        }

        composable(
            route = Screen.Success.route,
            arguments = listOf(
                navArgument(Screen.Success.ARG_EVENT_TYPE) { type = NavType.StringType },
                navArgument(Screen.Success.ARG_WORKER_NAME) { type = NavType.StringType },
                navArgument(Screen.Success.ARG_WORKER_ID) { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val eventType  = backStackEntry.arguments?.getString(Screen.Success.ARG_EVENT_TYPE)  ?: return@composable
            val workerName = backStackEntry.arguments?.getString(Screen.Success.ARG_WORKER_NAME) ?: return@composable
            val workerId   = backStackEntry.arguments?.getString(Screen.Success.ARG_WORKER_ID)   ?: return@composable
            SuccessScreen(
                navController = navController,
                eventType     = eventType,
                workerName    = workerName,
                workerId      = workerId,
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.AdminLogin.route) {
            AdminLoginScreen(navController = navController)
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
    }
}
