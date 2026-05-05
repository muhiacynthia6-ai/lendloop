package com.example.lendloop.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lendloop.models.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    var phone      by remember { mutableStateOf("") }
    var pin        by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔄", fontSize = 56.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text       = "LendLoop",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            text      = "Track what you lend and borrow",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text       = "Welcome back",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.fillMaxWidth()
        )
        Text(
            text     = "Log in to your account",
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value           = phone,
            onValueChange   = { phone = it; viewModel.clearError() },
            label           = { Text("Phone number") },
            leadingIcon     = { Icon(Icons.Default.Phone, null) },
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value         = pin,
            onValueChange = {
                if (it.length <= 6) { pin = it; viewModel.clearError() }
            },
            label        = { Text("PIN") },
            leadingIcon  = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { pinVisible = !pinVisible }) {
                    Icon(
                        imageVector        = if (pinVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = if (pinVisible) "Hide PIN" else "Show PIN"
                    )
                }
            },
            visualTransformation = if (pinVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text     = uiState.error!!,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick  = { viewModel.login(phone, pin) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color    = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In", style = MaterialTheme.typography.titleSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Register", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}