package com.claimsaathi.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.claimsaathi.app.data.AmountWarning
import com.claimsaathi.app.data.ChatBody
import com.claimsaathi.app.data.ChatCard
import com.claimsaathi.app.data.CreateClaimBody
import com.claimsaathi.app.data.DemoTemplates
import com.claimsaathi.app.data.Finance
import com.claimsaathi.app.data.Network
import com.claimsaathi.app.data.PatientDetails
import com.claimsaathi.app.data.Policy
import com.claimsaathi.app.data.PreviewBody
import com.claimsaathi.app.data.PreviewResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

fun inrD(v: Double?): String = if (v == null) "–" else inr(Math.round(v).toInt())

fun warningColor(w: AmountWarning) = when (w.severity) { "high" -> Danger; "medium" -> Warning; else -> Muted }

@Composable
fun WarningList(warnings: List<AmountWarning>) {
    warnings.forEach { w ->
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(warningColor(w).copy(alpha = 0.10f)).padding(10.dp)
        ) {
            w.claimNumber?.let { Text(it, color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Text((if (w.severity == "info") "ℹ️ " else "⚠️ ") + w.message, color = warningColor(w), fontSize = 13.sp)
        }
    }
}

/** Camera / gallery / files → a ready multipart "file" part. */
class UploadSources(val camera: () -> Unit, val gallery: () -> Unit, val files: () -> Unit)

@Composable
fun rememberUploadSources(prefix: String, onPart: (MultipartBody.Part) -> Unit, onError: (String) -> Unit): UploadSources {
    val context = LocalContext.current
    fun fromUri(uri: Uri?) { if (uri != null) runCatching { context.filePart(uri) }.onSuccess(onPart).onFailure { onError(it.message ?: "Could not read the file") } }
    val files = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { fromUri(it) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { fromUri(it) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? -> bmp?.let { onPart(it.jpegPart(prefix)) } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) camera.launch(null) else onError("Camera permission is needed to take a photo.") }
    return UploadSources(
        camera = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) camera.launch(null)
            else permission.launch(Manifest.permission.CAMERA)
        },
        gallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        files = { files.launch(arrayOf("application/pdf", "image/jpeg", "image/png", "image/heic", "image/*")) }
    )
}

@Composable
fun UploadSourceRow(sources: UploadSources) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(sources.camera) { Text("Camera", color = Primary) }
        TextButton(sources.gallery) { Text("Gallery", color = Primary) }
        TextButton(sources.files) { Text("PDF / files", color = Navy) }
    }
}

@Composable
private fun chipColors2() = FilterChipDefaults.filterChipColors(selectedContainerColor = Pale, selectedLabelColor = Navy, containerColor = Color.White, labelColor = Muted)

