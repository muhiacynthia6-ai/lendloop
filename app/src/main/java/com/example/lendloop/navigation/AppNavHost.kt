package com.example.lendloop.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lendloop.util.SessionManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors
import com.example.lendloop.di.AppEntryPoint

@Composable
fun AppNavHost() {
    val context = LocalContext.current

    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java
        ).sessionManager()
    }

    LendLoopNavGraph(sessionManager = sessionManager)
}