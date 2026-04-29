package com.example.lendloop.Screens

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lendloop.data.db.Direction
import com.example.lendloop.models.AddRecordViewModel
import com.example.lendloop.util.toFormattedDate
import java.util.*

val categories = listOf("Money", "Book", "Clothing", "Electronics", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val canBorrow by produceState(initialValue = true) {
        value = viewModel.canUserBorrow()
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val name = c.getString(
                        c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME).coerceAtLeast(0)
                    ) ?: ""
                    val contactId = c.getString(
                        c.getColumnIndex(ContactsContract.Contacts._ID).coerceAtLeast(0)
                    ) ?: ""
                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    var phone = ""
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            phone = pc.getString(
                                pc.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                ).coerceAtLeast(0)
                            ) ?: ""
                        }
                    }
                    viewModel.onPersonSelected(name, phone, uri.toString())
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Record", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Direction toggle ──────────────────────────────────────────
            Text("I...", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.direction == Direction.LENT,
                    onClick = { viewModel.onDirectionChange(Direction.LENT) },
                    label = { Text("Lent something out") }
                )
                FilterChip(
                    selected = uiState.direction == Direction.BORROWED,
                    onClick = { viewModel.onDirectionChange(Direction.BORROWED) },
                    label = { Text("Borrowed something") }
                )
            }

            // ── Restriction warning ───────────────────────────────────────
            if (uiState.direction == Direction.BORROWED && !canBorrow) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🔒 Borrowing Restricted",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your return rate has dropped below 50%. " +
                                    "You are restricted from borrowing for 30 days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // ── Item name ─────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.itemName,
                onValueChange = viewModel::onItemNameChange,
                label = { Text("What is it?") },
                placeholder = { Text("e.g. Ksh 500, Blue hoodie, Charger") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ── Person ────────────────────────────────────────────────────
            Text("Person", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.personName,
                    onValueChange = viewModel::onPersonNameChange,
                    label = { Text("Their name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Pick from contacts",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (uiState.personPhone.isNotBlank()) {
                Text(
                    text = "📱 ${uiState.personPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Category ──────────────────────────────────────────────────
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = uiState.category == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            // ── Amount ────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (Ksh) — optional") },
                placeholder = { Text("e.g. 500") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("Ksh ") }
            )

            // ── Due date ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set a due date", style = MaterialTheme.typography.labelLarge)
                Switch(
                    checked = uiState.hasDueDate,
                    onCheckedChange = viewModel::onDueDateToggle
                )
            }
            if (uiState.hasDueDate && uiState.dueDate != null) {
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = uiState.dueDate!!
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selected = Calendar.getInstance().apply {
                                    set(year, month, day, 0, 0, 0)
                                }.timeInMillis
                                viewModel.onDueDateChange(selected)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Due: ${uiState.dueDate!!.toFormattedDate()}")
                }
            }

            // ── Note ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note — optional") },
                placeholder = { Text("Any extra details...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // ── Error message ─────────────────────────────────────────────
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── Save button ───────────────────────────────────────────────
            val isRestricted = uiState.direction == Direction.BORROWED && !canBorrow
            Button(
                onClick = viewModel::saveRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading && !isRestricted
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isRestricted) "Borrowing Restricted 🔒" else "Save Record",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}