@Composable
fun StartClaimScreen(vm: AppVm, onBack: () -> Unit, onCreated: (String) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var policies by remember { mutableStateOf<List<Policy>>(emptyList()) }
    var templates by remember { mutableStateOf<DemoTemplates?>(null) }
    var policyId by remember { mutableStateOf("") }
    var cashless by remember { mutableStateOf(false) }
    var hospital by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var patient by remember { mutableStateOf(vm.user?.name ?: "") }
    var admission by remember { mutableStateOf("") }
    var discharge by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var estimate by remember { mutableStateOf("") }
    var bill by remember { mutableStateOf("") }
    var details by remember { mutableStateOf<PatientDetails?>(null) }
    var roomType by remember { mutableStateOf<String?>(null) }
    var network by remember { mutableStateOf<Boolean?>(null) }
    var otp by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf<PreviewResult?>(null) }
    LaunchedEffect(Unit) {
        runCatching { Network.api.policies() }.onSuccess { policies = it }
        runCatching { Network.api.templates() }.onSuccess { templates = it }
        policyId = templates?.policyId ?: policies.firstOrNull()?.id.orEmpty()
    }
    LaunchedEffect(policyId, cashless, estimate, bill, room, days) {
        if (policyId.isBlank() || (estimate.isBlank() && bill.isBlank() && room.isBlank())) { preview = null; return@LaunchedEffect }
        delay(400)
        runCatching {
            Network.api.preview(PreviewBody(policyId, if (cashless) "PREAUTH" else "REIMBURSEMENT", estimate.toIntOrNull(), if (cashless) null else bill.toIntOrNull(), room.toIntOrNull(), days.toIntOrNull(), reason.ifBlank { null }, treatment.ifBlank { null }, admission.ifBlank { null }))
        }.onSuccess { preview = it }
    }
    fun useSample() {
        val t = (if (cashless) templates?.preauth else templates?.reimbursement) ?: return
        t.policyId?.let { policyId = it }
        hospital = t.hospital; city = t.hospitalCity.orEmpty(); reason = t.reason; treatment = t.treatment.orEmpty()
        patient = t.patientName ?: patient; admission = t.admissionDate.orEmpty(); discharge = t.dischargeDate.orEmpty()
        days = t.days?.toString().orEmpty(); room = t.roomRentPerDay?.toString().orEmpty()
        estimate = t.estimatedAmount?.toString().orEmpty(); bill = t.billAmount?.toString().orEmpty()
        details = t.patientDetails; roomType = t.roomType; network = t.isNetworkHospital
    }
    @Composable fun PreviewBlock() {
        preview?.let { p ->
            WarningList(p.warnings)
            p.estimate?.let { e -> Text("You may get ${inr(e.approvedAmount)} · you pay about ${inr(e.outOfPocket)} · cover left ${inr(p.remainingSumInsured)}", color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        }
    }
    FadeInColumn {
        TopBar("Start a claim", onBack)
        StepDots(step, 3)
        when (step) {
            0 -> {
                ScreenTitle("How should we file this?")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!cashless, { cashless = false }, { Text("Reimbursement") }, colors = chipColors2())
                    FilterChip(cashless, { cashless = true }, { Text("Pre-auth (cashless)") }, colors = chipColors2())
                }
                policies.forEach { policy ->
                    AppCard(Modifier.clickable { policyId = policy.id }) {
                        Text(policy.policyNumber, color = if (policyId == policy.id) Primary else Navy, fontWeight = FontWeight.Bold)
                        Text("${policy.insurer} · ${inr(policy.sumInsured)} cover", color = Muted, fontSize = 13.sp)
                    }
                }
                if (policies.isEmpty()) Text("No policy yet. Link a policy first.", color = Muted)
                PrimaryButton("Next", policyId.isNotBlank()) { step = 1 }
            }
            1 -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Hospital & amounts", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    TextButton({ useSample() }, enabled = templates != null) { Text("✨ Use sample data", color = Primary, fontWeight = FontWeight.Bold) }
                }
                Field("Hospital", hospital) { hospital = it }
                Field("City", city) { city = it }
                Field("Diagnosis / reason", reason) { reason = it }
                Field("Treatment", treatment) { treatment = it }
                Field("Patient", patient) { patient = it }
                Field(if (cashless) "Planned admission (yyyy-MM-dd)" else "Admission date (yyyy-MM-dd)", admission) { admission = it }
                if (!cashless) Field("Discharge date (yyyy-MM-dd)", discharge) { discharge = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("Days", days, Modifier.weight(1f), KeyboardType.Number) { days = it.filter(Char::isDigit) }
                    Field("Room rent / day", room, Modifier.weight(1f), KeyboardType.Number) { room = it.filter(Char::isDigit) }
                }
                Field("Estimated amount", estimate, keyboard = KeyboardType.Number) { estimate = it.filter(Char::isDigit) }
                if (!cashless) Field("Final bill amount", bill, keyboard = KeyboardType.Number) { bill = it.filter(Char::isDigit) }
                PreviewBlock()
                PrimaryButton("Review", hospital.length >= 2 && reason.length >= 2 && patient.length >= 2) { step = 2 }
                GhostButton("Back") { step = 0 }
            }
            else -> {
                ScreenTitle("Confirm & consent")
                AppCard {
                    KeyRow("Type", if (cashless) "Pre-auth (cashless)" else "Reimbursement")
                    KeyRow("Hospital", hospital)
                    KeyRow("Patient", patient)
                    KeyRow("Admission", admission)
                    KeyRow(if (cashless) "Estimate" else "Bill", inr(if (cashless) estimate.toIntOrNull() else bill.toIntOrNull() ?: estimate.toIntOrNull()))
                }
                PreviewBlock()
                Text("Consent OTP (demo ${templates?.consentOtp ?: "111000"})", color = Muted, fontSize = 12.sp)
                OtpBoxes(otp) { otp = it }
                ErrorText(vm.error)
                PrimaryButton("Submit claim", otp.length == 6, vm.busy) {
                    vm.work {
                        val created = Network.api.createClaim(
                            CreateClaimBody(
                                policyId = policyId, type = if (cashless) "PREAUTH" else "REIMBURSEMENT", hospital = hospital.trim(), hospitalCity = city.trim().ifBlank { null },
                                reason = reason.trim(), treatment = treatment.trim().ifBlank { null }, admissionType = if (cashless) "PLANNED" else "EMERGENCY",
                                admissionDate = admission.ifBlank { null }, dischargeDate = if (cashless) null else discharge.ifBlank { null }, days = days.toIntOrNull(),
                                roomType = roomType, roomRentPerDay = room.toIntOrNull(), isNetworkHospital = network,
                                billAmount = if (cashless) null else bill.toIntOrNull(), estimatedAmount = estimate.toIntOrNull(),
                                patientName = patient.trim(), patientDetails = details ?: PatientDetails(relation = "Self"), consentOtp = otp
                            )
                        )
                        vm.refreshHome()
                        onCreated(created.id)
                    }
                }
                TextButton({ step = 1 }) { Text("Edit details", color = Navy) }
            }
        }
    }
}

private data class ChatMsg(val mine: Boolean, val text: String, val cards: List<ChatCard> = emptyList())

