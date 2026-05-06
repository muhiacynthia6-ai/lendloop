package com.example.lendloop.Screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lendloop.data.db.PaymentMethod
import com.example.lendloop.models.PaymentStep
import com.example.lendloop.models.PaymentUiState
import com.example.lendloop.models.PaymentViewModel

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
                title = { Text(uiState.step.title(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (uiState.step != PaymentStep.SUCCESS) {
                        IconButton(onClick = {
                            if (uiState.step == PaymentStep.CONFIRM)
                                viewModel.backToMethodSelect()
                            else
                                onNavigateBack()
                        }) {
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
                PaymentStep.SELECT_METHOD -> MethodSelectContent(
                    name   = uiState.personName,
                    amount = uiState.amount,
                    onSelect = viewModel::selectMethod
                )

                PaymentStep.CONFIRM -> ConfirmContent(
                    uiState      = uiState,
                    onPhoneChange = viewModel::updatePhone,
                    onEmailChange = viewModel::updatePaypalEmail,
                    onConfirm    = viewModel::confirmPayment,
                    onCancel     = viewModel::backToMethodSelect
                )

                PaymentStep.PROCESSING -> ProcessingContent()

                PaymentStep.SUCCESS -> SuccessContent(
                    uiState = uiState,
                    onDone  = onPaymentDone
                )

                PaymentStep.FAILED -> FailedContent(
                    error    = uiState.errorMessage,
                    onRetry  = viewModel::retryPayment,
                    onCancel = onNavigateBack
                )
            }
        }
    }
}

// ── Step title ──────────────────────────────────────────────────────────────
fun PaymentStep.title(): String = when (this) {
    PaymentStep.SELECT_METHOD -> "Pay"
    PaymentStep.CONFIRM       -> "Confirm Payment"
    PaymentStep.PROCESSING    -> "Processing…"
    PaymentStep.SUCCESS       -> "Payment Done"
    PaymentStep.FAILED        -> "Payment Failed"
}

// ── 1. Method Select ────────────────────────────────────────────────────────
@Composable
fun MethodSelectContent(
    name: String,
    amount: Double,
    onSelect: (PaymentMethod) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pay $name", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text("Ksh %.0f".format(amount), fontSize = 36.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        Text("Choose payment method", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        MethodCard(emoji = "📱", label = "M-Pesa", desc = "Pay via Safaricom STK Push") {
            onSelect(PaymentMethod.MPESA)
        }
        Spacer(Modifier.height(12.dp))
        MethodCard(emoji = "🅿", label = "PayPal", desc = "Send payment via PayPal") {
            onSelect(PaymentMethod.PAYPAL)
        }
        Spacer(Modifier.height(12.dp))
        MethodCard(emoji = "💵", label = "Cash", desc = "Record a cash payment") {
            onSelect(PaymentMethod.CASH)
        }
    }
}

@Composable
fun MethodCard(emoji: String, label: String, desc: String, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(desc, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── 2. Confirm ──────────────────────────────────────────────────────────────
@Composable
fun ConfirmContent(
    uiState: PaymentUiState,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Ksh %.0f".format(uiState.amount), fontSize = 36.sp,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text("to ${uiState.personName}", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        when (uiState.selectedMethod) {
            PaymentMethod.MPESA -> {
                Text("📱 M-Pesa", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = uiState.phoneNumber,
                    onValueChange = onPhoneChange,
                    label         = { Text("M-Pesa Phone Number") },
                    placeholder   = { Text("07XX XXX XXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    supportingText = { Text("Safaricom number to receive STK push") }
                )
            }

            PaymentMethod.PAYPAL -> {
                Text("🅿 PayPal", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = uiState.paypalEmail,
                    onValueChange = onEmailChange,
                    label         = { Text("PayPal Email Address") },
                    placeholder   = { Text("paypal@example.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    supportingText = { Text("Email of the PayPal account that sent the payment") }
                )
                Spacer(Modifier.height(8.dp))
                // Open PayPal app / browser for the payer
                TextButton(onClick = {
                    val paypalUrl = "https://www.paypal.com/paypalme/lendloop/${uiState.amount.toInt()}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paypalUrl))
                    context.startActivity(intent)
                }) {
                    Text("Open PayPal to request payment →")
                }
            }

            PaymentMethod.CASH -> {
                Text("💵 Cash", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("Record that cash has been received in person.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            null -> {}
        }

        // Error message
        uiState.errorMessage?.let { err ->
            Spacer(Modifier.height(12.dp))
            Text(err, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(28.dp))

        val confirmLabel = when (uiState.selectedMethod) {
            PaymentMethod.MPESA  -> "Send M-Pesa Request"
            PaymentMethod.PAYPAL -> "Confirm PayPal Received"
            PaymentMethod.CASH   -> "Confirm Cash Received"
            null                 -> "Confirm"
        }

        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text(confirmLabel)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

// ── 3. Processing ───────────────────────────────────────────────────────────
@Composable
fun ProcessingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Processing payment…")
    }
}

// ── 4. Success ──────────────────────────────────────────────────────────────
@Composable
fun SuccessContent(uiState: PaymentUiState, onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.CheckCircle, null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Payment Recorded!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))

        val detail = when (uiState.selectedMethod) {
            PaymentMethod.MPESA  -> uiState.mpesaMessage ?: "M-Pesa request sent. Check your phone."
            PaymentMethod.PAYPAL -> "PayPal payment of Ksh %.0f recorded.".format(uiState.amount)
            PaymentMethod.CASH   -> "Ksh %.0f cash recorded successfully.".format(uiState.amount)
            null                 -> "Ksh %.0f recorded.".format(uiState.amount)
        }
        Text(detail, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(28.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

// ── 5. Failed ───────────────────────────────────────────────────────────────
@Composable
fun FailedContent(error: String?, onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("❌", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Payment Failed", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(error ?: "Something went wrong",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(28.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Try Again") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}