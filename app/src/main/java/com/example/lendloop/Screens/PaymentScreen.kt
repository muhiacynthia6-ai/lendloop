package com.example.lendloop.Screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lendloop.data.db.PaymentMethod
import com.example.lendloop.data.db.UserRole
import com.example.lendloop.models.PaymentStep
import com.example.lendloop.models.PaymentUiState
import com.example.lendloop.models.PaymentViewModel

private val CashGreen = Color(0xFF2E7D32)
private val CashGold  = Color(0xFFF9A825)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onPaymentDone:  () -> Unit,
    navController:  NavController,
    userRole:       UserRole = UserRole.LENDER,
    viewModel:      PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.step.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (uiState.step != PaymentStep.SUCCESS) {
                        IconButton(onClick = {
                            if (uiState.step == PaymentStep.CONFIRM) viewModel.backToMethodSelect()
                            else onNavigateBack()
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.step) {
                PaymentStep.SELECT_METHOD -> MethodSelectContent(
                    name     = uiState.personName,
                    amount   = uiState.amount,
                    userRole = userRole,
                    onSelect = { viewModel.selectMethod(PaymentMethod.CASH) }
                )
                PaymentStep.CONFIRM      -> ConfirmContent(
                    uiState  = uiState,
                    userRole = userRole,
                    onConfirm = viewModel::confirmPayment,
                    onCancel  = viewModel::backToMethodSelect
                )
                PaymentStep.PROCESSING   -> ProcessingContent()
                PaymentStep.SUCCESS      -> SuccessContent(
                    uiState  = uiState,
                    userRole = userRole,
                    onDone   = onPaymentDone
                )
                PaymentStep.FAILED       -> FailedContent(
                    error    = uiState.errorMessage,
                    onRetry  = viewModel::retryPayment,
                    onCancel = onNavigateBack
                )
            }
        }
    }
}

private val PaymentStep.title: String get() = when (this) {
    PaymentStep.SELECT_METHOD -> "Record Payment"
    PaymentStep.CONFIRM       -> "Confirm Cash"
    PaymentStep.PROCESSING    -> "Processing..."
    PaymentStep.SUCCESS       -> "Transaction Complete"
    PaymentStep.FAILED        -> "Payment Failed"
}

// ═══════════════════════════════════════════════════════════════════════════════
//  METHOD SELECT — Cash only
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MethodSelectContent(
    name:     String,
    amount:   Double,
    userRole: UserRole,
    onSelect: () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = if (userRole == UserRole.LENDER) "Receive Cash Payment" else "Make Cash Payment",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "Ksh ${amount.toInt()}",
            fontSize   = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = if (userRole == UserRole.LENDER) "from $name" else "to $name",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(36.dp))

        // Cash-only card
        OutlinedCard(
            onClick  = onSelect,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            border   = BorderStroke(1.5.dp, CashGold.copy(alpha = 0.5f))
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier.size(52.dp).clip(CircleShape)
                        .background(CashGold.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Text("💵", fontSize = 26.sp) }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cash Payment", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (userRole == UserRole.LENDER) "Confirm cash received from $name"
                        else "Confirm cash handed to $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Warning note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text      = "⚠️ Cash payments cannot be reversed. Verify the amount before confirming.",
                modifier  = Modifier.padding(14.dp),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  CONFIRM
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ConfirmContent(
    uiState:  PaymentUiState,
    userRole: UserRole,
    onConfirm: () -> Unit,
    onCancel:  () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💵", fontSize = 60.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text       = "Ksh ${uiState.amount.toInt()}",
            fontSize   = 38.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = if (userRole == UserRole.LENDER) "received from ${uiState.personName}"
            else "to be paid to ${uiState.personName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text      = if (userRole == UserRole.LENDER)
                "You are confirming that you have received Ksh ${uiState.amount.toInt()} in cash from ${uiState.personName}."
            else
                "You are confirming that you have handed Ksh ${uiState.amount.toInt()} in cash to ${uiState.personName}.",
            textAlign = TextAlign.Center,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        uiState.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick  = onConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = CashGreen)
        ) {
            Text(
                text       = if (userRole == UserRole.LENDER) "✅ I Have Received Cash"
                else "✅ I Have Paid Cash",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick  = onCancel,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(14.dp)
        ) { Text("Back") }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  PROCESSING
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProcessingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = CashGreen, strokeWidth = 3.dp)
        Spacer(Modifier.height(16.dp))
        Text("Recording cash payment...", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  SUCCESS — "Payment Complete" with animated checkmark
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SuccessContent(
    uiState:  PaymentUiState,
    userRole: UserRole,
    onDone:   () -> Unit
) {
    // Animate checkmark scale on entry
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue  = if (visible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label        = "checkScale"
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated green circle + checkmark
        Box(
            modifier         = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(CashGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint               = CashGreen,
                modifier           = Modifier.size(72.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── "Payment Complete" — the main text the user asked for ──────────
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
        ) {
            Text(
                text       = "Transaction Complete! 🎉",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = CashGreen
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text      = "Ksh ${uiState.amount.toInt()}",
            fontSize  = 36.sp,
            fontWeight = FontWeight.Bold,
            color     = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = if (userRole == UserRole.LENDER)
                "Cash of Ksh ${uiState.amount.toInt()} received from ${uiState.personName}. Transaction is complete. ✓"
            else
                "Your payment of Ksh ${uiState.amount.toInt()} to ${uiState.personName} is complete. ✓",
            textAlign = TextAlign.Center,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(36.dp))

        Button(
            onClick  = onDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = CashGreen)
        ) {
            Text("Done", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  FAILED
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FailedContent(error: String?, onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("❌", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Payment Failed", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            text      = error ?: "Something went wrong",
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.error,
            style     = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(30.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("Try Again")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("Cancel")
        }
    }
}