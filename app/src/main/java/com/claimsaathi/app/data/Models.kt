@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.claimsaathi.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames

@Serializable
enum class UserRole { CUSTOMER, OPS, ADMIN, UNKNOWN }

@Serializable
enum class ClaimStatus { CREATED, PREAUTH_SUBMITTED, DOCS_PENDING, UNDER_REVIEW, QUERY_RAISED, NEEDS_HUMAN, APPROVED, REJECTED, SETTLED, UNKNOWN }

@Serializable
enum class ClaimType { CASHLESS, REIMBURSEMENT, UNKNOWN }

@Serializable
enum class AdmissionType { PLANNED, EMERGENCY, UNKNOWN }

@Serializable
enum class DocumentType {
    HEALTH_CARD, POLICY_SCHEDULE, CLAIM_FORM, PREAUTH_FORM, DOCTOR_ESTIMATE, DISCHARGE_SUMMARY,
    HOSPITAL_BILL, PHARMACY_BILL, LAB_REPORT, PRESCRIPTION, PAYMENT_RECEIPT, ID_PROOF, OTHER
}

@Serializable
enum class DocumentStatus { UPLOADED, VERIFIED, NEEDS_REVIEW, INVALID, UNKNOWN }

@Serializable
enum class QueryStatus { OPEN, ANSWERED, CLOSED, UNKNOWN }

@Serializable
enum class ActorType { AI, HUMAN, SYSTEM, UNKNOWN }

@Serializable
enum class SettlementStatus { PREVIEW, ESTIMATED, APPROVED, PAID, UNKNOWN }

@Serializable
enum class NotificationType { INFO, SUCCESS, WARNING, ACTION_REQUIRED, UNKNOWN }

@Serializable
enum class AppDocStatus { uploaded, verified, missing, rejected, unknown }

@Serializable
enum class StepState { done, current, pending, failed, unknown }

@Serializable
data class BankMasked(
    val accountName: String = "",
    val accountNumberMasked: String = "",
    val ifsc: String = "",
    val bankName: String? = null,
    val verified: Boolean = false
)

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String? = null,
    val phone: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val city: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val bank: BankMasked? = null,
    val pushEnabled: Boolean = false,
    val profileComplete: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class Member(val name: String = "", val relation: String? = null, val dob: String? = null)

@Serializable
data class WaitingPeriod(val name: String = "", val months: Int = 0)

@Serializable
data class PolicyClaimRef(
    val id: String = "",
    val claimNumber: String = "",
    val status: ClaimStatus = ClaimStatus.CREATED,
    val hospital: String? = null,
    val createdAt: String? = null
)

@Serializable
data class Policy(
    val id: String = "",
    val insurer: String = "",
    val planName: String? = null,
    val policyNumber: String = "",
    val sumInsured: Int = 0,
    val roomRentLimit: Int = 0,
    val icuLimit: Int? = null,
    val coPayPercent: Double = 0.0,
    val startDate: String = "",
    val endDate: String? = null,
    val waitingPeriods: List<WaitingPeriod> = emptyList(),
    val exclusions: List<String> = emptyList(),
    val members: List<Member>? = null,
    val isTemplate: Boolean = false,
    val analysis: PolicyAnalysis? = null,
    val summary: String? = null,
    val claims: List<PolicyClaimRef>? = null
)

@Serializable
data class PatientDetails(val age: Int? = null, val gender: String? = null, val relation: String? = null)

@Serializable
data class Counts(val documents: Int = 0, val queries: Int = 0)

@Serializable
data class HomeChecklist(
    val stage: String? = null,
    val required: List<String> = emptyList(),
    val missing: List<String> = emptyList(),
    val flagged: List<String> = emptyList(),
    val verified: List<String> = emptyList()
)

@Serializable
data class PolicyRef(
    val id: String = "",
    val insurer: String = "",
    val policyNumber: String = "",
    val planName: String? = null
)

@Serializable
data class Claim(
    val id: String = "",
    val claimNumber: String = "",
    val policyId: String = "",
    val patientName: String = "",
    val hospital: String = "",
    val hospitalCity: String? = null,
    val reason: String = "",
    val treatment: String? = null,
    val claimType: ClaimType = ClaimType.REIMBURSEMENT,
    val admissionType: AdmissionType = AdmissionType.EMERGENCY,
    val admissionDate: String? = null,
    val dischargeDate: String? = null,
    val days: Int? = null,
    val estimatedAmount: Int? = null,
    val billAmount: Int? = null,
    val status: ClaimStatus = ClaimStatus.CREATED,
    val createdAt: String? = null,
    val policy: PolicyRef? = null,
    val settlement: Settlement? = null,
    val checklist: HomeChecklist? = null,
    val roomRentPerDay: Int? = null,
    val isTemplate: Boolean = false,
    val warnings: List<AmountWarning> = emptyList(),
    @JsonNames("_count") val count: Counts? = null
)

