package com.claimsaathi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClaimsScreen(state: AppState, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader("Claims", "Track every step from submission to settlement.")
        state.claims.forEach { claim ->
            AppCard(Modifier.clickable { onOpen(claim.id) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(claim.id, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    StatusBadge(claim.status.label, statusTint(claim.status))
                }
                Spacer(Modifier.height(6.dp))
                Text(claim.hospital, color = Navy, fontWeight = FontWeight.SemiBold)
                Text(claim.reason, color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(inr(claim.amount), color = Ink, fontWeight = FontWeight.Bold)
                    Text(claim.updated, color = Muted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ClaimDetailScreen(state: AppState, claimId: String, onSettlement: () -> Unit) {
    val claim = state.claim(claimId) ?: return
    var reply by remember(claimId) { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AppCard {
            StatusBadge(claim.status.label, statusTint(claim.status))
            Spacer(Modifier.height(8.dp))
            Text(claim.hospital, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(claim.reason, color = Muted, fontSize = 13.sp)
            Text(inr(claim.amount), color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Updated ${claim.updated}", color = Muted, fontSize = 13.sp)
        }
        AppCard {
            Text("Timeline", color = Navy, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            claim.steps.forEachIndexed { index, step ->
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(stepColor(step.state)))
                        if (index < claim.steps.lastIndex) {
                            Box(
                                Modifier
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(if (step.state == StepState.Done) Success else Pale)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            step.title,
                            color = if (step.state == StepState.Upcoming) Muted else Ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(step.detail, color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }
        if (claim.query != null) {
            AppCard {
                Text("Insurer query", color = Navy, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(claim.query, color = Ink)
                Text("Reply in plain language. This stays inside the demo.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("I uploaded the receipt") }
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton("Send reply") {
                    state.replyToQuery(claim.id, reply)
                    reply = ""
                }
            }
        }
        if (claim.settlement != null) {
            PrimaryButton("View settlement", onSettlement)
        }
        TextButton(onClick = { state.advance(claim.id) }, modifier = Modifier.fillMaxWidth()) {
            Text("Advance demo status", color = Muted)
        }
        state.notice?.let { Text(it, color = Muted, fontSize = 13.sp) }
    }
}

private fun stepColor(state: StepState) = when (state) {
    StepState.Done -> Success
    StepState.Current -> Cyan
    StepState.Upcoming -> Pale
}

@Composable
fun SettlementScreen(state: AppState, claimId: String) {
    val claim = state.claim(claimId) ?: return
    val settlement = claim.settlement ?: return
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader("Settlement", "Every figure below is sample demo data.")
        AppCard {
            Text("Approved amount", color = Muted, fontSize = 13.sp)
            Text(inr(settlement.approvedAmount), color = Success, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(claim.hospital, color = Muted, fontSize = 13.sp)
        }
        AppCard {
            AmountRow("Hospital bill", inr(settlement.billAmount), Ink)
            settlement.lines.forEach { line ->
                Spacer(Modifier.height(8.dp))
                AmountRow(line.title, "− ${inr(line.amount)}", Danger)
                Text(line.reason, color = Muted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            AmountRow("You receive", inr(settlement.approvedAmount), Success)
        }
        Text(
            "Deductions follow the sample policy: non-payables, the ₹5,000 room-rent cap, and 10% co-pay. This is not a real payout.",
            color = Muted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun AmountRow(title: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DocsScreen(state: AppState) {
    val ready = state.documents.count { it.status == DocStatus.Verified }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(
            "Documents",
            "$ready of ${state.documents.size} verified. ${state.missingDocumentCount} still need attention."
        )
        AppCard {
            Text("Checklist", color = Navy, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            ProgressBar(ready / state.documents.size.toFloat(), Success)
        }
        state.documents.forEach { document ->
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(document.title, color = Ink, fontWeight = FontWeight.SemiBold)
                    StatusBadge(document.status.label, docTint(document.status))
                }
                document.fileName?.let { Text(it, color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                Text(document.note, color = Muted, fontSize = 13.sp)
                if (document.status == DocStatus.Missing) {
                    TextButton(onClick = { state.markUploaded(document.id) }) {
                        Text("Upload demo file", color = Navy, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        state.notice?.let { Text(it, color = Muted, fontSize = 13.sp) }
    }
}

@Composable
fun PolicyScreen(state: AppState, onAsk: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader("Policy", "Read from the sample Star Health PDF.")
        AppCard {
            Text("ON FILE", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${state.policy.insurer} ${state.policy.product}", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(state.policy.number, color = Muted, fontSize = 13.sp)
            Text("Holder · ${state.policy.holder}", color = Muted, fontSize = 13.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WhiteTile("Sum insured", inr(state.policy.sumInsured), Modifier.weight(1f))
            WhiteTile("Room rent", "${inr(state.policy.roomRentPerDay)} / day", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WhiteTile("Co-pay", state.policy.coPay, Modifier.weight(1f))
            WhiteTile("Network", "Demo hospital list", Modifier.weight(1f))
        }
        AppCard {
            Text("Waiting periods", color = Navy, fontWeight = FontWeight.SemiBold)
            state.policy.waitingPeriods.forEach { Text(it, color = Ink, fontSize = 13.sp) }
        }
        AppCard {
            Text("Exclusions", color = Navy, fontWeight = FontWeight.SemiBold)
            state.policy.exclusions.forEach { Text(it, color = Ink, fontSize = 13.sp) }
        }
        Text(
            "If a rule is not in the sample document, the assistant says it was not found. It does not guess.",
            color = Muted,
            fontSize = 13.sp
        )
        PrimaryButton("Ask about this policy", onAsk)
    }
}

@Composable
private fun WhiteTile(label: String, value: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Text(label, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Navy, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StartClaimScreen(state: AppState, onSubmitted: () -> Unit) {
    var hospital by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader("Start a claim", "Pre-authorisation preview. Nothing is sent to a hospital.")
        AppCard {
            LabeledField("Hospital", hospital, { hospital = it }, "Apollo Hospitals, Ahmedabad")
            LabeledField("Treatment", reason, { reason = it }, "Dengue admission")
            LabeledField("Estimated amount", amount, { amount = it.filter(Char::isDigit) }, "220000", KeyboardType.Number)
            TextButton(onClick = {
                hospital = "Apollo Hospitals, Ahmedabad"
                reason = "Planned dengue admission"
                amount = "180000"
                error = null
            }) { Text("Fill demo details", color = Cyan, fontWeight = FontWeight.SemiBold) }
        }
        AppCard {
            Text("Pre-auth preview", color = Navy, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            KeyValue("Patient", state.userName)
            KeyValue("Policy", state.policy.number)
            KeyValue("Hospital", hospital.ifBlank { "—" })
            KeyValue("Reason", reason.ifBlank { "—" })
            KeyValue("Estimate", amount.toIntOrNull()?.let(::inr) ?: "—")
        }
        error?.let { Text(it, color = Danger, fontSize = 13.sp) }
        PrimaryButton("Submit demo pre-auth") {
            val value = amount.toIntOrNull()
            if (hospital.isBlank() || reason.isBlank() || value == null || value <= 0) {
                error = "Add a hospital, a reason, and an amount."
            } else {
                state.submitClaim(hospital.trim(), reason.trim(), value)
                onSubmitted()
            }
        }
        Text("Demo only. This does not contact an insurer or TPA.", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboard: KeyboardType = KeyboardType.Text
) {
    Text(label, color = Muted, fontSize = 13.sp)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        singleLine = true
    )
    Spacer(Modifier.height(8.dp))
}
