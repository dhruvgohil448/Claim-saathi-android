package com.claimsaathi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val prompts = listOf(
    "What does my policy cover?",
    "What is my room-rent limit?",
    "Which documents are missing?",
    "Explain this claim status.",
    "Why was this amount deducted?"
)

@Composable
fun AssistantScreen(state: AppState) {
    var draft by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(AppBackground)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader("Ask Saathi", "Answers use the sample policy and claim only.")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prompts.forEach { prompt ->
                    Text(
                        prompt,
                        color = Navy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .clickable { state.ask(prompt) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            state.messages.forEach { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start) {
                    Text(
                        message.text,
                        color = if (message.isUser) Color.White else Ink,
                        modifier = Modifier
                            .fillMaxWidth(0.86f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (message.isUser) Navy else Color.White)
                            .padding(14.dp)
                    )
                }
            }
        }
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about the policy or claim") },
                shape = RoundedCornerShape(14.dp)
            )
            IconButton(onClick = {
                state.ask(draft)
                draft = ""
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Navy)
            }
        }
    }
}

@Composable
fun ProfileScreen(state: AppState, onLogout: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader("Profile", "Demo customer account.")
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "RS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Navy)
                        .padding(top = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Column {
                    Text(state.userName, color = Navy, fontWeight = FontWeight.SemiBold)
                    Text(state.email, color = Muted, fontSize = 13.sp)
                    Text(state.phone, color = Muted, fontSize = 13.sp)
                }
            }
        }
        AppCard {
            Text("Linked policy", color = Navy, fontWeight = FontWeight.SemiBold)
            Text("${state.policy.insurer} · ${state.policy.number}", color = Ink)
            Text("Sum insured ${inr(state.policy.sumInsured)}", color = Muted, fontSize = 13.sp)
        }
        AppCard {
            KeyValue("Role", "Customer")
            Spacer(Modifier.height(8.dp))
            KeyValue("Data", "Sample records on this device")
            Spacer(Modifier.height(8.dp))
            KeyValue("Payouts", "Not connected")
        }
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Log out", color = Danger, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