@Serializable
data class AmountWarning(val code: String = "", val severity: String = "info", val message: String = "", val field: String? = null, val claimId: String? = null, val claimNumber: String? = null)

@Serializable
data class PreviewEstimate(val billAmount: Int = 0, val approvedAmount: Int = 0, val coPayAmount: Int = 0, val outOfPocket: Int = 0)

@Serializable
data class PreviewResult(
    val warnings: List<AmountWarning> = emptyList(),
    val hasBlocking: Boolean = false,
    val sumInsured: Int? = null,
    val usedSumInsured: Int? = null,
    val remainingSumInsured: Int? = null,
    val roomRentLimit: Int? = null,
    val coPayPercent: Double? = null,
    val estimate: PreviewEstimate? = null
)

@Serializable
data class PreviewBody(
    val policyId: String? = null,
    val type: String,
    val estimatedAmount: Int? = null,
    val billAmount: Int? = null,
    val roomRentPerDay: Int? = null,
    val days: Int? = null,
    val reason: String? = null,
    val treatment: String? = null,
    val admissionDate: String? = null
)

@Serializable
data class ClaimTemplate(
    val claimType: String = "REIMBURSEMENT",
    val policyId: String? = null,
    val hospital: String = "",
    val hospitalCity: String? = null,
    val isNetworkHospital: Boolean? = null,
    val reason: String = "",
    val treatment: String? = null,
    val admissionType: String? = null,
    val admissionDate: String? = null,
    val dischargeDate: String? = null,
    val days: Int? = null,
    val roomType: String? = null,
    val roomRentPerDay: Int? = null,
    val estimatedAmount: Int? = null,
    val billAmount: Int? = null,
    val patientName: String? = null,
    val patientDetails: PatientDetails? = null
)

@Serializable
data class DemoTemplates(val policyId: String? = null, val policyNumber: String? = null, val preauth: ClaimTemplate? = null, val reimbursement: ClaimTemplate? = null, val consentOtp: String? = null)

@Serializable
data class FinanceAccount(val id: String = "", val bankName: String = "", val accountType: String = "", val maskedNumber: String = "", val ifsc: String? = null, val balance: Double = 0.0, val isPrimary: Boolean = false, val linkedForPayouts: Boolean = false)
@Serializable
data class ExpenseCategory(val category: String = "", val amount: Double = 0.0, val percent: Double = 0.0)
@Serializable
data class MonthlyExpenses(val month: String? = null, val label: String? = null, val total: Double = 0.0, val categories: List<ExpenseCategory> = emptyList())
@Serializable
data class MedicalSpend(val totalMedicalSpend: Double = 0.0, val insurerPaid: Double = 0.0, val outOfPocket: Double = 0.0, val insurerPaidPercent: Double = 0.0, val settledClaims: Int = 0, val pendingClaims: Int = 0, val thisMonthMedicalExpense: Double = 0.0)
@Serializable
data class Payout(val claimId: String? = null, val claimNumber: String = "", val hospital: String? = null, val amount: Double = 0.0, val utr: String? = null, val paidAt: String? = null, val creditedTo: String? = null)
@Serializable
data class Finance(val isDemo: Boolean = true, val note: String? = null, val accounts: List<FinanceAccount> = emptyList(), val totalBalance: Double = 0.0, val monthlyExpenses: MonthlyExpenses = MonthlyExpenses(), val medical: MedicalSpend = MedicalSpend(), val payouts: List<Payout> = emptyList(), val totalPayouts: Double = 0.0)
@Serializable
data class ChatCard(
    val type: String = "",
    val title: String? = null,
    val total: Double? = null,
    val month: String? = null,
    val accounts: List<FinanceAccount> = emptyList(),
    val categories: List<ExpenseCategory> = emptyList(),
    val payouts: List<Payout> = emptyList(),
    val totalMedicalSpend: Double? = null,
    val insurerPaid: Double? = null,
    val outOfPocket: Double? = null,
    val insurerPaidPercent: Double? = null
)
@Serializable
data class ChatSuggestions(val greeting: String? = null, val suggestions: List<String> = emptyList())

@Serializable
data class Deduction(val label: String = "", val amount: Int = 0, val reason: String? = null, val clause: String? = null)

