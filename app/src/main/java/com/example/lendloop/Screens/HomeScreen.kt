package com.example.lendloop.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.lendloop.models.HomeViewModel
import com.example.lendloop.ui.components.BorrowCard
import com.example.lendloop.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddRecord:    () -> Unit,
    onRecordClick:  (Int) -> Unit,
    onLogout:       () -> Unit,
    onHistory:      () -> Unit,
    onProfile:      () -> Unit,
    onDashboard:    () -> Unit,
    onReviewRecord: (recordId: Int, revieweeId: Int) -> Unit,
    onPayRecord:    (recordId: Int, amount: Double, personName: String) -> Unit,
    onEditRecord:   (Int) -> Unit,                                              // ← NEW
    navController:  NavHostController,
    viewModel:      HomeViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogout  by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch  by remember { mutableStateOf(false) }
    val tabs = listOf("Lent Out", "Borrowed")

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchBar(
                    query = searchQuery, onQueryChange = { searchQuery = it },
                    onSearch = {}, active = false, onActiveChange = {},
                    placeholder = { Text("Search by item or person...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        IconButton(onClick = { searchQuery = ""; showSearch = false }) {
                            Icon(Icons.Default.Close, "Close search")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {}
            } else {
                TopAppBar(
                    title = { Text("LendLoop", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        IconButton(onClick = onProfile) {
                            Icon(Icons.Default.Person, "Profile")
                        }
                        IconButton(onClick = onHistory) {
                            Icon(Icons.Default.History, "History")
                        }
                        IconButton(onClick = onDashboard) {
                            Icon(Icons.Default.BarChart, "Dashboard")
                        }
                        IconButton(onClick = { showLogout = true }) {
                            Icon(Icons.Default.ExitToApp, "Logout")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecord) {
                Icon(Icons.Default.Add, "Add record")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SummaryRow(
                totalLent     = uiState.totalLentAmount,
                totalBorrowed = uiState.totalBorrowedAmount
            )
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) })
                }
            }

            val allRecords = if (selectedTab == 0) uiState.lentOut else uiState.borrowed
            val records = if (searchQuery.isBlank()) allRecords
            else allRecords.filter {
                it.itemName.contains(searchQuery, ignoreCase = true) ||
                        it.personName.contains(searchQuery, ignoreCase = true)
            }

            if (records.isEmpty()) {
                EmptyState(message = when {
                    searchQuery.isNotBlank() -> "No results for \"$searchQuery\""
                    selectedTab == 0         -> "Nothing lent out yet.\nTap + to add a record."
                    else                     -> "Nothing borrowed yet.\nTap + to add a record."
                })
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        BorrowCard(
                            record         = record,
                            onClick        = { onRecordClick(record.id) },
                            onMarkReturned = { viewModel.markReturned(record.id) },
                            onReview       = { onReviewRecord(record.id, record.personId) },
                            onPay          = if (record.amount != null && record.amount > 0) {
                                { onPayRecord(record.id, record.amount, record.personName) }
                            } else null,
                            onEdit         = { onEditRecord(record.id) },               // ← NEW
                            onDelete       = { viewModel.deleteRecord(record) }         // ← NEW
                        )
                    }
                }
            }
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Log out?") },
            text  = { Text("You'll need your phone number and PIN to log back in.") },
            confirmButton = {
                TextButton(onClick = { showLogout = false; viewModel.logout(); onLogout() }) {
                    Text("Log out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SummaryRow(totalLent: Double, totalBorrowed: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(Modifier.weight(1f), "Total Lent Out", totalLent,
            MaterialTheme.colorScheme.primaryContainer)
        SummaryCard(Modifier.weight(1f), "Total Borrowed", totalBorrowed,
            MaterialTheme.colorScheme.secondaryContainer)
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, label: String, amount: Double, containerColor: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(if (amount > 0) "Ksh %.0f".format(amount) else "—",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}