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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
    startClaim: () -> Unit,
    askSaathi: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshHome()
            delay(5_000)
        }
    }
    val home = vm.home
    Box(Modifier.fillMaxSize()) {
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
            if (home == null) {
                SkeletonCard(2); SkeletonCard(3); SkeletonCard(1)
            } else if (home.activePolicy == null) {
                SaathiTip("Start by linking your health policy — I’ll explain your cover, room-rent limit and co-pay in simple words.")
            } else if (home.currentClaim == null) {
                SaathiTip("Your policy is linked. Tap “Start claim” — I’ll warn you about co-pay and room-rent limits before you submit.")
            }
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
                    Text("${if (claim.claimType.name == "CASHLESS") "Cashless pre-auth" else "Reimbursement"} · ${inr(claim.billAmount ?: claim.estimatedAmount)}${if (claim.isTemplate) " · sample" else ""}", color = Muted, fontSize = 13.sp)
                    val required = claim.checklist?.required?.size ?: 0
                    val verified = claim.checklist?.verified?.size ?: 0
                    if (required > 0) UploadProgressBar(verified, required)
                }
            }
            WarningList(home?.warnings.orEmpty())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(Icons.Outlined.AddCircle, "Start claim", "File or pre-auth", Modifier.weight(1f), startClaim)
                ActionTile(Icons.Outlined.Policy, "Link policy", "Add cover", Modifier.weight(1f), addPolicy)
                ActionTile(Icons.Outlined.AccountBalance, "Bank", "Payout account", Modifier.weight(1f), openBank)
            }
            Text("Needs you", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (home != null && home.pendingActions.isNullOrEmpty()) {
                AppCard { EmptyState(Icons.Outlined.CheckCircle, "You’re all caught up", "No pending actions. We’ll alert you when something needs you.") }
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
                    val tint = if (action.kind == "QUERY") Warning else Primary
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(when (action.kind) { "QUERY" -> Icons.Outlined.HelpOutline; "MISSING_DOC", "REUPLOAD_DOC" -> Icons.Outlined.UploadFile; "ADD_BANK" -> Icons.Outlined.AccountBalance; "COMPLETE_PROFILE" -> Icons.Outlined.PersonOutline; else -> Icons.Outlined.Description }, null, tint = tint, modifier = Modifier.size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(action.kind.replace('_', ' '), color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(action.title, color = Navy, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = Muted)
                    }
                }
            }
            ErrorText(vm.error)
            Spacer(Modifier.height(64.dp))
    }
    SaathiFab { askSaathi("Track my claim and tell me what to do next") }
    }
}