@Serializable
data class Settlement(
    val id: String? = null,
    val claimId: String? = null,
    val claimNumber: String? = null,
    val billAmount: Int = 0,
    val deductions: List<Deduction> = emptyList(),
    val coPayAmount: Int = 0,
    val approvedAmount: Int = 0,
    val explanation: String? = null,
    val status: SettlementStatus = SettlementStatus.PREVIEW,
    val utr: String? = null,
    val paidAt: String? = null,
    val isDemo: Boolean = true,
    val isEstimate: Boolean = false,
    val preview: Boolean = false
)

@Serializable
data class ClaimRef(
    val id: String = "",
    val claimNumber: String = "",
    val hospital: String? = null,
    val status: ClaimStatus? = null,
    val patientName: String? = null
)

@Serializable
data class ClaimQuery(
    val id: String = "",
    val claimId: String = "",
    val message: String = "",
    val requestedDocType: DocumentType? = null,
    val response: String? = null,
    val status: QueryStatus = QueryStatus.OPEN,
    val createdAt: String? = null,
    val claim: ClaimRef? = null,
    val document: UploadResponse? = null
)

@Serializable
data class AppNotification(
    val id: String = "",
    val claimId: String? = null,
    val type: NotificationType = NotificationType.INFO,
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    val createdAt: String? = null,
    val claim: ClaimRef? = null
)

@Serializable
data class NotificationsPage(val items: List<AppNotification> = emptyList(), val unread: Int = 0)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
    val isNewUser: Boolean = false,
    val needsProfile: Boolean = false
)

@Serializable
data class ProfileResponse(val user: User, val token: String)
@Serializable
data class OtpSent(val sent: Boolean = true, val phone: String? = null, val demo: Boolean? = null)

@Serializable
data class Coverage(val item: String = "", val covered: Boolean = true, val limit: Int? = null, val detail: String = "")
@Serializable
data class WaitingStatus(
    val name: String = "",
    val months: Int = 0,
    val eligibleFrom: String? = null,
    val active: Boolean = false,
    val status: String = ""
)

@Serializable
data class PolicyAnalysis(
    val policyNumber: String = "",
    val insurer: String = "",
    val sumInsured: Int = 0,
    val roomRentLimit: Int = 0,
    val coPayPercent: Double = 0.0,
    val isActive: Boolean = true,
    val members: List<Member> = emptyList(),
    val coverage: List<Coverage> = emptyList(),
    val exclusions: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val waitingPeriods: List<WaitingStatus> = emptyList(),
    val whatIsCovered: String = "",
    val source: String = "",
    val ai: String = "",
    val analyzedAt: String? = null
)

@Serializable
data class AddPolicyResponse(val policy: Policy, val analysis: PolicyAnalysis? = null, val extractedFromPdf: Boolean = false)

@Serializable
data class ChecklistItem(
    val type: DocumentType = DocumentType.OTHER,
    val label: String = "",
    val required: Boolean = false,
    val status: AppDocStatus = AppDocStatus.missing,
    val documentId: String? = null,
    val fileName: String? = null,
    val confidence: Double? = null,
    val reason: String? = null,
    val fix: String? = null
)

@Serializable
data class Progress(val required: Int = 0, val verified: Int = 0, val uploaded: Int = 0)

@Serializable
data class Checklist(
    val claimId: String = "",
    val claimType: ClaimType = ClaimType.REIMBURSEMENT,
    val stage: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val warnings: List<String> = emptyList(),
    val progress: Progress = Progress(),
    val complete: Boolean = false
)

@Serializable
data class ValidationCheck(val key: String = "", val label: String = "", val passed: Boolean? = null, val detail: String = "")

@Serializable
data class UploadValidation(
    val status: DocumentStatus = DocumentStatus.UPLOADED,
    val appStatus: AppDocStatus = AppDocStatus.uploaded,
    val confidence: Double = 0.0,
    val summary: String? = null,
    val fix: String? = null,
    val checks: List<ValidationCheck> = emptyList(),
    val warnings: List<String> = emptyList(),
    val extracted: JsonElement? = null
)

@Serializable
data class UploadResponse(
    val id: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val fileName: String = "",
    val status: DocumentStatus = DocumentStatus.UPLOADED,
    val validation: UploadValidation = UploadValidation(),
    val checklist: Checklist = Checklist(),
    val claimStatus: ClaimStatus = ClaimStatus.CREATED
)

@Serializable
data class Step(
    val key: String = "",
    val label: String = "",
    val done: Boolean = false,
    val state: StepState = StepState.pending,
    val at: String? = null,
    val note: String? = null
)

