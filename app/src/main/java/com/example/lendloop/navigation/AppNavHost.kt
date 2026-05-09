package com.example.lendloop.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.lendloop.di.AppEntryPoint
import com.example.lendloop.util.SessionManager
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java
        ).sessionManager()
    }
    val navController = rememberNavController()
    LendLoopNavGraph(
        sessionManager = sessionManager,
        navController  = navController
    )
}