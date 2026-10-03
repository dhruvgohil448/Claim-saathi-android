package com.claimsaathi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onContinue: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(28.dp))
        LogoMark()
        Text("Claim Saathi", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Your AI health insurance claim companion",
            color = Muted,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "Understand. Upload. Track. Get paid.",
            color = Navy,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        AppCard {
            Text("DEMO LOGIN", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Rajesh Sharma", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("customer@claimsaathi.demo", color = Muted, fontSize = 13.sp)
            Text("Customer · sample policy already loaded", color = Muted, fontSize = 13.sp)
        }
        PrimaryButton("Continue as Rajesh", onContinue)
        Text(
            "Demo only. No real insurer, hospital, or payment is connected.",
            color = Muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeScreen(
    state: AppState,
    onOpenClaim: (String) -> Unit,
    onOpenPolicy: () -> Unit,
    onStartClaim: () -> Unit,
    onOpenDocs: () -> Unit,
    onOpenAssistant: () -> Unit
) {
    val claim = state.activeClaim
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Good morning", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(state.userName, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "RS",
                color = Navy,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Pale)
                    .padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }
        if (claim?.query != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Warning.copy(alpha = 0.14f))
                    .clickable { onOpenClaim(claim.id) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Action required", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text(claim.query, color = Muted, fontSize = 13.sp)
                }
            }
        }
        if (claim != null) {
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(claim.id, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    StatusBadge(claim.status.label, statusTint(claim.status))
                }
                Spacer(Modifier.height(8.dp))
                Text(claim.hospital, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(claim.reason, color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(inr(claim.amount), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(claim.updated, color = Muted, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
                val done = claim.steps.count { it.state == StepState.Done }
                Text("Progress  $done of ${claim.steps.size}", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                ProgressBar(done / claim.steps.size.toFloat())
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Track claim") { onOpenClaim(claim.id) }
            }
        }
        Text("Quick actions", color = Navy, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Upload policy", Modifier.weight(1f), onOpenPolicy)
            ActionTile("Start claim", Modifier.weight(1f), onStartClaim)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Documents", Modifier.weight(1f), onOpenDocs)
            ActionTile("Ask Saathi", Modifier.weight(1f), onOpenAssistant)
        }
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Your policy", color = Navy, fontWeight = FontWeight.SemiBold)
                Text("View", color = Cyan, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onOpenPolicy))
            }
            Text("${state.policy.insurer} · ${state.policy.number}", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Sum insured", inr(state.policy.sumInsured), Modifier.weight(1f))
                MetricTile("Room rent", "${inr(state.policy.roomRentPerDay)}/day", Modifier.weight(1f))
            }
        }
        Text("Recent activity", color = Navy, fontWeight = FontWeight.SemiBold)
        AppCard {
            ActivityRow("Query opened", "Payment receipt needed for CLM-1042", "Today")
            Spacer(Modifier.height(10.dp))
            ActivityRow("Hospital bill verified", "Apollo bill matched the claim amount", "Yesterday")
            Spacer(Modifier.height(10.dp))
            ActivityRow("Policy read", "Sum insured and room rent explained", "28 Sep")
        }
    }
}

@Composable
private fun ActionTile(title: String, modifier: Modifier, onClick: () -> Unit) {
    Text(
        title,
        color = Ink,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    )
}

@Composable
private fun ActivityRow(title: String, detail: String, time: String) {
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(detail, color = Muted, fontSize = 13.sp)
        }
        Text(time, color = Muted, fontSize = 12.sp)
    }
}
