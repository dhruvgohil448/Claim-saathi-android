package com.claimsaathi.app.data

import kotlinx.serialization.Serializable

@Serializable data class RiskFlag(val code: String = "", val severity: String = "low", val message: String = "", val messageHi: String? = null)
@Serializable data class PredDeduction(val label: String = "", val amount: Double = 0.0, val reason: String = "", val reasonHi: String? = null, val clause: String? = null)
@Serializable data class Prediction(
    val claimId: String = "", val predictedPayout: Double = 0.0, val billAmount: Double = 0.0, val totalDeductions: Double = 0.0,
    val confidence: Double = 0.0, val confidenceLabel: String = "", val basis: String = "", val headline: String = "", val headlineHi: String = "",
    val deductions: List<PredDeduction> = emptyList(), val rejectionRisk: String = "LOW", val riskFlags: List<RiskFlag> = emptyList(), val tips: List<String> = emptyList()
)
@Serializable data class BillLine(
    val description: String = "", val qty: Double = 1.0, val rate: Double = 0.0, val amount: Double = 0.0, val payableAmount: Double = 0.0,
    val deduction: Double = 0.0, val status: String = "PAYABLE", val reason: String = "", val reasonHi: String? = null
)
@Serializable data class BillTotals(val billed: Double = 0.0, val payableBeforeCoPay: Double = 0.0, val nonPayable: Double = 0.0, val proportionateCut: Double = 0.0, val coPay: Double = 0.0, val finalPayable: Double = 0.0)
@Serializable data class BillAnalysis(
    val available: Boolean = false, val message: String? = null, val messageHi: String? = null, val items: List<BillLine> = emptyList(),
    val totals: BillTotals? = null, val summary: String? = null, val summaryHi: String? = null
)
@Serializable data class VoiceStatus(val sarvam: Boolean = false, val stt: String? = null, val tts: String? = null)
@Serializable data class SttBody(val audioBase64: String, val mimeType: String = "audio/mp4", val lang: String? = null)
@Serializable data class SttResult(val text: String? = null, val language: String? = null)
@Serializable data class TtsBody(val text: String, val lang: String? = null)
@Serializable data class TtsResult(val audioBase64: String? = null, val mimeType: String? = null, val provider: String? = null)
