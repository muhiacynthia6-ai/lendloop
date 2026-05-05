package com.example.lendloop.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lendloop.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onPaymentDone: () -> Unit,
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.step.title(), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (uiState.step != PaymentStep.SUCCESS) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.step) {

                PaymentStep.CONFIRM -> ConfirmContent(
                    name = uiState.personName,
                    amount = uiState.amount,
                    onConfirm = viewModel::confirmCashPayment,
                    onCancel = onNavigateBack
                )

                PaymentStep.PROCESSING -> ProcessingContent()

                PaymentStep.SUCCESS -> SuccessContent(
                    amount = uiState.amount,
                    onDone = onPaymentDone
                )

                PaymentStep.FAILED -> FailedContent(
                    error = uiState.errorMessage,
                    onRetry = viewModel::retryPayment,
                    onCancel = onNavigateBack
                )
            }
        }
    }
}

fun PaymentStep.title(): String = when (this) {
    PaymentStep.CONFIRM -> "Confirm Payment"
    PaymentStep.PROCESSING -> "Processing"
    PaymentStep.SUCCESS -> "Payment Done"
    PaymentStep.FAILED -> "Payment Failed"
}

@Composable
fun ConfirmContent(
    name: String,
    amount: Double,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text("💵", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))

        Text("Pay $name", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Text("Ksh %.0f".format(amount), fontSize = 32.sp)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Cash Received")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun ProcessingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Recording payment...")
    }
}

@Composable
fun SuccessContent(amount: Double, onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Icon(Icons.Default.CheckCircle, null, tint = Color.Green)

        Spacer(Modifier.height(16.dp))

        Text("Payment Recorded!", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text("Ksh %.0f recorded successfully".format(amount))

        Spacer(Modifier.height(24.dp))

        Button(onClick = onDone) {
            Text("Done")
        }
    }
}

@Composable
fun FailedContent(
    error: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text("❌", fontSize = 64.sp)

        Spacer(Modifier.height(16.dp))

        Text("Payment Failed")

        Spacer(Modifier.height(8.dp))

        Text(error ?: "Something went wrong")

        Spacer(Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}