@Composable
fun AddPolicyScreen(vm: AppVm, onBack: () -> Unit, onSaved: (String) -> Unit) {
    var insurer by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var sum by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("2026-01-01") }
    var room by remember { mutableStateOf("4000") }
    var copay by remember { mutableStateOf("10") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var filePartPicked by remember { mutableStateOf<okhttp3.MultipartBody.Part?>(null) }
    var showSources by remember { mutableStateOf(false) }
    val sources = rememberUploadSources("policy", onPart = { part ->
        filePartPicked = part
        fileName = part.headers?.get("Content-Disposition")?.substringAfter("filename=\"")?.substringBefore("\"") ?: "policy document"
    }, onError = { vm.error = it })
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
                showSources = !showSources
            }
            if (showSources) UploadSourceRow(sources)
            fileName?.let { Text("Attached: $it", color = Success, fontSize = 13.sp) }
            ErrorText(vm.error)
            PrimaryButton("Save policy", true, vm.busy) {
                vm.work {
                    val saved = if (filePartPicked != null) {
                        val fields = buildMap {
                            put("insurer", insurer.trim().plainBody())
                            put("policyNumber", number.trim().plainBody())
                            put("sumInsured", (sum.toIntOrNull() ?: 0).toString().plainBody())
                            put("startDate", start.plainBody())
                            room.toIntOrNull()?.let { put("roomRentLimit", it.toString().plainBody()) }
                            copay.toDoubleOrNull()?.let { put("coPayPercent", it.toString().plainBody()) }
                        }
                        Network.api.addPolicyPdf(fields, filePartPicked!!)
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
fun ChecklistScreen(vm: AppVm, claimId: String, onBack: () -> Unit, onUploaded: () -> Unit, askSaathi: (String) -> Unit = {}) {
    var list by remember { mutableStateOf<Checklist?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var validating by remember { mutableStateOf(false) }
    var pendingType by remember { mutableStateOf<DocumentType?>(null) }
    val context = LocalContext.current
    suspend fun load() { list = Network.api.checklist(claimId) }
    LaunchedEffect(claimId) { runCatching { load() }.onFailure { error = Network.apiMessage(it) } }
    val sources = rememberUploadSources(pendingType?.name?.lowercase() ?: "document", onPart = { part ->
        val type = pendingType ?: return@rememberUploadSources
        validating = true
        vm.work {
            try {
                vm.lastUpload = Network.api.upload(claimId, part, type.name.plainBody())
                load()
                onUploaded()
            } finally { validating = false }
        }
    }, onError = { error = it })
    Box(Modifier.fillMaxSize()) {
        FadeInColumn {
            TopBar("Documents", onBack)
            ScreenTitle("Upload bills", "Camera, gallery or PDF. Validation finishes before you move on.")
            if (list == null && error == null) { SkeletonCard(1); SkeletonCard(1); SkeletonCard(1) }
            list?.let {
                AppCard { UploadProgressBar(it.progress.verified, it.progress.required) }
                if (it.progress.verified >= 6) PredictionCard(claimId, it.progress.verified)
                val next = it.items.firstOrNull { item -> item.status.name != "verified" }
                if (next != null) SaathiTip("Next up: ${next.label}. Tap it, then take a photo or pick a file — I’ll check it instantly.${next.fix?.let { f -> " Tip: $f" } ?: ""}")
                else if (it.items.isNotEmpty()) SaathiTip("All documents verified. The claims team is reviewing — you’ll get an alert for any query.")
                it.warnings.forEach { warning -> Text(warning, color = Warning, fontSize = 13.sp) }
                it.items.forEach { item ->
                    AppCard(Modifier.clickable { pendingType = item.type }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(when (item.status.name) { "verified" -> Icons.Outlined.CheckCircle; "rejected" -> Icons.Outlined.Cancel; "uploaded" -> Icons.Outlined.Schedule; else -> Icons.Outlined.UploadFile }, null, tint = docColor(item.status), modifier = Modifier.size(22.dp))
                            Text(item.label, color = Navy, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            StatusChip(item.status.name, docColor(item.status))
                        }
                        item.fileName?.let { name -> Text(name, color = Muted, fontSize = 12.sp) }
                        item.fix?.let { fix -> Text(fix, color = Danger, fontSize = 13.sp) }
                        if (pendingType == item.type) {
                            UploadSourceRow(sources)
                        }
                    }
                }
            }
            ErrorText(error ?: vm.error)
            Spacer(Modifier.height(64.dp))
        }
        SaathiFab { askSaathi("What documents are missing for my claim?") }
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
        vm.claimsLoaded = true
    }
    FadeInColumn(safeBottom = false) {
        ScreenTitle("My claims", "Tap a card to track every step.")
        ChipRow {
            FilterChip(filter == null, { filter = null }, { Text("All") }, colors = chipColors())
            FilterChip(filter == "QUERY_RAISED,DOCS_PENDING", { filter = "QUERY_RAISED,DOCS_PENDING" }, { Text("Action") }, colors = chipColors())
            FilterChip(filter == "SETTLED", { filter = "SETTLED" }, { Text("Settled") }, colors = chipColors())
        }
        if (vm.claims.isEmpty() && !vm.claimsLoaded) { SkeletonCard(2); SkeletonCard(2) }
        else if (vm.claims.isEmpty()) AppCard { EmptyState(Icons.Outlined.Description, "No claims here", "Start a claim from Home — it will show up here and update live.") }
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
                Text("${if (claim.claimType.name == "CASHLESS") "Cashless pre-auth" else "Reimbursement"} · ${claim.patientName}${if (claim.isTemplate) " · sample" else ""}", color = Muted, fontSize = 12.sp)
                Text(inr(claim.billAmount ?: claim.estimatedAmount), fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
            }
        }
        ErrorText(vm.error)
    }
}

@Composable
fun TrackingScreen(claimId: String, onBack: () -> Unit, onQuery: () -> Unit, onSettlement: () -> Unit, onDocs: () -> Unit, askSaathi: (String) -> Unit = {}, onBill: () -> Unit = {}) {
    var timeline by remember { mutableStateOf<Timeline?>(null) }
    var detail by remember { mutableStateOf<com.claimsaathi.app.data.Claim?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(claimId) {
        while (true) {
            runCatching { Network.api.timeline(claimId) }.onSuccess { timeline = it; error = null }.onFailure { error = Network.apiMessage(it) }
            runCatching { Network.api.claim(claimId) }.onSuccess { detail = it }
            delay(5_000)
        }
    }
    val item = timeline
    val tip = when ((item?.status ?: detail?.status)?.name) {
        "DOCS_PENDING", "CREATED", "PREAUTH_SUBMITTED" -> "Upload your documents one by one — each is verified instantly and the details fill in automatically."
        "UNDER_REVIEW", "NEEDS_HUMAN" -> "Your claim is with the claims team. If they need anything, you’ll get a query alert here."
        "QUERY_RAISED" -> "The insurer has a question. Tap “Answer queries” and reply with the requested file to keep things moving."
        "APPROVED" -> "Approved! Settlement is being processed — open Settlement to see every deduction explained."
        "SETTLED" -> "Paid. Open Settlement to see exactly why each amount was deducted."
        "REJECTED" -> "This claim was rejected. Ask Saathi to explain why and what you can do next."
        else -> null
    }
    Box(Modifier.fillMaxSize()) {
    FadeInColumn {
        TopBar("Claim tracking", onBack)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(item?.claimNumber ?: "Claim", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            item?.status?.let { StatusChip(it.name, statusColor(it)) }
        }
        if (item == null && error == null) SkeletonCard(4)
        tip?.let { SaathiTip(it) }
        detail?.let { d ->
            AppCard {
                Text(d.hospital, color = Navy, fontWeight = FontWeight.Bold)
                Text("${if (d.claimType.name == "CASHLESS") "Cashless pre-auth" else "Reimbursement"} · ${d.patientName} · ${d.reason}", color = Muted, fontSize = 13.sp)
                Text("Bill ${inr(d.billAmount)} · Estimate ${inr(d.estimatedAmount)}", color = Navy, fontSize = 13.sp)
            }
            WarningList(d.warnings)
        }
        PredictionCard(claimId, timeline?.status)
        item?.latestOpsUpdate?.let { update ->
            AppCard {
                Text("LATEST UPDATE", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(update.message, color = Navy)
            }
        }
        if (!item?.steps.isNullOrEmpty()) AppCard {
            Text("PROGRESS", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val steps = item?.steps.orEmpty()
            Column {
                steps.forEachIndexed { index, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(22.dp).clip(CircleShape).background(if (step.state == StepState.pending) Border else stepColor(step.state)), contentAlignment = Alignment.Center) {
                                when (step.state) {
                                    StepState.done -> Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    StepState.failed -> Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    StepState.current -> PulseDot(Color.White)
                                    else -> {}
                                }
                            }
                            if (index < steps.size - 1) Box(Modifier.size(width = 2.dp, height = 30.dp).background(if (step.state == StepState.done) Success else Border))
                        }
                        Column(Modifier.padding(bottom = 10.dp)) {
                            Text(step.label, color = if (step.state == StepState.pending) Muted else Navy, fontWeight = if (step.state == StepState.current) FontWeight.Bold else FontWeight.SemiBold)
                            step.note?.let { Text(it, color = Muted, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
        if (!item?.openQueries.isNullOrEmpty()) PrimaryButton("Answer queries", onClick = onQuery)
        GhostButton("Upload documents", onClick = onDocs)
        GhostButton("Bill analysis (AI)", onClick = onBill)
        GhostButton("Settlement", onClick = onSettlement)
        ErrorText(error)
        Spacer(Modifier.height(64.dp))
    }
    SaathiFab { askSaathi("Track my claim and explain the next step") }
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
    var showSources by remember { mutableStateOf(false) }
    val sources = rememberUploadSources("query-reply", onPart = { part ->
        vm.work {
            val answered = Network.api.respond(queryId, reply.ifBlank { "Uploaded the requested file" }.plainBody(), part, query?.requestedDocType?.name?.plainBody())
            query = answered
            result = answered.document
        }
    }, onError = { vm.error = it })
    FadeInColumn {
        TopBar("Query", onBack)
        AppCard { Text(query?.message ?: "Loading…", color = Navy) }
        explain?.explanation?.let { AppCard { Text(it, color = Navy) } }
        Field("Your reply", reply) { reply = it }
        PrimaryButton("Send reply", loading = vm.busy) {
            vm.work { query = Network.api.respond(queryId, reply.ifBlank { "Replied from the app" }.plainBody(), null, null) }
        }
        GhostButton("Attach file / photo and send") { showSources = !showSources }
        if (showSources) UploadSourceRow(sources)
        result?.validation?.let { Text("Document: ${prettyStatus(it.appStatus.name)}${it.fix?.let { f -> " · $f" } ?: ""}", color = if (it.appStatus.name == "verified") Success else Warning) }
        if (query?.status?.name == "CLOSED") Text("Query resolved ✓", color = Success, fontWeight = FontWeight.Bold)
        result?.validation?.checks?.forEach { check ->
            AppCard { Text("${check.label}: ${check.detail}", color = Muted, fontSize = 13.sp) }
        }
        ErrorText(vm.error)
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
                Text(if (it.status.name == "PAID") "PAID TO YOUR ACCOUNT" else "APPROVED AMOUNT", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(inr(it.approvedAmount), color = Success, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                StatusChip(it.status.name, if (it.status.name == "PAID") Success else Warning)
                it.explanation?.let { e -> Text(e, color = Muted, fontSize = 12.sp) }
                it.utr?.let { utr -> Text("UTR $utr${it.paidAt?.let { d -> " · paid ${d.take(10)}" } ?: ""}", color = Navy) }
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
        if (notes.isEmpty()) AppCard { EmptyState(Icons.Outlined.NotificationsNone, "No alerts yet", "Claim updates in English & Hindi will appear here live.") }
        notes.forEach { note ->
            AppCard(Modifier.clickable {
                vm.work { Network.api.markRead(note.id) }
                note.claimId?.let(onOpenClaim)
            }) {
                Text((when (note.type.name) { "WARNING" -> "⚠️ "; "SUCCESS" -> "✅ "; "ACTION_REQUIRED" -> "👉 "; else -> "" }) + note.title + (if (!note.read) "  •" else ""), color = Navy, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                note.claim?.claimNumber?.let { Text("$it · ${note.createdAt?.take(10).orEmpty()}", color = Muted, fontSize = 11.sp) }
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
    var current by remember { mutableStateOf<com.claimsaathi.app.data.BankMasked?>(null) }
    LaunchedEffect(Unit) { runCatching { Network.api.bank() }.onSuccess { current = it.bank } }
    FadeInColumn {
        TopBar("Bank account", onBack)
        current?.let { b ->
            AppCard {
                Text("PAYOUT ACCOUNT", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${b.bankName ?: "Bank"} ${b.accountNumberMasked}", color = Navy, fontWeight = FontWeight.Bold)
                Text("${b.accountName} · ${b.ifsc}${if (b.verified) " · verified ✓" else ""}", color = Muted, fontSize = 13.sp)
            }
        }
        FinanceSection()
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
