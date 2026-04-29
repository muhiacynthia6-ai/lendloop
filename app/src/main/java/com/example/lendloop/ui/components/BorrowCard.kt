package com.example.lendloop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.util.daysAgo
import com.example.lendloop.util.isOverdue
import com.example.lendloop.util.toFormattedDate

@Composable
fun BorrowCard(
    record: BorrowRecord,
    onClick: () -> Unit,
    onMarkReturned: () -> Unit,
    onReview: (() -> Unit)? = null,
    showRemindButton: Boolean = false,
    onRemind: () -> Unit = {}
) {
    val isOverdue = record.dueDate?.isOverdue() == true
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Top row: item name + amount ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.itemName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.personName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (record.amount != null) {
                    Text(
                        text = "Ksh %.0f".format(record.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Bottom row: dates + action buttons ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.lentAt.daysAgo(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.dueDate != null) {
                        Text(
                            text = if (isOverdue)
                                "⚠ Overdue · ${record.dueDate.toFormattedDate()}"
                            else
                                "Due ${record.dueDate.toFormattedDate()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    if (showRemindButton) {
                        IconButton(onClick = onRemind) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Send reminder",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Mark returned",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // ── Confirm return dialog ─────────────────────────────────────────────
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Mark as returned?") },
            text = { Text("This will move \"${record.itemName}\" to your history.") },
            confirmButton = {
                TextButton(onClick = {
                    onMarkReturned()
                    showConfirm = false
                    onReview?.invoke() // ← triggers review screen after confirming
                }) {
                    Text("Yes, returned")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}