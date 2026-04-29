package com.example.lendloop.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lendloop.Screens.AddRecordScreen
import com.example.lendloop.Screens.HistoryScreen
import com.example.lendloop.Screens.HomeScreen
import com.example.lendloop.Screens.LoginScreen
import com.example.lendloop.Screens.PaymentScreen
import com.example.lendloop.Screens.PersonScreen
import com.example.lendloop.Screens.ProfileScreen
import com.example.lendloop.Screens.RegisterScreen
import com.example.lendloop.Screens.WelcomeScreen
import com.example.lendloop.screens.ReviewScreen
import com.example.lendloop.util.SessionManager

@Composable
fun LendLoopNavGraph(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (sessionManager.isLoggedIn()) Routes.HOME else Routes.WELCOME

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── Welcome ───────────────────────────────────────────────────────
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin    = { navController.navigate(Routes.LOGIN) }
            )
        }

        // ── Register ──────────────────────────────────────────────────────
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ─────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onAddRecord    = { navController.navigate(Routes.ADD_RECORD) },
                onHistory      = { navController.navigate(Routes.HISTORY) },
                onProfile      = { navController.navigate(Routes.PROFILE) },
                onRecordClick  = { recordId ->
                    navController.navigate(Routes.personRoute(recordId))
                },
                onReviewRecord = { recordId, revieweeId ->
                    navController.navigate(Routes.reviewRoute(recordId, revieweeId))
                },
                onPayRecord    = { recordId, amount, personName ->
                    navController.navigate(Routes.paymentRoute(recordId, amount, personName))
                },
                onLogout       = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Add Record ────────────────────────────────────────────────────
        composable(Routes.ADD_RECORD) {
            AddRecordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── History ───────────────────────────────────────────────────────
        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Person Detail ─────────────────────────────────────────────────
        composable(
            route     = Routes.PERSON,
            arguments = listOf(
                navArgument("personId") { type = NavType.IntType }
            )
        ) {
            PersonScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Review ────────────────────────────────────────────────────────
        composable(
            route     = Routes.REVIEW,
            arguments = listOf(
                navArgument("recordId")   { type = NavType.IntType },
                navArgument("revieweeId") { type = NavType.IntType }
            )
        ) {
            ReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onDone         = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Payment ───────────────────────────────────────────────────────
        composable(
            route     = Routes.PAYMENT,
            arguments = listOf(
                navArgument("recordId")   { type = NavType.IntType },
                navArgument("amount")     { type = NavType.FloatType },
                navArgument("personName") { type = NavType.StringType }
            )
        ) {
            PaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                onPaymentDone  = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}