package com.example.lendloop.Screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lendloop.data.db.PaymentMethod
import com.example.lendloop.models.PaymentStep
import com.example.lendloop.models.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onPaymentDone: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.step) {
                            PaymentStep.SELECT_METHOD -> "Pay Now"
                            PaymentStep.MPESA_FORM    -> "M-Pesa"
                            PaymentStep.PROCESSING    -> "Processing"
                            PaymentStep.SUCCESS       -> "Success"
                            PaymentStep.FAILED        -> "Failed"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (uiState.step != PaymentStep.SUCCESS) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->

        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "payment_step",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { step ->
            when (step) {

                // ── Step 1: Choose method ─────────────────────────────────
                PaymentStep.SELECT_METHOD -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "💳", fontSize = 56.sp)

                        Text(
                            text = "Pay ${viewModel.getPersonName()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        // Amount display
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ksh %.0f".format(viewModel.getAmount()),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Select payment method",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // ── M-Pesa ────────────────────────────────────────
                        PaymentMethodCard(
                            emoji = "📱",
                            title = "M-Pesa",
                            subtitle = "Send STK push to your phone",
                            badgeText = "Recommended",
                            badgeColor = Color(0xFF4CAF50),
                            color = Color(0xFF4CAF50),
                            onClick = {
                                viewModel.onMethodSelected(PaymentMethod.MPESA)
                            }
                        )

                        // ── PayPal ────────────────────────────────────────
                        PaymentMethodCard(
                            emoji = "🌐",
                            title = "PayPal",
                            subtitle = "Opens PayPal.me in your browser",
                            badgeText = "International",
                            badgeColor = Color(0xFF003087),
                            color = Color(0xFF003087),
                            onClick = {
                                viewModel.onMethodSelected(PaymentMethod.PAYPAL)
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            "https://www.paypal.me/pay/${viewModel.getAmount().toInt()}"
                                        )
                                    )
                                )
                            }
                        )

                        // ── Cash ──────────────────────────────────────────
                        PaymentMethodCard(
                            emoji = "💵",
                            title = "Physical / Cash",
                            subtitle = "Mark as settled in person",
                            badgeText = "Instant",
                            badgeColor = Color(0xFFFF9800),
                            color = Color(0xFFFF9800),
                            onClick = {
                                viewModel.onMethodSelected(PaymentMethod.CASH)
                                viewModel.confirmCashPayment()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ── Step 2: M-Pesa phone entry ────────────────────────────
                PaymentStep.MPESA_FORM -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "📱", fontSize = 56.sp)

                        Text(
                            text = "M-Pesa Payment",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Enter the M-Pesa number to send\nthe STK push prompt to.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = viewModel::onPhoneNumberChange,
                            label = { Text("Phone number") },
                            placeholder = { Text("e.g. 0712 345 678") },
                            prefix = { Text("+254  ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            )
                        )

                        // Amount summary
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Amount to pay",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Ksh %.0f".format(uiState.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = viewModel::initiateMpesaPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = "Send M-Pesa Request",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        TextButton(
                            onClick = { viewModel.retryPayment() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("← Choose a different method")
                        }
                    }
                }

                // ── Step 3: Processing ────────────────────────────────────
                PaymentStep.PROCESSING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            strokeWidth = 6.dp,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Sending M-Pesa request...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A prompt will appear on your phone.\nEnter your M-Pesa PIN to confirm.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── Step 4: Success ───────────────────────────────────────
                PaymentStep.SUCCESS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Sent!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (uiState.selectedMethod) {
                                PaymentMethod.MPESA ->
                                    "Check your phone and enter your\n" +
                                            "M-Pesa PIN to complete the payment."
                                PaymentMethod.PAYPAL ->
                                    "You were redirected to PayPal.\n" +
                                            "Complete the payment there."
                                PaymentMethod.CASH ->
                                    "Cash payment recorded.\n" +
                                            "This record has been marked as paid."
                                null -> "Payment has been recorded."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        if (uiState.mpesaRef.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = "Ref: ${uiState.mpesaRef}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onPaymentDone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Done", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }

                // ── Step 5: Failed ────────────────────────────────────────
                PaymentStep.FAILED -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "❌", fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Failed",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage
                                ?: "Something went wrong. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = viewModel::retryPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Try Again", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable payment method card ──────────────────────────────────────────────

@Composable
fun PaymentMethodCard(
    emoji: String,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji badge
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 26.sp)
                }
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
