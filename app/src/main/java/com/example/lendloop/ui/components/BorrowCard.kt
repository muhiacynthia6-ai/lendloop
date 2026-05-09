package com.example.lendloop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    record:           BorrowRecord,
    onClick:          () -> Unit,
    onMarkReturned:   () -> Unit,
    onReview:         (() -> Unit)? = null,
    showRemindButton: Boolean       = false,
    onRemind:         () -> Unit    = {},
    onEdit:           (() -> Unit)? = null,
    onDelete:         (() -> Unit)? = null,
    onPay:            (() -> Unit)? = null
) {
    val isOverdue    = record.dueDate?.isOverdue() == true
    var showConfirm  by remember { mutableStateOf(false) }
    var showDelete   by remember { mutableStateOf(false) }

    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Top row: item name + amount ───────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.itemName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(record.personName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (record.amount != null) {
                    Text("Ksh %.0f".format(record.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Bottom row: dates + action buttons ────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(record.lentAt.daysAgo(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (record.dueDate != null) {
                        Text(
                            text  = if (isOverdue)
                                "⚠ Overdue · ${record.dueDate.toFormattedDate()}"
                            else
                                "Due ${record.dueDate.toFormattedDate()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pay button
                    if (onPay != null) {
                        IconButton(onClick = onPay) {
                            Icon(Icons.Default.Payments, "Pay",
                                tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    // Remind button
                    if (showRemindButton) {
                        IconButton(onClick = onRemind) {
                            Icon(Icons.Default.Notifications, "Send reminder",
                                tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    // Edit button
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, "Edit record",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    // Delete button
                    if (onDelete != null) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Default.Delete, "Delete record",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    // Mark returned button
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Default.CheckCircle, "Mark returned",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // ── Confirm return dialog ──────────────────────────────────────────────
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Mark as returned?") },
            text  = { Text("This will move \"${record.itemName}\" to your history.") },
            confirmButton = {
                TextButton(onClick = {
                    onMarkReturned()
                    showConfirm = false
                    onReview?.invoke()
                }) { Text("Yes, returned") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // ── Confirm delete dialog ──────────────────────────────────────────────
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete record?") },
            text  = { Text("\"${record.itemName}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete?.invoke()
                    showDelete = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}