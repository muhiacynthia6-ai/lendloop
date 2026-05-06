package com.example.lendloop.Screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.Status
import com.example.lendloop.models.*
import com.example.lendloop.ui.theme.*
import com.example.lendloop.util.toFormattedDate
import kotlin.math.max

private val LenderGradient   = listOf(Color(0xFF461A7A), Color(0xFF7B3FBF))
private val BorrowerGradient = listOf(Color(0xFF1A4566), Color(0xFF2196A8))
private val OverdueRed       = Color(0xFFC0392B)
private val GreenSuccess     = Color(0xFF2D7A5F)
private val MpesaGreen       = Color(0xFF4CAF50)
private val PayPalBlue       = Color(0xFF1565C0)
private val CashGold         = Color(0xFFF9A825)

// ═══════════════════════════════════════════════════════════════════════════════
//  ROOT — animates between role selector ↔ lender ↔ borrower
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun DashboardScreen(
    onNavigateToHome: () -> Unit,
    onLogout:         () -> Unit,
    navController:    NavController,
    viewModel:        DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState    = state.role,
        transitionSpec = {
            fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 5 } togetherWith
                    fadeOut(tween(180))
        },
        label = "role_anim"
    ) { role ->
        when (role) {
            DashboardRole.NONE     -> RoleSelectorScreen(
                userName         = state.userName,
                onSelectLender   = { viewModel.selectRole(DashboardRole.LENDER) },
                onSelectBorrower = { viewModel.selectRole(DashboardRole.BORROWER) },
                onGoHome         = onNavigateToHome,
                onLogout         = onLogout
            )
            DashboardRole.LENDER   -> LenderDashboard(
                stats    = state.lender,
                userName = state.userName,
                onBack   = { viewModel.clearRole() },
                onGoHome = onNavigateToHome
            )
            DashboardRole.BORROWER -> BorrowerDashboard(
                stats    = state.borrower,
                userName = state.userName,
                onBack   = { viewModel.clearRole() },
                onGoHome = onNavigateToHome
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  ROLE SELECTOR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun RoleSelectorScreen(
    userName:         String,
    onSelectLender:   () -> Unit,
    onSelectBorrower: () -> Unit,
    onGoHome:         () -> Unit,
    onLogout:         () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0533), Color(0xFF2D0E52), Color(0xFF120B1E))
                )
            )
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier         = Modifier.size(72.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) { Text("🔄", fontSize = 36.sp) }

            Spacer(Modifier.height(20.dp))
            Text("Welcome back,", color = Color.White.copy(alpha = 0.65f), fontSize = 15.sp)
            Text(userName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "How are you using LendLoop today?",
                color = Color.White.copy(alpha = 0.50f), fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(44.dp))

            RoleCard(
                gradient = LenderGradient,
                emoji    = "💸",
                title    = "I'm a Lender",
                subtitle = "Track money & items I've lent out",
                badge    = "See who owes you",
                onClick  = onSelectLender
            )
            Spacer(Modifier.height(18.dp))
            RoleCard(
                gradient = BorrowerGradient,
                emoji    = "🤝",
                title    = "I'm a Borrower",
                subtitle = "Track what I owe and my repayments",
                badge    = "View your dues",
                onClick  = onSelectBorrower
            )

            Spacer(Modifier.height(38.dp))

            OutlinedButton(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.75f)),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Text("Open full app")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onLogout) {
                Text("Log out", color = Color.White.copy(alpha = 0.38f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun RoleCard(
    gradient: List<Color>,
    emoji:    String,
    title:    String,
    subtitle: String,
    badge:    String,
    onClick:  () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(20.dp),
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(gradient))
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier         = Modifier.size(54.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 26.sp) }

                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(subtitle, color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward, null,
                    tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  LENDER DASHBOARD
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderDashboard(stats: LenderStats, userName: String, onBack: () -> Unit, onGoHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lender Dashboard", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Hi $userName 👋", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.SwapHoriz, "Switch role")
                    }
                },
                actions = {
                    TextButton(onClick = onGoHome) { Text("Full App", fontSize = 13.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero
            HeroBanner(
                gradient = LenderGradient,
                topLabel = "Total Lent Out",
                bigValue = "Ksh %.0f".format(stats.totalLentOut),
                pills    = listOfNotNull(
                    "Active" to "${stats.activeLoansCount}",
                    "Recovered" to "Ksh %.0f".format(stats.totalRecovered),
                    if (stats.overdueCount > 0) "Overdue ⚠" to "${stats.overdueCount}" else null
                ),
                overdueHighlight = stats.overdueCount > 0
            )

            DashSection("Overview") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoTile(Modifier.weight(1f), Icons.AutoMirrored.Filled.TrendingUp, Amethyst500, "Active Loans", "${stats.activeLoansCount}")
                    InfoTile(Modifier.weight(1f), Icons.Default.CheckCircle, GreenSuccess, "Recovered", "Ksh %.0f".format(stats.totalRecovered))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoTile(
                        Modifier.weight(1f), Icons.Default.Warning,
                        if (stats.overdueCount > 0) OverdueRed else PurpleGray400,
                        "Overdue", "${stats.overdueCount} · Ksh %.0f".format(stats.overdueAmount)
                    )
                    InfoTile(Modifier.weight(1f), Icons.Default.People, Violet500, "Top Borrowers", "${stats.topBorrowers.size}")
                }
            }

            if (stats.topBorrowers.isNotEmpty()) {
                DashSection("Who Owes You Most") { TopBorrowersCard(stats.topBorrowers) }
            }

            DashSection("Payments Received") {
                PaymentBreakdownCard(stats.mpesaReceived, stats.paypalReceived, stats.cashReceived)
            }

            DashSection("Activity — Last 6 Months") { BarChartCard(stats.monthlyBars) }

            DashSection("Recent") { RecentRecordsCard(stats.recentLent, Direction.LENT) }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  BORROWER DASHBOARD
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerDashboard(stats: BorrowerStats, userName: String, onBack: () -> Unit, onGoHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Borrower Dashboard", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Hi $userName 👋", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.SwapHoriz, "Switch role") }
                },
                actions = {
                    TextButton(onClick = onGoHome) { Text("Full App", fontSize = 13.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeroBanner(
                gradient = BorrowerGradient,
                topLabel = "Total You Owe",
                bigValue = "Ksh %.0f".format(stats.totalOwed),
                pills    = listOfNotNull(
                    "Active" to "${stats.activeBorrowsCount}",
                    "Paid" to "Ksh %.0f".format(stats.totalPaid),
                    if (stats.overdueCount > 0) "Overdue ⚠" to "${stats.overdueCount}" else null
                ),
                overdueHighlight = stats.overdueCount > 0
            )

            DashSection("Overview") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoTile(Modifier.weight(1f), Icons.AutoMirrored.Filled.TrendingDown, Color(0xFF1A4566), "Active Borrows", "${stats.activeBorrowsCount}")
                    InfoTile(Modifier.weight(1f), Icons.Default.CheckCircle, GreenSuccess, "Total Paid", "Ksh %.0f".format(stats.totalPaid))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoTile(
                        Modifier.weight(1f), Icons.Default.Warning,
                        if (stats.overdueCount > 0) OverdueRed else PurpleGray400,
                        "Overdue", "${stats.overdueCount}"
                    )
                    InfoTile(Modifier.weight(1f), Icons.Default.AccessTime, Gold500, "Remaining", "Ksh %.0f".format(stats.totalOwed - stats.totalPaid))
                }
            }

            if (stats.nextDueRecord != null) {
                DashSection("Next Due") { NextDueCard(stats.nextDueRecord) }
            }

            DashSection("Payments Made") {
                PaymentBreakdownCard(stats.mpesaPaid, stats.paypalPaid, stats.cashPaid)
            }

            DashSection("Activity — Last 6 Months") { BarChartCard(stats.monthlyBars) }

            DashSection("Recent") { RecentRecordsCard(stats.recentBorrowed, Direction.BORROWED) }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun DashSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        content()
    }
}