@Serializable
data class OpsUpdate(val kind: String = "", val message: String = "", val at: String? = null)

@Serializable
data class TimelineDocument(
    val id: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val status: DocumentStatus = DocumentStatus.UPLOADED,
    val fileName: String? = null
)

@Serializable
data class ClaimEvent(
    val id: String = "",
    val status: ClaimStatus = ClaimStatus.CREATED,
    val title: String = "",
    val description: String? = null,
    val actor: ActorType = ActorType.SYSTEM,
    val createdAt: String? = null
)

@Serializable
data class Timeline(
    val status: ClaimStatus = ClaimStatus.CREATED,
    val claimNumber: String? = null,
    val events: List<ClaimEvent> = emptyList(),
    val openQueries: List<ClaimQuery> = emptyList(),
    val settlement: Settlement? = null,
    val documents: List<TimelineDocument> = emptyList(),
    val steps: List<Step> = emptyList(),
    val currentStep: String? = null,
    val latestOpsUpdate: OpsUpdate? = null,
    val checklistWarnings: List<String> = emptyList()
)

@Serializable
data class Grounded(val policyNumber: String? = null, val claimNumber: String? = null)

@Serializable
data class ChatReply(
    val answer: String = "",
    val intent: String = "",
    val sources: List<String> = emptyList(),
    val followUps: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val cards: List<ChatCard> = emptyList(),
    val grounded: Grounded? = null,
    val ai: String? = null
)

@Serializable
data class PendingAction(
    val kind: String = "",
    val title: String = "",
    val claimId: String? = null,
    val queryId: String? = null,
    val documentType: String? = null
)

@Serializable
data class HomeCounts(
    val policies: Int = 0,
    val claims: Int = 0,
    val activeClaims: Int = 0,
    val openQueries: Int = 0,
    val pendingActions: Int = 0,
    val unreadNotifications: Int = 0
)

@Serializable
data class Home(
    val user: User,
    val activePolicy: Policy? = null,
    val currentClaim: Claim? = null,
    val pendingActions: List<PendingAction> = emptyList(),
    val counts: HomeCounts = HomeCounts(),
    val paidOut: Int = 0,
    val warnings: List<AmountWarning> = emptyList()
)

@Serializable
data class CoverageResult(
    val covered: Boolean = false,
    val warnings: List<String> = emptyList(),
    val clauses: List<String> = emptyList(),
    val explanation: String = "",
    val estimatedPayable: Double = 0.0
)

@Serializable
data class QueryExplain(val explanation: String = "", val nextSteps: List<String> = emptyList())

@Serializable
data class SignedUrl(val url: String = "")
@Serializable
data class OkResponse(val ok: Boolean = true)
@Serializable
data class BankResponse(val bank: BankMasked? = null)
@Serializable
data class PreauthResponse(val ok: Boolean = true, val estimate: Settlement? = null)

@Serializable data class PhoneBody(val phone: String)
@Serializable data class VerifyBody(val phone: String, val otp: String)
@Serializable data class ProfileBody(val name: String, val email: String, val dob: String, val gender: String, val city: String? = null)
@Serializable data class ProfilePatch(val name: String? = null, val email: String? = null, val city: String? = null)
@Serializable data class AddPolicyBody(
    val insurer: String,
    val policyNumber: String,
    val sumInsured: Int,
    val startDate: String,
    val endDate: String? = null,
    val planName: String? = null,
    val roomRentLimit: Int? = null,
    val coPayPercent: Double? = null,
    val members: List<Member>? = null
)
@Serializable data class BankBody(val accountName: String, val accountNumber: String, val ifsc: String, val bankName: String? = null, val otp: String)
@Serializable data class CreateClaimBody(
    val policyId: String,
    val type: String,
    val hospital: String,
    val hospitalCity: String? = null,
    val reason: String,
    val treatment: String? = null,
    val admissionType: String = "EMERGENCY",
    val admissionDate: String? = null,
    val dischargeDate: String? = null,
    val days: Int? = null,
    val roomType: String? = null,
    val roomRentPerDay: Int? = null,
    val isNetworkHospital: Boolean? = null,
    val billAmount: Int? = null,
    val estimatedAmount: Int? = null,
    val patientName: String,
    val patientDetails: PatientDetails? = null,
    val consentOtp: String? = null
)
@Serializable data class ChatBody(val message: String, val claimId: String? = null, val lang: String? = null)
@Serializable data class ApiErrorBody(val error: ApiErr) {
    @Serializable data class ApiErr(val code: String = "", val message: String = "")
}
