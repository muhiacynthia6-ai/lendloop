package com.example.lendloop.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.lendloop.ui.review.ReviewViewModel

@Composable
fun WelcomeScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    navController: NavHostController,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔄", fontSize = 72.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LendLoop",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Never lose track of what you've\nlent or borrowed again.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureRow(emoji = "📋", text = "Log items, money, books — anything")
            FeatureRow(emoji = "⏰", text = "Get notified when items are overdue")
            FeatureRow(emoji = "💬", text = "Send reminders via WhatsApp or SMS")
            FeatureRow(emoji = "📊", text = "See exactly who owes you what")
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("I already have an account", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun FeatureRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
