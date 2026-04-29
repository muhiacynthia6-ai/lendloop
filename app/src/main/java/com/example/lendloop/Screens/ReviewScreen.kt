package com.example.lendloop.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lendloop.ui.review.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a Review", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.alreadyReviewed) {
                // Already reviewed state
                Text(text = "✅", fontSize = 56.sp)
                Text(
                    text = "Already Reviewed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You've already left a review for this transaction.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Go back")
                }
            } else {
                // Review form
                Text(text = "⭐", fontSize = 56.sp)
                Text(
                    text = "How did it go?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rate this transaction to help build trust in the community.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Star rating
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (uiState.rating) {
                            1 -> "😞 Poor"
                            2 -> "😐 Fair"
                            3 -> "🙂 Good"
                            4 -> "😊 Great"
                            5 -> "🤩 Excellent!"
                            else -> "Tap to rate"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.rating > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { star ->
                            val isSelected = star <= uiState.rating
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.2f else 1f,
                                animationSpec = tween(150),
                                label = "star_scale_$star"
                            )
                            Icon(
                                imageVector = if (isSelected)
                                    Icons.Filled.Star
                                else
                                    Icons.Outlined.StarOutline,
                                contentDescription = "$star stars",
                                tint = if (isSelected) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(scale)
                                    .clickable { viewModel.onRatingChange(star) }
                            )
                        }
                    }
                }

                // Comment
                OutlinedTextField(
                    value = uiState.comment,
                    onValueChange = viewModel::onCommentChange,
                    label = { Text("Comment (optional)") },
                    placeholder = { Text("e.g. Returned on time, great person to lend to!") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Error
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Submit button
                Button(
                    onClick = viewModel::submitReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Submit Review",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                // Skip button
                TextButton(
                    onClick = viewModel::skipReview,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Skip for now",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
