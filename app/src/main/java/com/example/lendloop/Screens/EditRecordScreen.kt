package com.example.lendloop.Screens

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.ItemCondition
import com.example.lendloop.models.EditRecordViewModel
import com.example.lendloop.util.createImageUri
import com.example.lendloop.util.toFormattedDate
import java.util.*

private val editCategories = listOf("Money", "Electronics", "Books", "Clothes", "Tools", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: EditRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var cameraUri       by remember { mutableStateOf<Uri?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    // ── Camera launcher ───────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.onPhotoUriChange(it.toString()) }
        }
    }

    // ── Gallery launcher ──────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoUriChange(it.toString()) }
    }

    // ── Contacts launcher ─────────────────────────────────────────────────
    val contactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            queryContactInfo(context, it) { name, phone ->
                viewModel.onPersonSelected(name, phone, it.toString())
            }
        }
    }

    // ── Photo picker dialog ───────────────────────────────────────────────
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Change Photo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showPhotoDialog = false
                            createImageUri(context)?.let { uri ->
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Take a photo")
                    }
                    OutlinedButton(
                        onClick = {
                            showPhotoDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Record", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick  = viewModel::saveChanges,
                        enabled  = !uiState.isLoading
                    ) {
                        Text(
                            text       = "Save",
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
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
            Spacer(Modifier.height(4.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // ── Direction toggle ──────────────────────────────────────────
            Text("I...", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.direction == Direction.LENT,
                    onClick  = { viewModel.onDirectionChange(Direction.LENT) },
                    label    = { Text("Lent something out") }
                )
                FilterChip(
                    selected = uiState.direction == Direction.BORROWED,
                    onClick  = { viewModel.onDirectionChange(Direction.BORROWED) },
                    label    = { Text("Borrowed something") }
                )
            }

            // ── Item name ─────────────────────────────────────────────────
            OutlinedTextField(
                value          = uiState.itemName,
                onValueChange  = viewModel::onItemNameChange,
                label          = { Text("What is it?") },
                placeholder    = { Text("e.g. Ksh 500, Blue hoodie, MacBook") },
                modifier       = Modifier.fillMaxWidth(),
                singleLine     = true,
                isError        = uiState.error != null && uiState.itemName.isBlank(),
                supportingText = {
                    if (uiState.error != null && uiState.itemName.isBlank())
                        Text("Required", color = MaterialTheme.colorScheme.error)
                }
            )

            // ── Person ────────────────────────────────────────────────────
            Text("Person", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = uiState.personName,
                    onValueChange = viewModel::onPersonNameChange,
                    label         = { Text("Their name") },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    isError       = uiState.error != null && uiState.personName.isBlank()
                )
                IconButton(onClick = { contactLauncher.launch(null) }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Pick from contacts",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (uiState.personPhone.isNotBlank()) {
                Text(
                    text  = "📱 ${uiState.personPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Category ──────────────────────────────────────────────────
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                editCategories.forEach { cat ->
                    FilterChip(
                        selected = uiState.category == cat,
                        onClick  = { viewModel.onCategoryChange(cat) },
                        label    = { Text(cat) }
                    )
                }
            }

            // ── Electronics details ───────────────────────────────────────
            if (uiState.category == "Electronics") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text       = "📱 Electronics Details",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        OutlinedTextField(
                            value         = uiState.brand,
                            onValueChange = viewModel::onBrandChange,
                            label         = { Text("Brand") },
                            placeholder   = { Text("e.g. Apple, Samsung, HP") },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true
                        )
                        OutlinedTextField(
                            value         = uiState.model,
                            onValueChange = viewModel::onModelChange,
                            label         = { Text("Model") },
                            placeholder   = { Text("e.g. iPhone 14, Galaxy S23") },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true
                        )
                        OutlinedTextField(
                            value         = uiState.serialNumber,
                            onValueChange = viewModel::onSerialNumberChange,
                            label         = { Text("Serial Number (optional)") },
                            placeholder   = { Text("e.g. C02XG0XLJGH5") },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true
                        )
                        Text(
                            text  = "Condition",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ItemCondition.entries.forEach { condition ->
                                FilterChip(
                                    selected = uiState.condition == condition,
                                    onClick  = { viewModel.onConditionChange(condition) },
                                    label    = {
                                        Text(
                                            when (condition) {
                                                ItemCondition.NEW  -> "New"
                                                ItemCondition.GOOD -> "Good"
                                                ItemCondition.FAIR -> "Fair"
                                                ItemCondition.POOR -> "Poor"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                        OutlinedTextField(
                            value           = uiState.estimatedValue,
                            onValueChange   = viewModel::onEstimatedValueChange,
                            label           = { Text("Estimated Value (Ksh) — optional") },
                            modifier        = Modifier.fillMaxWidth(),
                            singleLine      = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix          = { Text("Ksh ") }
                        )
                    }
                }
            }

            // ── Amount ────────────────────────────────────────────────────
            OutlinedTextField(
                value           = uiState.amount,
                onValueChange   = viewModel::onAmountChange,
                label           = { Text("Amount (Ksh) — optional") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix          = { Text("Ksh ") }
            )

            // ── Photo ─────────────────────────────────────────────────────
            Text("Photo", style = MaterialTheme.typography.labelLarge)
            if (uiState.photoUri != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model              = uiState.photoUri,
                        contentDescription = "Item photo",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showPhotoDialog = true }
                    )
                    IconButton(
                        onClick  = { viewModel.onPhotoUriChange(null) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Remove",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick  = { showPhotoDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add a photo")
                }
            }

            // ── Due date ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Set a due date", style = MaterialTheme.typography.labelLarge)
                Switch(
                    checked         = uiState.hasDueDate,
                    onCheckedChange = viewModel::onDueDateToggle
                )
            }
            if (uiState.hasDueDate && uiState.dueDate != null) {
                OutlinedButton(
                    onClick  = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = uiState.dueDate!!
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.onDueDateChange(
                                    Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                    }.timeInMillis
                                )
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
                value         = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label         = { Text("Note — optional") },
                placeholder   = { Text("Any extra details...") },
                modifier      = Modifier.fillMaxWidth(),
                minLines      = 2
            )

            // ── Error ─────────────────────────────────────────────────────
            if (uiState.error != null) {
                Text(
                    text  = "⚠️ ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── Save button ───────────────────────────────────────────────
            Button(
                onClick  = viewModel::saveChanges,
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = "Save Changes",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun queryContactInfo(
    context: android.content.Context,
    uri: Uri,
    onResult: (String, String) -> Unit
) {
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val nameCol = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val idCol = c.getColumnIndex(ContactsContract.Contacts._ID)
            
            val name = if (nameCol >= 0) c.getString(nameCol) ?: "" else ""
            val contactId = if (idCol >= 0) c.getString(idCol) ?: "" else ""
            
            var phone = ""
            if (contactId.isNotEmpty()) {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { pc ->
                    if (pc.moveToFirst()) {
                        val phoneCol = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        phone = if (phoneCol >= 0) pc.getString(phoneCol) ?: "" else ""
                    }
                }
            }
            onResult(name, phone)
        }
    }
}
