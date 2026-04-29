package com.example.lendloop.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lendloop.data.db.Review
import com.example.lendloop.data.db.TrustScore
import com.example.lendloop.models.ProfileViewModel
import com.example.lendloop.models.ProfileUiState
import com.example.lendloop.ui.components.TrustScoreCard
import com.example.lendloop.util.toFormattedDate

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileContent(uiState, onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Profile header ────────────────────────────────────────
                item {
                    ProfileHeader(
                        userName      = uiState.userName,
                        averageRating = uiState.averageRating,
                        reviewCount   = uiState.reviews.size
                    )
                }

                // ── Trust score ───────────────────────────────────────────
                item {
                    Text(
                        text = "TRUST SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TrustScoreCard(trustScore = uiState.trustScore)
                }

                // ── Stats ─────────────────────────────────────────────────
                item {
                    Text(
                        text = "STATS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatsRow(trustScore = uiState.trustScore)
                }

                // ── Restriction banner ────────────────────────────────────
                uiState.trustScore?.let { score ->
                    if (score.isRestricted) {
                        item {
                            RestrictionBanner(
                                restrictedUntil = score.restrictedUntil
                            )
                        }
                    }
                }

                // ── Reviews header ────────────────────────────────────────
                item {
                    Text(
                        text = "REVIEWS (${uiState.reviews.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                // ── Reviews list ──────────────────────────────────────────
                if (uiState.reviews.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No reviews yet.\n" +
                                            "Complete transactions to earn reviews.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.reviews, key = { it.id }) { review ->
                        ReviewCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    averageRating: Float,
    reviewCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.firstOrNull()
                            ?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= averageRating.toInt())
                            Icons.Filled.Star
                        else
                            Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (star <= averageRating.toInt())
                            Color(0xFFFFC107)
                        else
                            Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (averageRating > 0)
                        "%.1f ($reviewCount reviews)".format(averageRating)
                    else
                        "No reviews yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsRow(trustScore: TrustScore?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label    = "Borrowed",
            value    = "${trustScore?.totalBorrowed ?: 0}",
            color    = MaterialTheme.colorScheme.primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label    = "Returned",
            value    = "${trustScore?.totalReturned ?: 0}",
            color    = Color(0xFF4CAF50)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label    = "Rate",
            value    = "%.0f%%".format(trustScore?.returnRate ?: 100f),
            color    = when {
                (trustScore?.returnRate ?: 100f) >= 80f -> Color(0xFF4CAF50)
                (trustScore?.returnRate ?: 100f) >= 50f -> Color(0xFFFF9800)
                else                                    -> Color(0xFFF44336)
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RestrictionBanner(restrictedUntil: Long?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "🔒 Borrowing Restricted",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = "Your return rate dropped below 50%. You cannot borrow " +
                        "items until ${restrictedUntil?.toFormattedDate() ?: "restriction lifts"}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = "Return your outstanding items to improve your rate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= review.rating)
                                Icons.Filled.Star
                            else
                                Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (star <= review.rating)
                                Color(0xFFFFC107)
                            else
                                Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text  = review.createdAt.toFormattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text  = "\"${review.comment}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userName = "John Doe",
                averageRating = 4.5f,
                reviews = listOf(
                    Review(id = 1, recordId = 1, reviewerId = 2, revieweeId = 1, rating = 5, comment = "Great guy!"),
                    Review(id = 2, recordId = 2, reviewerId = 3, revieweeId = 1, rating = 4, comment = "Prompt return.")
                ),
                trustScore = TrustScore(
                    userId = 1,
                    totalBorrowed = 10,
                    totalReturned = 9,
                    returnRate = 90f
                )
            ),
            onNavigateBack = {}
        )
    }
}
