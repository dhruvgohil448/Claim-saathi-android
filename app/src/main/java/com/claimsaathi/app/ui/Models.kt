package com.claimsaathi.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

enum class AppTab { Home, Claims, Docs, Assistant, Profile }

enum class ClaimStatus(val label: String) {
    Submitted("Submitted"),
    DocsVerified("Docs verified"),
    UnderReview("Under review"),
    ActionRequired("Action required"),
    Approved("Approved"),
    Settled("Settled")
}

enum class DocStatus(val label: String) {
    Verified("Verified"),
    NeedsReview("Needs review"),
    Missing("Missing")
}

enum class StepState { Done, Current, Upcoming }

data class TimelineStep(
    val title: String,
    val detail: String,
    val state: StepState
)

data class ClaimDocument(
    val id: String,
    val title: String,
    val fileName: String?,
    val status: DocStatus,
    val note: String
)

data class DeductionLine(
    val title: String,
    val amount: Int,
    val reason: String
)

data class Settlement(
    val billAmount: Int,
    val lines: List<DeductionLine>,
    val approvedAmount: Int
) {
    val deductions: Int get() = lines.sumOf { it.amount }
}

data class Claim(
    val id: String,
    val hospital: String,
    val reason: String,
    val amount: Int,
    val status: ClaimStatus,
    val updated: String,
    val steps: List<TimelineStep>,
    val query: String?,
    val settlement: Settlement?
)

data class ChatMessage(
    val id: Int,
    val isUser: Boolean,
    val text: String
)

data class PolicySummary(
    val insurer: String = "Star Health",
    val product: String = "Family Health Optima",
    val number: String = "POL-88421",
    val holder: String = "Rajesh Sharma",
    val sumInsured: Int = 500_000,
    val roomRentPerDay: Int = 5_000,
    val coPay: String = "10%",
    val waitingPeriods: List<String> = listOf(
        "Initial waiting period: 30 days",
        "Pre-existing conditions: 2 years",
        "Specific illnesses: 1 year"
    ),
    val exclusions: List<String> = listOf(
        "Cosmetic treatment",
        "Dental care, unless caused by an accident",
        "Items listed as non-payable consumables"
    )
)

fun inr(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}

class AppState : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var tab by mutableStateOf(AppTab.Home)
    var claims by mutableStateOf(seedClaims())
    var documents by mutableStateOf(seedDocuments())
    var messages by mutableStateOf(
        listOf(
            ChatMessage(
                1,
                false,
                "Hi Rajesh. I read your Star Health policy. Sum insured is ₹5,00,000 and room rent is capped at ₹5,000 a day. Ask me about coverage, documents, or claim CLM-1042."
            )
        )
    )
    var notice by mutableStateOf<String?>(null)
    private var nextMessageId = 2

    val userName = "Rajesh Sharma"
    val email = "customer@claimsaathi.demo"
    val phone = "+91 98765 43210"
    val policy = PolicySummary()

    val activeClaim: Claim? get() = claims.firstOrNull { it.status != ClaimStatus.Settled } ?: claims.firstOrNull()
    val missingDocumentCount: Int get() = documents.count { it.status != DocStatus.Verified }

    fun login() {
        isLoggedIn = true
        tab = AppTab.Home
    }

    fun logout() {
        isLoggedIn = false
    }

    fun claim(id: String): Claim? = claims.firstOrNull { it.id == id }

    fun markUploaded(documentId: String) {
        documents = documents.map { document ->
            if (document.id != documentId) document
            else document.copy(
                status = DocStatus.NeedsReview,
                fileName = "Upload_$documentId.pdf",
                note = "Uploaded in the demo. Waiting for a check."
            )
        }
        notice = "File uploaded. Demo only."
    }

    fun replyToQuery(claimId: String, message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return
        claims = claims.map { claim ->
            if (claim.id != claimId) return@map claim
            val current = claim.steps.indexOfFirst { it.state == StepState.Current }
            val steps = claim.steps.mapIndexed { index, step ->
                when {
                    index == current -> TimelineStep("Query resolved", "You replied: $trimmed", StepState.Done)
                    index == current + 1 && step.state == StepState.Upcoming ->
                        step.copy(state = StepState.Current)
                    else -> step
                }
            }
            claim.copy(query = null, status = ClaimStatus.UnderReview, steps = steps)
        }
        notice = "Reply sent. A person reviews this only in the demo."
    }

    fun advance(claimId: String) {
        claims = claims.map { claim ->
            if (claim.id != claimId) return@map claim
            val current = claim.steps.indexOfFirst { it.state == StepState.Current }
            if (current < 0) return@map claim
            val steps = claim.steps.mapIndexed { index, step ->
                when (index) {
                    current -> step.copy(state = StepState.Done)
                    current + 1 -> step.copy(detail = "Updated in the demo.", state = StepState.Current)
                    else -> step
                }
            }
            val next = steps.getOrNull(current + 1)
            val status = if (next == null) ClaimStatus.Settled else statusFor(next.title)
            claim.copy(steps = steps, status = status)
        }
        notice = "Status moved forward. This is sample data."
    }

    fun submitClaim(hospital: String, reason: String, amount: Int) {
        val id = "CLM-${Random.nextInt(2000, 9000)}"
        val claim = Claim(
            id = id,
            hospital = hospital,
            reason = reason,
            amount = amount,
            status = ClaimStatus.Submitted,
            updated = "Just now",
            steps = listOf(
                TimelineStep("Submitted", "Demo pre-auth created.", StepState.Current),
                TimelineStep("Docs verified", "Waiting for documents.", StepState.Upcoming),
                TimelineStep("Under review", "Not started.", StepState.Upcoming),
                TimelineStep("Approved", "Not started.", StepState.Upcoming),
                TimelineStep("Settled", "Not started.", StepState.Upcoming)
            ),
            query = null,
            settlement = null
        )
        claims = listOf(claim) + claims
        notice = "$id submitted as a demo. No insurer received it."
    }

    fun ask(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val user = ChatMessage(nextMessageId++, true, trimmed)
        val reply = ChatMessage(nextMessageId++, false, answer(trimmed))
        messages = messages + user + reply
    }

    private fun answer(text: String): String {
        val query = text.lowercase()
        return when {
            "room" in query ->
                "Room rent is capped at ₹5,000 per day on policy ${policy.number}. A higher room tariff is deducted in the settlement."
            "cover" in query || "policy" in query || "sum" in query || "wait" in query || "exclu" in query ->
                "${policy.insurer} ${policy.product}, ${policy.number}. Sum insured ${inr(policy.sumInsured)}. Co-pay ${policy.coPay}. ${policy.waitingPeriods.joinToString(". ")}. Excluded: ${policy.exclusions.joinToString("; ")}."
            "document" in query || "missing" in query || "upload" in query -> {
                val pending = documents.filter { it.status != DocStatus.Verified }
                if (pending.isEmpty()) "Every required document is verified."
                else pending.joinToString(" ") { "${it.title}: ${it.status.label}. ${it.note}" }
            }
            "status" in query || "claim" in query || "track" in query -> {
                val claim = activeClaim ?: return "There is no active claim in this demo."
                "${claim.id} at ${claim.hospital} is ${claim.status.label.lowercase()}. Amount ${inr(claim.amount)}. ${claim.query ?: "No open query."}"
            }
            "deduct" in query || "settle" in query || "why" in query || "amount" in query -> {
                val settlement = claims.firstOrNull { it.settlement != null }?.settlement
                    ?: return "A settlement breakdown is not ready for the newest claim."
                val reasons = settlement.lines.joinToString(" ") { "${it.title} ${inr(it.amount)}: ${it.reason}" }
                "Sample bill ${inr(settlement.billAmount)}. Deductions ${inr(settlement.deductions)}. Approved ${inr(settlement.approvedAmount)}. $reasons These figures are demo data."
            }
            else ->
                "I can explain this demo policy, the room-rent limit, missing documents, claim status, and why an amount was deducted. If something is not in the sample policy, check it with the insurer."
        }
    }

    private fun statusFor(title: String) = when (title) {
        "Docs verified" -> ClaimStatus.DocsVerified
        "Under review" -> ClaimStatus.UnderReview
        "Query", "Action required" -> ClaimStatus.ActionRequired
        "Approved" -> ClaimStatus.Approved
        "Settled" -> ClaimStatus.Settled
        else -> ClaimStatus.Submitted
    }
}

