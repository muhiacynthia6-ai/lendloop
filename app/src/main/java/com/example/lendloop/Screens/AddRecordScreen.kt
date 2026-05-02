package com.example.lendloop.screens

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
import com.example.lendloop.models.AddRecordViewModel
import com.example.lendloop.models.AddRecordUiState
import com.example.lendloop.util.createImageUri
import com.example.lendloop.util.toFormattedDate
import java.util.*

private val CATEGORIES = listOf("Money", "Book", "Clothing", "Electronics", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: AddRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val canBorrow by produceState(initialValue = true) {
        value = viewModel.canUserBorrow()
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    // ── Activity Launchers ────────────────────────────────────────────────

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.onPhotoUriChange(it.toString()) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoUriChange(it.toString()) }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { contactUri ->
            queryContactInfo(context, contactUri) { name, phone ->
                viewModel.onPersonSelected(name, phone, contactUri.toString())
            }
        }
    }

    // ── UI Components ─────────────────────────────────────────────────────

    if (showPhotoDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoDialog = false },
            onTakePhoto = {
                showPhotoDialog = false
                createImageUri(context)?.let { uri ->
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                }
            },
            onPickFromGallery = {
                showPhotoDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Record", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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

            DirectionToggle(
                direction = uiState.direction,
                onDirectionChange = viewModel::onDirectionChange
            )

            if (uiState.direction == Direction.BORROWED && !canBorrow) {
                RestrictionWarning()
            }

            OutlinedTextField(
                value = uiState.itemName,
                onValueChange = viewModel::onItemNameChange,
                label = { Text("What is it?") },
                placeholder = { Text("e.g. Ksh 500, Blue hoodie, MacBook") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            PersonSection(
                name = uiState.personName,
                phone = uiState.personPhone,
                onNameChange = viewModel::onPersonNameChange,
                onPickContact = { contactPickerLauncher.launch(null) }
            )

            CategorySelector(
                selectedCategory = uiState.category,
                onCategoryChange = viewModel::onCategoryChange
            )

            if (uiState.category == "Electronics") {
                ElectronicsDetailsForm(uiState, viewModel)
            }

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

            PhotoSection(
                photoUri = uiState.photoUri,
                onAddPhoto = { showPhotoDialog = true },
                onRemovePhoto = { viewModel.onPhotoUriChange(null) }
            )

            DueDateSection(
                hasDueDate = uiState.hasDueDate,
                dueDate = uiState.dueDate,
                onToggle = viewModel::onDueDateToggle,
                onChange = viewModel::onDueDateChange
            )

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note — optional") },
                placeholder = { Text("Any extra details...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val isRestricted = uiState.direction == Direction.BORROWED && !canBorrow
            Button(
                onClick = viewModel::saveRecord,
                enabled = !uiState.isLoading && !isRestricted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRestricted) "Borrowing Restricted 🔒" else "Save Record",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Sub-Composables ───────────────────────────────────────────────────

@Composable
private fun DirectionToggle(
    direction: Direction,
    onDirectionChange: (Direction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("I...", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = direction == Direction.LENT,
                onClick = { onDirectionChange(Direction.LENT) },
                label = { Text("Lent something out") }
            )
            FilterChip(
                selected = direction == Direction.BORROWED,
                onClick = { onDirectionChange(Direction.BORROWED) },
                label = { Text("Borrowed something") }
            )
        }
    }
}

@Composable
private fun RestrictionWarning() {
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
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your return rate has dropped below 50%. You are restricted from borrowing items.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun PersonSection(
    name: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onPickContact: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Person", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Their name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = onPickContact) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Pick from contacts",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (phone.isNotBlank()) {
            Text(
                text = "📱 $phone",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CATEGORIES.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat) }
                )
            }
        }
    }
}

@Composable
private fun ElectronicsDetailsForm(
    uiState: AddRecordUiState,
    viewModel: AddRecordViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📱 Electronics Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = viewModel::onBrandChange,
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.model,
                onValueChange = viewModel::onModelChange,
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.serialNumber,
                onValueChange = viewModel::onSerialNumberChange,
                label = { Text("Serial Number (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text("Condition", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ItemCondition.entries.forEach { cond ->
                    FilterChip(
                        selected = uiState.condition == cond,
                        onClick = { viewModel.onConditionChange(cond) },
                        label = { Text(cond.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            OutlinedTextField(
                value = uiState.estimatedValue,
                onValueChange = viewModel::onEstimatedValueChange,
                label = { Text("Estimated Value (Ksh)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("Ksh ") }
            )
        }
    }
}

@Composable
private fun PhotoSection(
    photoUri: String?,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Photo", style = MaterialTheme.typography.labelLarge)
        if (photoUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Item photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAddPhoto() }
                )
                IconButton(
                    onClick = onRemovePhoto,
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
                onClick = onAddPhoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add a photo")
            }
        }
    }
}

@Composable
private fun DueDateSection(
    hasDueDate: Boolean,
    dueDate: Long?,
    onToggle: (Boolean) -> Unit,
    onChange: (Long) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Set a due date", style = MaterialTheme.typography.labelLarge)
            Switch(checked = hasDueDate, onCheckedChange = onToggle)
        }
        if (hasDueDate && dueDate != null) {
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            onChange(Calendar.getInstance().apply { set(y, m, d) }.timeInMillis)
                        },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Due: ${dueDate.toFormattedDate()}")
            }
        }
    }
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Photo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Take a photo")
                }
                OutlinedButton(onClick = onPickFromGallery, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Choose from gallery")
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
