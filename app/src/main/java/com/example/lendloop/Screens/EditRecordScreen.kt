package com.example.lendloop.Screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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

private val editCategories = listOf(
    "Money",
    "Electronics",
    "Books",
    "Clothes",
    "Tools",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: EditRecordViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    var showPhotoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNotifySheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    // CAMERA
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {
                cameraUri?.let {
                    viewModel.onPhotoUriChange(it.toString())
                }
            }
        }

    // GALLERY
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {
                viewModel.onPhotoUriChange(it.toString())
            }
        }

    // CONTACTS
    val contactLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickContact()
        ) { uri ->

            uri?.let {
                queryContactInfo(context, uri) { name, phone ->
                    viewModel.onPersonSelected(
                        name,
                        phone,
                        uri.toString()
                    )
                }
            }
        }

    // PHOTO DIALOG
    if (showPhotoDialog) {

        AlertDialog(
            onDismissRequest = {
                showPhotoDialog = false
            },

            title = {
                Text("Change Photo")
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

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

                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Take Photo")
                    }

                    OutlinedButton(
                        onClick = {

                            showPhotoDialog = false
                            galleryLauncher.launch("image/*")

                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            Icons.Default.Image,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Choose from Gallery")
                    }
                }
            },

            confirmButton = {},

            dismissButton = {

                TextButton(
                    onClick = {
                        showPhotoDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // DELETE DIALOG
    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Delete Record?")
            },

            text = {
                Text(
                    "\"${uiState.itemName}\" will be permanently deleted."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showDeleteDialog = false
                        viewModel.deleteRecord()

                    }
                ) {

                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // NOTIFY SHEET
    if (showNotifySheet) {

        val isLender = uiState.direction == Direction.LENT

        val notifyMessage = if (isLender) {

            "Hi ${uiState.personName}! I've updated the record for " +
                    "\"${uiState.itemName}\". Please check your LendLoop 📋"

        } else {

            NotificationHelper.buildBorrowerThankYouMessage(
                borrowerName = uiState.personName,
                itemName = uiState.itemName,
                amount = uiState.amount.toDoubleOrNull()
            )
        }

        ModalBottomSheet(
            onDismissRequest = {
                showNotifySheet = false
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = if (isLender)
                        "Notify Borrower 📋"
                    else
                        "Thank the Lender 💝",

                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = notifyMessage,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Button(
                    onClick = {

                        val intent = Intent(Intent.ACTION_SEND).apply {

                            type = "text/plain"

                            putExtra(
                                Intent.EXTRA_TEXT,
                                notifyMessage
                            )
                        }

                        context.startActivity(intent)

                        showNotifySheet = false
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Send")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Edit Record",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onNavigateBack
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            showNotifySheet = true
                        }
                    ) {

                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notify"
                        )
                    }

                    IconButton(
                        onClick = {
                            showDeleteDialog = true
                        }
                    ) {

                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    TextButton(
                        onClick = viewModel::saveChanges,
                        enabled = !uiState.isLoading
                    ) {

                        Text(
                            "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp),

            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {

                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // DIRECTION
            item {

                Text(
                    "I...",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected = uiState.direction == Direction.LENT,

                        onClick = {
                            viewModel.onDirectionChange(Direction.LENT)
                        },

                        label = {
                            Text("Lent Something")
                        }
                    )

                    FilterChip(
                        selected = uiState.direction == Direction.BORROWED,

                        onClick = {
                            viewModel.onDirectionChange(Direction.BORROWED)
                        },

                        label = {
                            Text("Borrowed Something")
                        }
                    )
                }
            }

            // ITEM NAME
            item {

                OutlinedTextField(
                    value = uiState.itemName,

                    onValueChange = viewModel::onItemNameChange,

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("What is it?")
                    },

                    placeholder = {
                        Text("e.g. Ksh 500, Laptop")
                    },

                    singleLine = true
                )
            }

            // PERSON
            item {

                Text(
                    "Person",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = uiState.personName,

                        onValueChange = viewModel::onPersonNameChange,

                        modifier = Modifier.weight(1f),

                        label = {
                            Text("Their Name")
                        },

                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            contactLauncher.launch(null)
                        }
                    ) {

                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Pick Contact"
                        )
                    }
                }

                if (uiState.personPhone.isNotBlank()) {

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "📱 ${uiState.personPhone}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // CATEGORY
            item {

                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    editCategories.forEach { category ->

                        FilterChip(
                            selected = uiState.category == category,

                            onClick = {
                                viewModel.onCategoryChange(category)
                            },

                            label = {
                                Text(category)
                            }
                        )
                    }
                }
            }

            // ELECTRONICS
            if (uiState.category == "Electronics") {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Text(
                                "Electronics Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = uiState.brand,
                                onValueChange = viewModel::onBrandChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Brand") }
                            )

                            OutlinedTextField(
                                value = uiState.model,
                                onValueChange = viewModel::onModelChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Model") }
                            )

                            OutlinedTextField(
                                value = uiState.serialNumber,
                                onValueChange = viewModel::onSerialNumberChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Serial Number") }
                            )

                            Text(
                                "Condition",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                ItemCondition.entries.forEach { condition ->

                                    FilterChip(
                                        selected = uiState.condition == condition,

                                        onClick = {
                                            viewModel.onConditionChange(condition)
                                        },

                                        label = {
                                            Text(condition.name)
                                        }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = uiState.estimatedValue,

                                onValueChange = viewModel::onEstimatedValueChange,

                                modifier = Modifier.fillMaxWidth(),

                                label = {
                                    Text("Estimated Value")
                                },

                                prefix = {
                                    Text("Ksh ")
                                },

                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                        }
                    }
                }
            }

            // AMOUNT
            item {

                OutlinedTextField(
                    value = uiState.amount,

                    onValueChange = viewModel::onAmountChange,

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("Amount")
                    },

                    prefix = {
                        Text("Ksh ")
                    },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }

            // PHOTO
            item {

                Text(
                    "Photo",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.photoUri != null) {

                    Box {

                        AsyncImage(
                            model = uiState.photoUri,
                            contentDescription = null,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showPhotoDialog = true
                                },

                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = {
                                viewModel.onPhotoUriChange(null)
                            },

                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {

                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove Photo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                } else {

                    OutlinedButton(
                        onClick = {
                            showPhotoDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Add Photo")
                    }
                }
            }

            // DUE DATE
            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Set Due Date")

                    Switch(
                        checked = uiState.hasDueDate,

                        onCheckedChange = viewModel::onDueDateToggle
                    )
                }

                if (uiState.hasDueDate && uiState.dueDate != null) {

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {

                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = uiState.dueDate!!
                            }

                            DatePickerDialog(
                                context,

                                { _, year, month, day ->

                                    val millis =
                                        Calendar.getInstance().apply {

                                            set(
                                                year,
                                                month,
                                                day,
                                                0,
                                                0,
                                                0
                                            )

                                        }.timeInMillis

                                    viewModel.onDueDateChange(millis)

                                },

                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)

                            ).show()
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            "Due: ${uiState.dueDate!!.toFormattedDate()}"
                        )
                    }
                }
            }

            // NOTE
            item {

                OutlinedTextField(
                    value = uiState.note,

                    onValueChange = viewModel::onNoteChange,

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("Notes")
                    },

                    minLines = 3
                )
            }

            // ERROR
            if (uiState.error != null) {

                item {

                    Text(
                        "⚠️ ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // SAVE BUTTON
            item {

                Button(
                    onClick = viewModel::saveChanges,

                    enabled = !uiState.isLoading,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {

                    if (uiState.isLoading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text("Save Changes")
                    }
                }
            }

            // DELETE BUTTON
            item {

                OutlinedButton(
                    onClick = {
                        showDeleteDialog = true
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {

                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Delete Record")
                }
            }
        }
    }
}

// THANK YOU MESSAGE
object NotificationHelper {

    fun buildBorrowerThankYouMessage(
        borrowerName: String,
        itemName: String,
        amount: Double?
    ): String {

        return if (amount != null) {

            "Hi $borrowerName 😊 Thank you for returning/reminding me about Ksh ${
                amount.toInt()
            } for \"$itemName\" 🙌"

        } else {

            "Hi $borrowerName 😊 Thank you for returning \"$itemName\" 🙌"
        }
    }
}

// CONTACT PICKER
private fun queryContactInfo(
    context: Context,
    uri: Uri,
    onResult: (String, String) -> Unit
) {

    context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )?.use { cursor ->

        if (cursor.moveToFirst()) {

            val name =
                cursor.getString(
                    cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                ) ?: ""

            val id =
                cursor.getString(
                    cursor.getColumnIndex(
                        ContactsContract.Contacts._ID
                    )
                ) ?: ""

            var phone = ""

            if (id.isNotEmpty()) {

                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(id),
                    null

                )?.use { phoneCursor ->

                    if (phoneCursor.moveToFirst()) {

                        phone =
                            phoneCursor.getString(
                                phoneCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            ) ?: ""
                    }
                }
            }

            onResult(name, phone)
        }
    }
}