private fun seedClaims() = listOf(
    Claim(
        id = "CLM-1042",
        hospital = "Apollo Hospitals, Ahmedabad",
        reason = "Dengue admission",
        amount = 220_000,
        status = ClaimStatus.ActionRequired,
        updated = "Today, 9:40 AM",
        steps = listOf(
            TimelineStep("Submitted", "Pre-auth sent on 28 Sep.", StepState.Done),
            TimelineStep("Docs verified", "Policy, bill, and ID checked.", StepState.Done),
            TimelineStep("Under review", "Ops reviewed the hospital bill.", StepState.Done),
            TimelineStep("Query", "Upload the payment receipt.", StepState.Current),
            TimelineStep("Approved", "Waiting on the receipt.", StepState.Upcoming),
            TimelineStep("Settled", "Not started.", StepState.Upcoming)
        ),
        query = "Upload the payment receipt so we can match the bill.",
        settlement = Settlement(
            billAmount = 220_000,
            lines = listOf(
                DeductionLine("Non-payable items", 18_000, "Gloves, syringes, and toiletries are not payable."),
                DeductionLine("Room rent above cap", 15_000, "Room was ₹8,000 a day. The policy cap is ₹5,000."),
                DeductionLine("Co-pay 10%", 7_000, "Your policy shares 10% of the allowed amount.")
            ),
            approvedAmount = 180_000
        )
    ),
    Claim(
        id = "CLM-0988",
        hospital = "Fortis Hospital, Mohali",
        reason = "Day-care procedure",
        amount = 110_000,
        status = ClaimStatus.Settled,
        updated = "12 Sep",
        steps = listOf(
            TimelineStep("Submitted", "Filed on 2 Sep.", StepState.Done),
            TimelineStep("Docs verified", "All documents matched.", StepState.Done),
            TimelineStep("Approved", "Approved on 10 Sep.", StepState.Done),
            TimelineStep("Settled", "Sample payout recorded.", StepState.Done)
        ),
        query = null,
        settlement = Settlement(
            billAmount = 110_000,
            lines = listOf(DeductionLine("Non-payable items", 15_000, "Registration and consumables were excluded.")),
            approvedAmount = 95_000
        )
    )
)

private fun seedDocuments() = listOf(
    ClaimDocument("policy", "Policy PDF", "Star_Health_Policy.pdf", DocStatus.Verified, "Name and policy number match."),
    ClaimDocument("bill", "Hospital bill", "Apollo_Bill.pdf", DocStatus.Verified, "Amount matches the claim."),
    ClaimDocument("discharge", "Discharge summary", "Discharge_Summary.jpg", DocStatus.NeedsReview, "The discharge date is hard to read."),
    ClaimDocument("receipt", "Payment receipt", null, DocStatus.Missing, "Ops asked for this on claim CLM-1042."),
    ClaimDocument("id", "Photo ID", "Aadhaar.pdf", DocStatus.Verified, "Name matches the policyholder.")
)