@Composable
fun ChatTab(vm: AppVm) {
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMsg>()) }
    var chips by remember { mutableStateOf(listOf<String>()) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) runCatching { Network.api.chatSuggestions() }.onSuccess { s ->
            s.greeting?.let { messages = listOf(ChatMsg(false, it)) }
            chips = s.suggestions
        }
    }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
    fun send(text: String) {
        val t = text.trim()
        if (t.isBlank() || sending) return
        messages = messages + ChatMsg(true, t); input = ""; sending = true; error = null
        val claimId = vm.home?.currentClaim?.id
        scope.launch {
            runCatching { Network.api.chat(ChatBody(t, claimId)) }
                .onSuccess { r -> messages = messages + ChatMsg(false, r.answer, r.cards); chips = r.suggestions.ifEmpty { r.followUps.ifEmpty { chips } } }
                .onFailure { error = Network.apiMessage(it) }
            sending = false
        }
    }
    Column(Modifier.fillMaxSize().background(AppBackground).imePadding()) {
        Column(Modifier.fillMaxWidth().background(NavyBrush).statusBarsPadding().padding(20.dp)) {
            Text("Ask Saathi", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Claims, cover, bank balances and medical spend", color = Color.White.copy(0.75f), fontSize = 13.sp)
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { m ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.mine) Alignment.End else Alignment.Start, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(m.text, color = if (m.mine) Color.White else Navy, modifier = Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(18.dp)).background(if (m.mine) Navy else Color.White).padding(12.dp))
                    m.cards.forEach { FinanceCard(it) }
                }
            }
            if (sending) item { Text("Saathi is typing…", color = Muted, fontSize = 13.sp) }
            error?.let { item { Text(it, color = Danger, fontSize = 13.sp) } }
        }
        if (chips.isNotEmpty()) {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach { c -> FilterChip(false, { send(c) }, { Text(c, fontSize = 12.sp) }, colors = chipColors2()) }
            }
        }
        Row(Modifier.background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { Field("Ask about your claim or money…", input) { input = it } }
            TextButton({ send(input) }, enabled = input.isNotBlank() && !sending) { Text("Send", color = Primary, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun FinanceCard(card: ChatCard) {
    AppCard(Modifier.widthIn(max = 340.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(card.title ?: card.type, color = Navy, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            StatusChip("Demo", Muted)
        }
        when (card.type) {
            "accounts" -> {
                card.accounts.forEach { a -> KeyRow("${a.bankName} ${a.maskedNumber}", inrD(a.balance)) }
                KeyRow("Total balance", inrD(card.total))
            }
            "expenses" -> {
                val max = card.categories.maxOfOrNull { it.amount }?.takeIf { it > 0 } ?: 1.0
                card.categories.forEach { c ->
                    KeyRow(c.category, inrD(c.amount))
                    LinearProgressIndicator(progress = { (c.amount / max).toFloat() }, modifier = Modifier.fillMaxWidth().height(5.dp), color = Primary, trackColor = Pale)
                }
                KeyRow("Total spent", inrD(card.total))
            }
            "medical" -> {
                KeyRow("Hospital bills", inrD(card.totalMedicalSpend))
                KeyRow("Insurer paid", inrD(card.insurerPaid))
                KeyRow("Out of pocket", inrD(card.outOfPocket))
                LinearProgressIndicator(progress = { ((card.insurerPaidPercent ?: 0.0) / 100).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Success, trackColor = Pale)
                Text("${(card.insurerPaidPercent ?: 0.0).toInt()}% covered by insurance", color = Muted, fontSize = 12.sp)
            }
            "payouts" -> {
                if (card.payouts.isEmpty()) Text("No payouts yet", color = Muted, fontSize = 13.sp)
                card.payouts.forEach { p -> KeyRow("${p.claimNumber} ${p.paidAt?.take(10).orEmpty()}", inrD(p.amount)); p.utr?.let { Text("UTR $it", color = Muted, fontSize = 11.sp) } }
                KeyRow("Total received", inrD(card.total))
            }
        }
    }
}

@Composable
fun FinanceSection() {
    var finance by remember { mutableStateOf<Finance?>(null) }
    LaunchedEffect(Unit) { runCatching { Network.api.finance() }.onSuccess { finance = it } }
    finance?.let { f ->
        Text("Money", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        f.note?.let { Text(it, color = Muted, fontSize = 12.sp) }
        FinanceCard(ChatCard("accounts", "Linked bank accounts", f.totalBalance, accounts = f.accounts))
        FinanceCard(ChatCard("expenses", "Expenses · ${f.monthlyExpenses.label.orEmpty()}", f.monthlyExpenses.total, categories = f.monthlyExpenses.categories))
        FinanceCard(ChatCard("medical", "Medical spend", totalMedicalSpend = f.medical.totalMedicalSpend, insurerPaid = f.medical.insurerPaid, outOfPocket = f.medical.outOfPocket, insurerPaidPercent = f.medical.insurerPaidPercent))
        FinanceCard(ChatCard("payouts", "Claim payouts received", f.totalPayouts, payouts = f.payouts))
    }
}
