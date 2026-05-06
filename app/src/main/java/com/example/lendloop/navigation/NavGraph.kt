package com.example.lendloop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.lendloop.Screens.*
import com.example.lendloop.util.SessionManager

@Composable
fun LendLoopNavGraph(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    // ✅ After login → Dashboard (role selector) first, not Home
    val startDestination =
        if (sessionManager.isLoggedIn()) Routes.DASHBOARD else Routes.WELCOME

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin    = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {   // ✅ → Dashboard
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {   // ✅ → Dashboard
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        // ✅ Dashboard is now the landing screen after login
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                    }
                },
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddRecord    = { navController.navigate(Routes.ADD_RECORD) },
                onHistory      = { navController.navigate(Routes.HISTORY) },
                onProfile      = { navController.navigate(Routes.PROFILE) },
                onDashboard    = { navController.navigate(Routes.DASHBOARD) },
                onRecordClick  = { id -> navController.navigate(Routes.personRoute(id)) },
                onReviewRecord = { recordId, revieweeId ->
                    navController.navigate(Routes.reviewRoute(recordId, revieweeId))
                },
                onPayRecord = { recordId, amount, personName ->
                    navController.navigate(Routes.paymentRoute(recordId, amount, personName))
                },
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(Routes.ADD_RECORD) {
            AddRecordScreen(
                onNavigateBack = { navController.popBackStack() },
                navController  = navController
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                navController  = navController
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                navController  = navController
            )
        }

        composable(
            route     = Routes.PERSON,
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) {
            PersonScreen(
                onNavigateBack = { navController.popBackStack() },
                navController  = navController
            )
        }

        composable(
            route     = Routes.EDIT_RECORD,
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) {
            EditRecordScreen(
                onNavigateBack = { navController.popBackStack() },
                navController  = navController
            )
        }

        composable(
            route = Routes.REVIEW,
            arguments = listOf(
                navArgument("recordId")   { type = NavType.IntType },
                navArgument("revieweeId") { type = NavType.IntType }
            )
        ) {
            ReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(
            route = Routes.PAYMENT,
            arguments = listOf(
                navArgument("recordId")   { type = NavType.IntType },
                navArgument("amount")     { type = NavType.StringType },
                navArgument("personName") { type = NavType.StringType }
            )
        ) {
            PaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                onPaymentDone  = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
    }
}