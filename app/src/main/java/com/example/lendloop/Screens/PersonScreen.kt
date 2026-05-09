package com.example.lendloop.Screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.Status
import com.example.lendloop.models.PersonViewModel
import com.example.lendloop.ui.components.BorrowCard
import com.example.lendloop.ui.components.EmptyState
import com.example.lendloop.util.NotificationHelper
import com.example.lendloop.util.toFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    onNavigateBack: () -> Unit,
    onEditRecord:   (Int) -> Unit,
    navController:  NavController,
    viewModel:      PersonViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsState()
    val context           = LocalContext.current
    var showReminderSheet by remember { mutableStateOf(false) }
    var showThankYouSheet by remember { mutableStateOf(false) }
    var selectedRecord    by remember { mutableStateOf<BorrowRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.person?.name ?: "Person", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Balance summary card ───────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BalanceStat("They owe you",  uiState.totalLent,     MaterialTheme.colorScheme.primary)
                    BalanceStat("You owe them",  uiState.totalBorrowed, MaterialTheme.colorScheme.secondary)
                    BalanceStat("Net balance",   uiState.netBalance,
                        if (uiState.netBalance >= 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error)
                }
            }

            val activeRecords   = uiState.records.filter { it.status == Status.ACTIVE }
            val returnedRecords = uiState.records.filter { it.status == Status.RETURNED }

            if (uiState.records.isEmpty()) {
                EmptyState(message = "No records with this person yet.")
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeRecords.isNotEmpty()) {
                        item {
                            Text("Active", style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(activeRecords, key = { it.id }) { record ->
                            BorrowCard(
                                record         = record,
                                onClick        = {},
                                onMarkReturned = {
                                    // Borrower marks returned → show thank you sheet
                                    if (record.direction == Direction.BORROWED) {
                                        selectedRecord = record
                                        showThankYouSheet = true
                                    }
                                    viewModel.markReturned(record.id)
                                },
                                showRemindButton = record.direction == Direction.LENT,
                                onRemind = {
                                    selectedRecord = record
                                    showReminderSheet = true
                                    viewModel.updateLastReminded(record.id)
                                },
                                onEdit   = { onEditRecord(record.id) },
                                onDelete = { viewModel.deleteRecord(record) }
                            )
                        }
                    }

                    if (returnedRecords.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("Returned", style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(returnedRecords, key = { "r_${it.id}" }) { record ->
                            ReturnedRecordItem(record)
                        }
                    }
                }
            }
        }
    }

    // ── Lender reminder sheet ──────────────────────────────────────────────
    if (showReminderSheet && selectedRecord != null) {
        val record = selectedRecord!!
        val message = NotificationHelper.buildReturnReminderMessage(
            borrowerName = uiState.person?.name ?: "",
            itemName     = record.itemName,
            lentDate     = record.lentAt,
            amount       = record.amount
        )
        ModalBottomSheet(onDismissRequest = { showReminderSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Send Reminder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = {
                    context.startActivity(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message)
                    })
                    showReminderSheet = false
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp)); Text("Send via WhatsApp / SMS")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── Borrower thank you sheet ───────────────────────────────────────────
    if (showThankYouSheet && selectedRecord != null) {
        val record = selectedRecord!!
        val message = NotificationHelper.buildBorrowerThankYouMessage(
            borrowerName = uiState.person?.name ?: "Friend",
            itemName     = record.itemName,
            amount       = record.amount
        )
        ModalBottomSheet(onDismissRequest = { showThankYouSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Favorite, null, Modifier.size(48.dp), tint = Color(0xFFE91E63))
                Text("Say Thank You 💝", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(message, modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = {
                    context.startActivity(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message)
                    })
                    showThankYouSheet = false
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp)); Text("Send Thank You")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BalanceStat(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (value > 0) "Ksh %.0f".format(value) else "—",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ReturnedRecordItem(record: BorrowRecord) {
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(record.itemName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Returned ${record.returnedAt?.toFormattedDate() ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("✓ Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
