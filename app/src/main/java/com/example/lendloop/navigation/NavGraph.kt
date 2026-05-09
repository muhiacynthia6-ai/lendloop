package com.example.lendloop.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lendloop.Screens.*
import com.example.lendloop.data.db.UserRole
import com.example.lendloop.util.SessionManager

val DoubleType: NavType<Double> = object : NavType<Double>(false) {
    override fun get(bundle: Bundle, key: String): Double = bundle.getDouble(key)
    override fun parseValue(value: String): Double = value.toDouble()
    override fun put(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

@Composable
fun LendLoopNavGraph(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {

    val startDestination =
        if (sessionManager.isLoggedIn()) {
            Routes.DASHBOARD
        } else {
            Routes.WELCOME
        }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) }
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
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister      = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess            = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToHome = { navController.navigate(Routes.HOME) },
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
                onAddRecord = { navController.navigate(Routes.ADD_RECORD) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onProfile = { navController.navigate(Routes.PROFILE) },
                onDashboard = { navController.navigate(Routes.DASHBOARD) },
                onRecordClick = { personId ->
                    navController.navigate(Routes.personRoute(personId))
                },
                onReviewRecord = { recordId, revieweeId ->
                    navController.navigate(Routes.reviewRoute(recordId, revieweeId))
                },
                onPayRecord = { recordId, amount, personName ->
                    val userRole = getCurrentUserRole(navController)
                    navController.navigate(
                        Routes.paymentRoute(recordId, amount, personName, userRole)
                    )
                },
                onEditRecord = { recordId ->
                    navController.navigate(Routes.editRecordRoute(recordId))
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
                navController = navController
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PERSON,
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) {
            PersonScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditRecord = { recordId ->
                    navController.navigate(Routes.editRecordRoute(recordId))
                },
                navController = navController
            )
        }

        composable(
            route = Routes.EDIT_RECORD,
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) {
            EditRecordScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(
            route = Routes.REVIEW,
            arguments = listOf(
                navArgument("recordId") { type = NavType.IntType },
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
                navArgument("recordId") { type = NavType.IntType },
                navArgument("amount") { type = DoubleType },
                navArgument("personName") { type = NavType.StringType },
                navArgument("userRole") {
                    type = NavType.StringType
                    defaultValue = UserRole.BORROWER.name
                }
            )
        ) { backStackEntry ->
            val userRole = UserRole.valueOf(
                backStackEntry.arguments?.getString("userRole") ?: UserRole.BORROWER.name
            )

            PaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                onPaymentDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                navController = navController,
                userRole = userRole
            )
        }
    }
}

private fun getCurrentUserRole(navController: NavHostController): UserRole {
    val currentRoute = navController.currentBackStackEntry?.destination?.route?.lowercase()
    return when {
        currentRoute?.contains("lender") == true -> UserRole.LENDER
        else -> UserRole.BORROWER
    }
}
