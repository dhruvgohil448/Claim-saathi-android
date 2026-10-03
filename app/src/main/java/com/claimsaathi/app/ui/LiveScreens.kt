package com.claimsaathi.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claimsaathi.app.BuildConfig
import com.claimsaathi.app.data.AddPolicyBody
import com.claimsaathi.app.data.BankBody
import com.claimsaathi.app.data.ChatBody
import com.claimsaathi.app.data.ChatReply
import com.claimsaathi.app.data.Checklist
import com.claimsaathi.app.data.ClaimQuery
import com.claimsaathi.app.data.CreateClaimBody
import com.claimsaathi.app.data.DocumentType
import com.claimsaathi.app.data.Network
import com.claimsaathi.app.data.PatientDetails
import com.claimsaathi.app.data.PhoneBody
import com.claimsaathi.app.data.Policy
import com.claimsaathi.app.data.PolicyAnalysis
import com.claimsaathi.app.data.ProfileBody
import com.claimsaathi.app.data.ProfilePatch
import com.claimsaathi.app.data.QueryExplain
import com.claimsaathi.app.data.Settlement
import com.claimsaathi.app.data.StepState
import com.claimsaathi.app.data.Timeline
import com.claimsaathi.app.data.TokenStore
import com.claimsaathi.app.data.UploadResponse
import com.claimsaathi.app.data.VerifyBody
import kotlinx.coroutines.delay

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Pale,
    selectedLabelColor = Navy,
    containerColor = Color.White,
    labelColor = Muted
)

