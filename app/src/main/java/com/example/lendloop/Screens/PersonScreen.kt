package com.example.lendloop.Screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.Status
import com.example.lendloop.models.PersonViewModel
import com.example.lendloop.ui.components.BorrowCard
import com.example.lendloop.ui.components.EmptyState
import com.example.lendloop.util.toFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showReminderSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<BorrowRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.person?.name ?: "Person",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BalanceStat(
                        label = "They owe you",
                        value = uiState.totalLent,
                        color = MaterialTheme.colorScheme.primary
                    )
                    BalanceStat(
                        label = "You owe them",
                        value = uiState.totalBorrowed,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    BalanceStat(
                        label = "Net balance",
                        value = uiState.netBalance,
                        color = if (uiState.netBalance >= 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            val activeRecords = uiState.records.filter { it.status == Status.ACTIVE }
            val returnedRecords = uiState.records.filter { it.status == Status.RETURNED }

            if (uiState.records.isEmpty()) {
                EmptyState(message = "No records with this person yet.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeRecords.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(activeRecords, key = { it.id }) { record ->
                            BorrowCard(
                                record = record,
                                onClick = { },
                                onMarkReturned = { viewModel.markReturned(record.id) },
                                showRemindButton = record.direction == Direction.LENT,
                                onRemind = {
                                    selectedRecord = record
                                    showReminderSheet = true
                                    viewModel.updateLastReminded(record.id)
                                }
                            )
                        }
                    }

                    if (returnedRecords.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Returned",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(returnedRecords, key = { "r_${it.id}" }) { record ->
                            ReturnedRecordItem(record = record)
                        }
                    }
                }
            }
        }
    }

    if (showReminderSheet && selectedRecord != null) {
        val record = selectedRecord!!
        val reminderMessage = buildReminderMessage(
            personName = uiState.person?.name ?: "",
            itemName = record.itemName,
            lentAt = record.lentAt,
            amount = record.amount
        )

        ModalBottomSheet(
            onDismissRequest = { showReminderSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Send Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reminderMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, reminderMessage)
                        }
                        context.startActivity(intent)
                        showReminderSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send via WhatsApp / SMS")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BalanceStat(
    label: String,
    value: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (value > 0) "Ksh %.0f".format(value) else "—",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReturnedRecordItem(record: BorrowRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Returned ${record.returnedAt?.toFormattedDate() ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "✓ Done",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun buildReminderMessage(
    personName: String,
    itemName: String,
    lentAt: Long,
    amount: Double?
): String {
    val dateStr = lentAt.toFormattedDate()
    val amountStr = if (amount != null) " (Ksh %.0f)".format(amount) else ""
    return "Hey $personName! Just a friendly reminder — you still have my $itemName$amountStr that I lent you on $dateStr. Please let me know when you can return it 🙏"
}