@Composable
fun HeroBanner(
    gradient:         List<Color>,
    topLabel:         String,
    bigValue:         String,
    pills:            List<Pair<String, String>>,
    overdueHighlight: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient)).padding(20.dp)
    ) {
        Column {
            Text(topLabel, color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
            Text(bigValue, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                pills.forEach { (label, value) ->
                    val isOverduePill = overdueHighlight && label.contains("Overdue")
                    Column {
                        Text(
                            value,
                            color      = if (isOverduePill) Color(0xFFFFADAD) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                        Text(label, color = Color.White.copy(alpha = 0.58f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoTile(modifier: Modifier, icon: ImageVector, iconColor: Color, label: String, value: String) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier.size(36.dp).clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TopBorrowersCard(borrowers: List<Pair<String, Double>>) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            borrowers.forEachIndexed { i, (name, amount) ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier         = Modifier.size(28.dp).clip(CircleShape)
                                .background(when (i) { 0 -> Gold500; 1 -> PurpleGray400; else -> Color(0xFFAD8A56) }),
                            contentAlignment = Alignment.Center
                        ) { Text("${i + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(10.dp))
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Text("Ksh %.0f".format(amount), color = Amethyst500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (i < borrowers.lastIndex)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            }
        }
    }
}

@Composable
fun NextDueCard(record: BorrowRecord) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border   = androidx.compose.foundation.BorderStroke(1.dp, CashGold.copy(alpha = 0.38f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier.size(44.dp).clip(CircleShape).background(CashGold.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.AccessTime, null, tint = CashGold, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Next Due", fontSize = 11.sp, color = CashGold, fontWeight = FontWeight.SemiBold)
                Text(record.itemName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "Due ${record.dueDate?.toFormattedDate() ?: "—"}  ·  from ${record.personName}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (record.amount != null)
                Text("Ksh %.0f".format(record.amount), fontWeight = FontWeight.Bold, color = CashGold)
        }
    }
}

@Composable
fun PaymentBreakdownCard(mpesa: Double, paypal: Double, cash: Double) {
    val total = (mpesa + paypal + cash).coerceAtLeast(1.0)
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PayRow("📱", "M-Pesa",  mpesa,  mpesa / total,  MpesaGreen)
            PayRow("🅿",  "PayPal",  paypal, paypal / total, PayPalBlue)
            PayRow("💵", "Cash",    cash,   cash / total,   CashGold)
        }
    }
}

@Composable
fun PayRow(emoji: String, label: String, amount: Double, fraction: Double, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            Text("Ksh %.0f".format(amount), fontWeight = FontWeight.SemiBold, color = color)
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(fraction.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color)
            )
        }
    }
}

@Composable
fun BarChartCard(bars: List<MonthlyBar>) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendPill(Amethyst400, "Lent")
                LegendPill(Color(0xFF26A69A), "Borrowed")
            }
            Spacer(Modifier.height(14.dp))
            if (bars.isEmpty() || bars.all { it.lent == 0 && it.borrowed == 0 }) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) { Text("No activity yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
            } else {
                val maxVal = bars.maxOf { max(it.lent, it.borrowed) }.coerceAtLeast(1)
                Row(
                    modifier              = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    bars.forEach { bar ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment     = Alignment.Bottom,
                                modifier              = Modifier.height(95.dp)
                            ) {
                                val lh = (bar.lent.toFloat() / maxVal * 88).dp
                                Box(Modifier.width(9.dp).height(lh.coerceAtLeast(3.dp))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Amethyst400).align(Alignment.Bottom))
                                val bh = (bar.borrowed.toFloat() / maxVal * 88).dp
                                Box(Modifier.width(9.dp).height(bh.coerceAtLeast(3.dp))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Color(0xFF26A69A)).align(Alignment.Bottom))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(bar.label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendPill(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RecentRecordsCard(records: List<BorrowRecord>, direction: Direction) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No records yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            Column(modifier = Modifier.padding(4.dp)) {
                records.forEach { record ->
                    RecentRow(record, direction)
                    if (record != records.last())
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.13f)
                        )
                }
            }
        }
    }
}

@Composable
fun RecentRow(record: BorrowRecord, direction: Direction) {
    val isReturned = record.status == Status.RETURNED
    val dotColor   = when {
        isReturned                  -> GreenSuccess
        direction == Direction.LENT -> Amethyst400
        else                        -> Color(0xFF26A69A)
    }
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(38.dp).clip(CircleShape).background(dotColor.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (direction == Direction.LENT) Icons.AutoMirrored.Filled.TrendingUp
                else Icons.AutoMirrored.Filled.TrendingDown,
                null, tint = dotColor, modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(record.itemName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${if (direction == Direction.LENT) "To" else "From"} ${record.personName}",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (record.amount != null)
                Text("Ksh %.0f".format(record.amount), fontWeight = FontWeight.Bold,
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(dotColor.copy(alpha = 0.13f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(if (isReturned) "Done" else "Active",
                    fontSize = 10.sp, color = dotColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}