@Composable
fun LoginScreen(vm: AppVm, onSent: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    AppScreen(hero = { NavyHero("Claim Saathi", "Your AI health-insurance companion") }, safeBottom = true) {
        AppCard {
            Text("Login with mobile", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Use the number on your policy. No SMS is sent in demo.", color = Muted, fontSize = 13.sp)
            Field("10-digit phone", phone, keyboard = KeyboardType.Phone) { phone = it.filter(Char::isDigit).take(10) }
            ErrorText(vm.error)
            PrimaryButton("Get OTP", phone.length == 10, vm.busy) {
                vm.work {
                    Network.api.sendOtp(PhoneBody(phone))
                    onSent(phone)
                }
            }
        }
        Text("Demo OTP is always 111000", color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun OtpScreen(vm: AppVm, phone: String, onBack: () -> Unit, onProfile: () -> Unit, onHome: () -> Unit) {
    var otp by remember { mutableStateOf("") }
    AppScreen(hero = { NavyHero("Verify OTP", "Sent to $phone") { StepDots(1, 3) } }, safeBottom = true) {
        TopBar("Back", onBack)
        AppCard {
            Text("Enter the 6-digit code", color = Navy, fontWeight = FontWeight.Bold)
            Text("Demo code: 111000", color = Muted, fontSize = 13.sp)
            OtpBoxes(otp) { otp = it }
            ErrorText(vm.error)
            PrimaryButton("Verify & continue", otp.length == 6, vm.busy) {
                vm.work {
                    val res = Network.api.verifyOtp(VerifyBody(phone, otp))
                    vm.signIn(res.token, res.user)
                    if (res.needsProfile) onProfile() else onHome()
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(vm: AppVm, onDone: () -> Unit) {
    var name by remember { mutableStateOf(vm.user?.name?.takeIf { it != "New user" }.orEmpty()) }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1990-01-01") }
    var gender by remember { mutableStateOf("male") }
    var city by remember { mutableStateOf("") }
    AppScreen(hero = { NavyHero("Almost there", "Complete your profile to start a claim") { StepDots(2, 3) } }, safeBottom = true) {
        AppCard {
            Field("Full name", name) { name = it }
            Field("Email", email, keyboard = KeyboardType.Email) { email = it }
            Field("Date of birth (yyyy-MM-dd)", dob) { dob = it }
            Text("Gender", color = Muted, fontSize = 12.sp)
            ChipRow {
                listOf("male", "female", "other").forEach { option ->
                    FilterChip(gender == option, { gender = option }, { Text(option.replaceFirstChar { it.titlecase() }) }, colors = chipColors())
                }
            }
            Field("City", city) { city = it }
            ErrorText(vm.error)
            PrimaryButton("Save & enter app", name.length >= 2 && email.contains("@"), vm.busy) {
                vm.work {
                    val res = Network.api.putProfile(ProfileBody(name.trim(), email.trim(), dob.trim(), gender, city.trim().ifBlank { null }))
                    vm.replaceToken(res.token, res.user)
                    onDone()
                }
            }
        }
    }
}

@Composable
fun HomeTab(
    vm: AppVm,
    openClaim: (String) -> Unit,
    openQuery: (String) -> Unit,
    openChecklist: (String) -> Unit,
    openPolicy: (String) -> Unit,
    addPolicy: () -> Unit,
    openBank: () -> Unit,
    openProfile: () -> Unit,
    startClaim: () -> Unit
) {
    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshHome()
            delay(5_000)
        }
    }
    val home = vm.home
    AppScreen(
        safeBottom = false,
        hero = {
            Column(
                Modifier.fillMaxWidth().background(NavyBrush).statusBarsPadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Good to see you", color = Color.White.copy(0.72f), fontSize = 13.sp)
                Text(
                    home?.user?.name ?: vm.user?.name ?: "Claim Saathi",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(Modifier.fillMaxWidth()) {
                    QuickStat("Paid out", inr(home?.paidOut))
                    QuickStat("Open claims", "${home?.counts?.activeClaims ?: 0}")
                    QuickStat("Queries", "${home?.counts?.openQueries ?: 0}")
                }
            }
        }
    ) {
            home?.activePolicy?.let { policy ->
                AppCard(Modifier.clickable { openPolicy(policy.id) }) {
                    Text("ACTIVE POLICY", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(policy.insurer, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(policy.policyNumber, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(Modifier.fillMaxWidth()) {
                        QuickMetric("${inr(policy.sumInsured)} cover")
                        QuickMetric("${inr(policy.roomRentLimit)}/day")
                        QuickMetric("${policy.coPayPercent.toInt()}% copay")
                    }
                }
            }
            home?.currentClaim?.let { claim ->
                AppCard(Modifier.clickable { openClaim(claim.id.ifBlank { claim.claimNumber }) }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(claim.claimNumber, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        StatusChip(claim.status.name, statusColor(claim.status))
                    }
                    Text(claim.hospital, color = Navy, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val required = claim.checklist?.required?.size ?: 0
                    val verified = claim.checklist?.verified?.size ?: 0
                    if (required > 0) {
                        Text("$verified of $required documents verified", color = Muted, fontSize = 13.sp)
                        ProgressLine(verified, required)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(Icons.Outlined.AddCircle, "Start claim", "File or pre-auth", Modifier.weight(1f), startClaim)
                ActionTile(Icons.Outlined.Policy, "Link policy", "Add cover", Modifier.weight(1f), addPolicy)
                ActionTile(Icons.Outlined.AccountBalance, "Bank", "Payout account", Modifier.weight(1f), openBank)
            }
            Text("Needs you", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (home?.pendingActions.isNullOrEmpty()) {
                AppCard { Text("You’re all caught up.", color = Muted) }
            }
            home?.pendingActions?.forEach { action ->
                AppCard(Modifier.clickable {
                    when (action.kind) {
                        "QUERY" -> action.queryId?.let(openQuery)
                        "MISSING_DOC", "REUPLOAD_DOC" -> action.claimId?.let(openChecklist)
                        "LINK_POLICY" -> addPolicy()
                        "ADD_BANK" -> openBank()
                        "COMPLETE_PROFILE" -> openProfile()
                    }
                }) {
                    Text(action.kind.replace('_', ' '), color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(action.title, color = Navy, fontWeight = FontWeight.SemiBold)
                }
            }
            ErrorText(vm.error)
    }
}

@Composable
fun AddPolicyScreen(vm: AppVm, onBack: () -> Unit, onSaved: (String) -> Unit) {
    var insurer by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var sum by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("2026-01-01") }
    var room by remember { mutableStateOf("5000") }
    var copay by remember { mutableStateOf("10") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        fileUri = uri
        fileName = uri?.let { picked ->
            context.contentResolver.query(picked, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "policy.pdf"
        }
    }
    FadeInColumn {
        TopBar("Link a policy", onBack)
        ScreenTitle("Add your cover", "Fill the details and attach the policy PDF. The file is stored and shown on the dashboard.")
        AppCard {
            Field("Insurer", insurer) { insurer = it }
            Field("Policy number", number) { number = it }
            Field("Sum insured", sum, keyboard = KeyboardType.Number) { sum = it.filter(Char::isDigit) }
            Field("Start date", start) { start = it }
            Field("Room rent / day", room, keyboard = KeyboardType.Number) { room = it.filter(Char::isDigit) }
            Field("Co-pay %", copay, keyboard = KeyboardType.Number) { copay = it.filter { ch -> ch.isDigit() } }
            GhostButton(if (fileName == null) "Upload policy document" else "Change document") {
                picker.launch(arrayOf("application/pdf", "image/*"))
            }
            fileName?.let { Text("Attached: $it", color = Success, fontSize = 13.sp) }
            ErrorText(vm.error)
            PrimaryButton("Save policy", insurer.length >= 2 && number.length >= 3, vm.busy) {
                vm.work {
                    val saved = if (fileUri != null) {
                        val fields = buildMap {
                            put("insurer", insurer.trim().plainBody())
                            put("policyNumber", number.trim().plainBody())
                            put("sumInsured", (sum.toIntOrNull() ?: 0).toString().plainBody())
                            put("startDate", start.plainBody())
                            room.toIntOrNull()?.let { put("roomRentLimit", it.toString().plainBody()) }
                            copay.toDoubleOrNull()?.let { put("coPayPercent", it.toString().plainBody()) }
                        }
                        Network.api.addPolicyPdf(fields, context.filePart(fileUri!!))
                    } else {
                        Network.api.addPolicy(
                            AddPolicyBody(insurer.trim(), number.trim(), sum.toIntOrNull() ?: 0, start, roomRentLimit = room.toIntOrNull(), coPayPercent = copay.toDoubleOrNull())
                        )
                    }
                    onSaved(saved.policy.id)
                }
            }
        }
    }
}

@Composable
fun PolicyReaderScreen(policyId: String, onBack: () -> Unit, askAssistant: () -> Unit) {
    var analysis by remember { mutableStateOf<PolicyAnalysis?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(policyId) {
        runCatching { Network.api.analyze(policyId) }.onSuccess { analysis = it }.onFailure { error = Network.apiMessage(it) }
    }
    FadeInColumn {
        TopBar("Policy reader", onBack)
        if (analysis == null && error == null) CircularProgressIndicator(color = Primary)
        analysis?.let {
            ScreenTitle(it.insurer, it.policyNumber)
            AppCard { Text(it.whatIsCovered, color = Navy, fontSize = 15.sp) }
            Text("What’s covered", color = Navy, fontWeight = FontWeight.Bold)
            it.coverage.forEach { row ->
                AppCard {
                    Text(row.item, fontWeight = FontWeight.SemiBold, color = Navy)
                    Text(row.detail, color = Muted, fontSize = 13.sp)
                }
            }
            if (it.exclusions.isNotEmpty()) {
                AppCard {
                    Text("Not covered", color = Navy, fontWeight = FontWeight.Bold)
                    it.exclusions.forEach { line -> Text("· $line", color = Muted, fontSize = 13.sp) }
                }
            }
            if (it.waitingPeriods.isNotEmpty()) {
                AppCard {
                    Text("Waiting periods", color = Navy, fontWeight = FontWeight.Bold)
                    it.waitingPeriods.forEach { wait -> StatusChip(wait.status.ifBlank { wait.name }, if (wait.active) Warning else Success) }
                }
            }
            GhostButton("Ask assistant") { askAssistant() }
        }
        ErrorText(error)
    }
}

@Composable
fun StartClaimScreen(vm: AppVm, onBack: () -> Unit, onCreated: (String) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var policies by remember { mutableStateOf<List<Policy>>(emptyList()) }
    var policyId by remember { mutableStateOf("") }
    var cashless by remember { mutableStateOf(false) }
    var hospital by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var patient by remember { mutableStateOf(vm.user?.name ?: "") }
    var amount by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("111000") }
    var warnings by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        runCatching { Network.api.policies() }.onSuccess {
            policies = it
            policyId = it.firstOrNull()?.id ?: ""
            if (patient.isBlank()) patient = it.firstOrNull()?.members?.firstOrNull()?.name ?: vm.user?.name.orEmpty()
        }
    }
    FadeInColumn {
        TopBar("Start a claim", onBack)
        StepDots(step, 3)
        AnimatedContent(step, label = "claim-step") { current ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (current) {
                    0 -> {
                        ScreenTitle("How should we file this?")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(!cashless, { cashless = false }, { Text("Reimbursement") }, colors = chipColors())
                            FilterChip(cashless, { cashless = true }, { Text("Pre-auth") }, colors = chipColors())
                        }
                        policies.forEach { policy ->
                            AppCard(Modifier.clickable { policyId = policy.id }) {
                                Text(policy.policyNumber, color = if (policyId == policy.id) Primary else Navy, fontWeight = FontWeight.Bold)
                                Text(policy.insurer, color = Muted, fontSize = 13.sp)
                            }
                        }
                        PrimaryButton("Next", policyId.isNotBlank()) { step = 1 }
                    }
                    1 -> {
                        ScreenTitle("Hospital & patient")
                        Field("Hospital", hospital) { hospital = it }
                        Field("City", city) { city = it }
                        Field("Reason", reason) { reason = it }
                        Field("Patient", patient) { patient = it }
                        Field(if (cashless) "Estimated amount" else "Bill amount", amount, keyboard = KeyboardType.Number) { amount = it.filter(Char::isDigit) }
                        PrimaryButton("Review", hospital.length >= 2 && reason.length >= 2) { step = 2 }
                        GhostButton("Back") { step = 0 }
                    }
                    else -> {
                        ScreenTitle("Confirm & consent")
                        AppCard {
                            KeyRow("Type", if (cashless) "Pre-auth" else "Reimbursement")
                            KeyRow("Hospital", hospital)
                            KeyRow("Patient", patient)
                            KeyRow("Amount", inr(amount.toIntOrNull()))
                        }
                        warnings.forEach { Text(it, color = Warning, fontSize = 13.sp) }
                        Text("Consent OTP", color = Muted, fontSize = 12.sp)
                        OtpBoxes(otp) { otp = it }
                        ErrorText(vm.error)
                        GhostButton("Check coverage") {
                            vm.work {
                                warnings = Network.api.checkCoverage(claimBody(policyId, cashless, hospital, city, reason, patient, amount, otp)).warnings
                            }
                        }
                        PrimaryButton("Submit claim", otp.length == 6, vm.busy) {
                            vm.work {
                                val created = Network.api.createClaim(claimBody(policyId, cashless, hospital, city, reason, patient, amount, otp))
                                if (cashless) runCatching { Network.api.preauth(created.id) }
                                onCreated(created.id)
                            }
                        }
                        TextButton({ step = 1 }) { Text("Edit details", color = Navy) }
                    }
                }
            }
        }
    }
}

private fun claimBody(policyId: String, cashless: Boolean, hospital: String, city: String, reason: String, patient: String, amount: String, otp: String) =
    CreateClaimBody(
        policyId = policyId,
        type = if (cashless) "PREAUTH" else "REIMBURSEMENT",
        hospital = hospital.trim(),
        hospitalCity = city.trim().ifBlank { null },
        reason = reason.trim(),
        admissionType = "EMERGENCY",
        billAmount = if (cashless) null else amount.toIntOrNull(),
        estimatedAmount = if (cashless) amount.toIntOrNull() else null,
        patientName = patient.trim(),
        patientDetails = PatientDetails(relation = "Self"),
        consentOtp = otp
    )

@Composable
fun ChecklistScreen(vm: AppVm, claimId: String, onBack: () -> Unit, onUploaded: () -> Unit) {
    var list by remember { mutableStateOf<Checklist?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var validating by remember { mutableStateOf(false) }
    var pendingType by remember { mutableStateOf<DocumentType?>(null) }
    val context = LocalContext.current
    suspend fun load() { list = Network.api.checklist(claimId) }
    LaunchedEffect(claimId) { runCatching { load() }.onFailure { error = Network.apiMessage(it) } }
    fun upload(uri: Uri?) {
        val type = pendingType ?: return
        if (uri == null) return
        validating = true
        vm.work {
            vm.lastUpload = Network.api.upload(claimId, context.filePart(uri), type.name.plainBody())
            validating = false
            onUploaded()
        }
    }
    val files = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { upload(it) }
    val photos = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { upload(it) }
    Box(Modifier.fillMaxSize()) {
        FadeInColumn {
            TopBar("Documents", onBack)
            ScreenTitle("Upload bills", "Camera, gallery or PDF. Validation finishes before you move on.")
            list?.let {
                ProgressLine(it.progress.verified, it.progress.required)
                Text("${it.progress.verified}/${it.progress.required} verified", color = Muted, fontSize = 13.sp)
                it.warnings.forEach { warning -> Text(warning, color = Warning, fontSize = 13.sp) }
                it.items.forEach { item ->
                    AppCard(Modifier.clickable { pendingType = item.type }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(item.label, color = Navy, fontWeight = FontWeight.SemiBold)
                            StatusChip(item.status.name, docColor(item.status))
                        }
                        item.fileName?.let { name -> Text(name, color = Muted, fontSize = 12.sp) }
                        item.fix?.let { fix -> Text(fix, color = Danger, fontSize = 13.sp) }
                        if (pendingType == item.type) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton({ photos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Gallery", color = Primary) }
                                TextButton({ files.launch(arrayOf("application/pdf", "image/*")) }) { Text("PDF / files", color = Navy) }
                            }
                        }
                    }
                }
            }
            ErrorText(error ?: vm.error)
        }
        LoadingScrim(validating || vm.busy, "Validating document…")
    }
}

@Composable
fun ValidationScreen(upload: UploadResponse?, onNext: () -> Unit, onTrack: () -> Unit) {
    val result = upload
    FadeInColumn {
        ScreenTitle(prettyStatus(result?.validation?.appStatus?.name ?: "Validation"), result?.let { "${(it.validation.confidence * 100).toInt()}% confidence" })
        if (result == null) {
            AppCard { Text("No upload yet.", color = Muted) }
            return@FadeInColumn
        }
        result.validation.summary?.let { AppCard { Text(it, color = Navy) } }
        result.validation.checks.forEach { check ->
            val color = when (check.passed) { true -> Success; false -> Danger; null -> Muted }
            val mark = when (check.passed) { true -> "✓"; false -> "✗"; null -> "–" }
            AppCard {
                Text("$mark  ${check.label}", color = color, fontWeight = FontWeight.Bold)
                Text(check.detail, color = Muted, fontSize = 13.sp)
            }
        }
        result.validation.warnings.forEach { Text(it, color = Warning, fontSize = 13.sp) }
        result.validation.fix?.let { Text(it, color = Danger) }
        PrimaryButton("Upload next", onClick = onNext)
        GhostButton("Track claim", onClick = onTrack)
    }
}

@Composable
fun ClaimsTab(vm: AppVm, onOpen: (String) -> Unit) {
    var filter by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(filter) {
        runCatching { Network.api.claims(filter) }.onSuccess { vm.claims = it }.onFailure { vm.error = Network.apiMessage(it) }
    }
    FadeInColumn(safeBottom = false) {
        ScreenTitle("My claims", "Tap a card to track every step.")
        ChipRow {
            FilterChip(filter == null, { filter = null }, { Text("All") }, colors = chipColors())
            FilterChip(filter == "QUERY_RAISED,DOCS_PENDING", { filter = "QUERY_RAISED,DOCS_PENDING" }, { Text("Action") }, colors = chipColors())
            FilterChip(filter == "SETTLED", { filter = "SETTLED" }, { Text("Settled") }, colors = chipColors())
        }
        if (vm.claims.isEmpty()) AppCard { Text("No claims in this filter.", color = Muted) }
        vm.claims.forEach { claim ->
            AppCard(Modifier.clickable { onOpen(claim.id) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(claim.claimNumber, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    StatusChip(claim.status.name, statusColor(claim.status))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocalHospital, null, tint = Navy, modifier = Modifier.size(16.dp))
                    Text(claim.hospital, color = Navy, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(inr(claim.billAmount ?: claim.estimatedAmount), fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
            }
        }
        ErrorText(vm.error)
    }
}

@Composable
fun TrackingScreen(claimId: String, onBack: () -> Unit, onQuery: () -> Unit, onSettlement: () -> Unit, onDocs: () -> Unit) {
    var timeline by remember { mutableStateOf<Timeline?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(claimId) {
        while (true) {
            runCatching { Network.api.timeline(claimId) }.onSuccess { timeline = it; error = null }.onFailure { error = Network.apiMessage(it) }
            delay(5_000)
        }
    }
    val item = timeline
    FadeInColumn {
        TopBar("Claim tracking", onBack)
        ScreenTitle(item?.claimNumber ?: "Claim", item?.status?.name?.let(::prettyStatus))
        item?.latestOpsUpdate?.let { update ->
            AppCard {
                Text("LATEST UPDATE", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(update.message, color = Navy)
            }
        }
        AppCard {
            item?.steps?.forEach { step ->
                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    if (step.state == StepState.current) PulseDot(Primary)
                    else Box(Modifier.size(14.dp).clip(CircleShape).background(stepColor(step.state)))
                    Column {
                        Text(step.label, color = Navy, fontWeight = FontWeight.SemiBold)
                        step.note?.let { Text(it, color = Muted, fontSize = 13.sp) }
                    }
                }
            }
        }
        if (!item?.openQueries.isNullOrEmpty()) PrimaryButton("Answer queries", onClick = onQuery)
        GhostButton("Upload documents", onClick = onDocs)
        GhostButton("Settlement", onClick = onSettlement)
        ErrorText(error)
    }
}

@Composable
fun QueriesScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    var items by remember { mutableStateOf<List<ClaimQuery>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching { Network.api.queries("OPEN") }.onSuccess { items = it }.onFailure { error = Network.apiMessage(it) }
    }
    FadeInColumn {
        TopBar("Queries", onBack)
        ScreenTitle("Ops needs you", "Reply with a note or the requested file.")
        if (items.isEmpty()) AppCard { Text("No open queries.", color = Muted) }
        items.forEach { query ->
            AppCard(Modifier.clickable { onOpen(query.id) }) {
                Text(query.claim?.claimNumber ?: "Query", color = Primary, fontWeight = FontWeight.Bold)
                Text(query.message, color = Navy)
                query.requestedDocType?.let { StatusChip(it.name, Warning) }
            }
        }
        ErrorText(error)
    }
}

@Composable
fun QueryDetailScreen(vm: AppVm, queryId: String, onBack: () -> Unit) {
    var query by remember { mutableStateOf<ClaimQuery?>(null) }
    var explain by remember { mutableStateOf<QueryExplain?>(null) }
    var reply by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<UploadResponse?>(null) }
    val context = LocalContext.current
    LaunchedEffect(queryId) {
        runCatching { Network.api.explainQuery(queryId) }.onSuccess { explain = it }
        runCatching { Network.api.queries(null) }.onSuccess { query = it.find { q -> q.id == queryId } }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        vm.work {
            val answered = Network.api.respond(queryId, reply.ifBlank { "Uploaded the requested file" }.plainBody(), context.filePart(uri), query?.requestedDocType?.name?.plainBody())
            query = answered
            result = answered.document
        }
    }
    FadeInColumn {
        TopBar("Query", onBack)
        AppCard { Text(query?.message ?: "Loading…", color = Navy) }
        explain?.explanation?.let { AppCard { Text(it, color = Navy) } }
        Field("Your reply", reply) { reply = it }
        PrimaryButton("Send reply", loading = vm.busy) {
            vm.work { query = Network.api.respond(queryId, reply.ifBlank { "Replied from the app" }.plainBody(), null, null) }
        }
        GhostButton("Attach a file") { picker.launch(arrayOf("application/pdf", "image/*")) }
        if (query?.status?.name == "CLOSED") Text("Query resolved ✓", color = Success, fontWeight = FontWeight.Bold)
        result?.validation?.checks?.forEach { check ->
            AppCard { Text("${check.label}: ${check.detail}", color = Muted, fontSize = 13.sp) }
        }
        ErrorText(vm.error)
    }
}

@Composable
fun AssistantTab(vm: AppVm) {
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Pair<Boolean, String>>()) }
    var followUps by remember { mutableStateOf(listOf<String>()) }
    val starters = listOf("Where is my claim?", "Which documents are still pending?", "What is my room rent limit?", "How much will I get?", "What is not covered?")
    val listState = rememberLazyListState()
    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val claimId = vm.home?.currentClaim?.id?.ifBlank { vm.home?.currentClaim?.claimNumber }
        messages = messages + (true to trimmed)
        input = ""
        vm.work {
            val reply: ChatReply = Network.api.chat(ChatBody(trimmed, claimId))
            messages = messages + (false to reply.answer)
            followUps = reply.followUps
        }
    }
    Column(Modifier.fillMaxSize().background(AppBackground).imePadding()) {
        Column(Modifier.fillMaxWidth().background(NavyBrush).statusBarsPadding().padding(20.dp)) {
            Text("Ask Saathi", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Answers stay grounded in your policy and claim.", color = Color.White.copy(0.75f), fontSize = 13.sp)
        }
        LazyColumn(Modifier.weight(1f).padding(16.dp), listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (messages.isEmpty()) {
                item { Text("Try a starter", color = Muted, fontSize = 13.sp) }
                items(starters) { prompt ->
                    AppCard(Modifier.clickable { send(prompt) }) { Text(prompt, color = Navy) }
                }
            }
            items(messages.size) { index ->
                val (mine, text) = messages[index]
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text,
                        color = if (mine) Color.White else Navy,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (mine) Navy else Color.White)
                            .padding(14.dp)
                            .animateContentSize()
                    )
                }
            }
            items(followUps) { prompt ->
                TextButton({ send(prompt) }) { Text(prompt, color = Primary) }
            }
        }
        Column(Modifier.background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Field("Message", input) { input = it }
            PrimaryButton("Send", !vm.busy && input.isNotBlank(), vm.busy) { send(input) }
            ErrorText(vm.error)
        }
    }
}

@Composable
fun SettlementScreen(claimId: String, onBack: () -> Unit) {
    var settlement by remember { mutableStateOf<Settlement?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(claimId) {
        runCatching { Network.api.settlement(claimId) }.onSuccess { settlement = it }.onFailure { error = Network.apiMessage(it) }
    }
    val item = settlement
    FadeInColumn {
        TopBar("Settlement", onBack)
        ScreenTitle(if (item?.preview == true) "Estimate" else "Payout", if (item?.isDemo == true) "Demo settlement" else null)
        item?.let {
            AppCard {
                KeyRow("Hospital bill", inr(it.billAmount))
                it.deductions.forEach { line ->
                    KeyRow(line.label, "− ${inr(line.amount)}")
                    line.reason?.let { reason -> Text(reason, color = Muted, fontSize = 12.sp) }
                }
                KeyRow("Co-pay", inr(it.coPayAmount))
                Text(inr(it.approvedAmount), color = Success, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                it.utr?.let { utr -> Text("UTR $utr", color = Navy) }
            }
        }
        GhostButton("Open summary PDF") {
            val url = BuildConfig.API_BASE_URL + "claims/$claimId/summary.pdf?token=${TokenStore.token}"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        ErrorText(error)
    }
}

@Composable
fun AlertsTab(vm: AppVm, onOpenClaim: (String) -> Unit) {
    var notes by remember { mutableStateOf(listOf<com.claimsaathi.app.data.AppNotification>()) }
    LaunchedEffect(Unit) {
        while (true) {
            runCatching { Network.api.notifications() }.onSuccess {
                notes = it.items
                vm.unread = it.unread
            }
            delay(5_000)
        }
    }
    FadeInColumn(safeBottom = false) {
        ScreenTitle("Alerts", "Live updates from ops and the claim agent.")
        TextButton({
            vm.work {
                Network.api.markAllRead()
                vm.unread = 0
                notes = notes.map { it.copy(read = true) }
            }
        }) { Text("Mark all read", color = Navy) }
        if (notes.isEmpty()) AppCard { Text("No alerts yet.", color = Muted) }
        notes.forEach { note ->
            AppCard(Modifier.clickable {
                vm.work { Network.api.markRead(note.id) }
                note.claimId?.let(onOpenClaim)
            }) {
                Text(note.title, color = Navy, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(note.body, color = Muted, fontSize = 13.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ProfileTab(vm: AppVm, onBank: () -> Unit, onLoggedOut: () -> Unit) {
    var name by remember { mutableStateOf(vm.user?.name ?: "") }
    var city by remember { mutableStateOf(vm.user?.city ?: "") }
    AppScreen(
        safeBottom = false,
        hero = { NavyHero(vm.user?.name ?: "Profile", vm.user?.phone ?: vm.user?.email.orEmpty()) }
    ) {
        AppCard {
            Field("Name", name) { name = it }
            Field("City", city) { city = it }
            ErrorText(vm.error)
            PrimaryButton("Save profile", !vm.busy, vm.busy) {
                vm.work {
                    val res = Network.api.patchProfile(ProfilePatch(name = name.trim(), city = city.trim()))
                    vm.replaceToken(res.token, res.user)
                }
            }
        }
        ActionTile(Icons.Outlined.AccountBalance, "Bank account", "For settlement payouts", Modifier.fillMaxWidth(), onBank)
        GhostButton("Log out") {
            vm.logout()
            onLoggedOut()
        }
    }
}

@Composable
fun BankScreen(vm: AppVm, onBack: () -> Unit, onSaved: () -> Unit) {
    var holder by remember { mutableStateOf(vm.user?.name ?: "") }
    var account by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    FadeInColumn {
        TopBar("Bank account", onBack)
        ScreenTitle("Payout account", "Verified with demo OTP 111000. Stored masked.")
        AppCard {
            Field("Account name", holder) { holder = it }
            Field("Account number", account, keyboard = KeyboardType.Number) { account = it.filter(Char::isDigit) }
            Field("IFSC", ifsc) { ifsc = it.uppercase() }
            Field("Bank name", bank) { bank = it }
            ErrorText(vm.error)
            PrimaryButton("Save bank", account.length >= 8 && ifsc.length >= 8, vm.busy) {
                vm.work {
                    Network.api.saveBank(BankBody(holder.trim(), account.trim(), ifsc.trim(), bank.trim().ifBlank { null }, "111000"))
                    onSaved()
                }
            }
        }
    }
}
