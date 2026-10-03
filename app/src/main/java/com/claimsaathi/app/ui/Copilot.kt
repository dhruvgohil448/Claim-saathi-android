package com.claimsaathi.app.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Base64
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claimsaathi.app.data.BillAnalysis
import com.claimsaathi.app.data.Network
import com.claimsaathi.app.data.Prediction
import com.claimsaathi.app.data.TtsBody
import java.io.File
import java.util.Locale

private fun riskColor(s: String) = when (s.lowercase()) { "high" -> Danger; "medium" -> Warning; else -> Muted }

/** "Saathi predicts" card: estimated payout + expandable reasons (English / हिंदी). */
@Composable
fun PredictionCard(claimId: String, refreshKey: Any? = null) {
    var p by remember { mutableStateOf<Prediction?>(null) }
    var open by remember { mutableStateOf(false) }
    var hindi by remember { mutableStateOf(false) }
    LaunchedEffect(claimId, refreshKey) { runCatching { Network.api.prediction(claimId) }.onSuccess { p = it } }
    val x = p ?: return
    AppCard(Modifier.animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Primary, modifier = Modifier.size(18.dp))
            Text("SAATHI PREDICTS", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(if (hindi) "EN" else "हिं", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Pale).clickable { hindi = !hindi }.padding(horizontal = 8.dp, vertical = 3.dp))
        }
        Text(inrD(x.predictedPayout), color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(if (hindi) x.headlineHi else "${x.headline} · ${x.confidenceLabel}", color = Muted, fontSize = 13.sp)
        Text(x.basis, color = Muted, fontSize = 12.sp)
        x.riskFlags.forEach { f ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(riskColor(f.severity)))
                Text(if (hindi) f.messageHi ?: f.message else f.message, color = Navy, fontSize = 12.sp)
            }
        }
        Row(Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (open) "Hide reasons" else "Why ${inrD(x.totalDeductions)} is deducted", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = Primary)
        }
        if (open) {
            KeyRow("Hospital bill", inrD(x.billAmount))
            x.deductions.forEach { d ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppBackground).padding(10.dp)) {
                    Row { Text(d.label, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("−${inrD(d.amount)}", color = Danger, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    Text(if (hindi) d.reasonHi ?: d.reason else d.reason, color = Muted, fontSize = 12.sp)
                }
            }
            KeyRow("You get", inrD(x.predictedPayout))
            x.tips.forEach { Text("💡 $it", color = Navy, fontSize = 12.sp) }
        }
    }
}

/** Bill analysis: every hospital bill line marked green (payable) / amber (partial) / red (not payable). */
@Composable
fun BillAnalysisScreen(claimId: String, onBack: () -> Unit) {
    var b by remember { mutableStateOf<BillAnalysis?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var hindi by remember { mutableStateOf(false) }
    LaunchedEffect(claimId) { runCatching { Network.api.billAnalysis(claimId) }.onSuccess { b = it }.onFailure { error = Network.apiMessage(it) } }
    FadeInColumn {
        TopBar("Bill analysis", onBack)
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScreenTitle("Saathi checked your bill", "Line by line against your policy rules")
            Spacer(Modifier.weight(1f))
            Text(if (hindi) "EN" else "हिं", color = Navy, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Pale).clickable { hindi = !hindi }.padding(horizontal = 10.dp, vertical = 4.dp))
        }
        if (b == null && error == null) { SkeletonCard(3); SkeletonCard(3) }
        b?.let { a ->
            if (!a.available) SaathiTip((if (hindi) a.messageHi else a.message) ?: "Upload the hospital bill first.")
            else {
                (if (hindi) a.summaryHi else a.summary)?.let { SaathiTip(it) }
                a.totals?.let { t ->
                    AppCard {
                        KeyRow("Billed", inrD(t.billed))
                        KeyRow("Not payable", "−${inrD(t.nonPayable)}")
                        KeyRow("Room-rent cut", "−${inrD(t.proportionateCut)}")
                        KeyRow("Co-pay", "−${inrD(t.coPay)}")
                        KeyRow("You get", inrD(t.finalPayable))
                    }
                }
                a.items.forEach { i ->
                    val c = when (i.status) { "NON_PAYABLE" -> Danger; "PARTIAL" -> Warning; else -> Success }
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(c))
                            Text(i.description, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            StatusChip(when (i.status) { "NON_PAYABLE" -> "Not payable"; "PARTIAL" -> "Partial"; else -> "Payable" }, c)
                        }
                        Text("${inrD(i.payableAmount)} of ${inrD(i.amount)}", color = Navy, fontSize = 13.sp)
                        Text(if (hindi) i.reasonHi ?: i.reason else i.reason, color = Muted, fontSize = 12.sp)
                    }
                }
            }
        }
        ErrorText(error)
        Spacer(Modifier.height(32.dp))
    }
}

/** Voice output: Sarvam bulbul audio from the server when configured, else on-device TextToSpeech (hi-IN / en-IN). */
class SaathiVoice(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private var recFile: File? = null
    init { tts = TextToSpeech(context.applicationContext) { } }

    suspend fun speak(text: String, hindi: Boolean) {
        stopSpeaking()
        val audio = runCatching { Network.api.tts(TtsBody(text, if (hindi) "hi" else "en")) }.getOrNull()?.audioBase64
        if (!audio.isNullOrBlank()) {
            runCatching {
                val f = File(context.cacheDir, "saathi_tts.wav"); f.writeBytes(Base64.decode(audio, Base64.DEFAULT))
                player = MediaPlayer().apply { setDataSource(f.absolutePath); prepare(); start() }
            }.onSuccess { return }
        }
        tts?.language = if (hindi) Locale("hi", "IN") else Locale("en", "IN")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "saathi")
    }
    fun stopSpeaking() { runCatching { player?.stop(); player?.release() }; player = null; tts?.stop() }

    fun startRecording(): Boolean = runCatching {
        val f = File(context.cacheDir, "saathi_voice.m4a"); recFile = f
        @Suppress("DEPRECATION")
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioSamplingRate(16000); r.setAudioEncodingBitRate(64000)
        r.setOutputFile(f.absolutePath); r.prepare(); r.start()
        recorder = r
    }.isSuccess
    /** Stops and returns base64 m4a, or null. */
    fun stopRecording(): String? {
        val r = recorder ?: return null
        recorder = null
        runCatching { r.stop() }; r.release()
        val bytes = recFile?.takeIf { it.exists() }?.readBytes() ?: return null
        return if (bytes.size < 1000) null else Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    fun release() { stopSpeaking(); runCatching { recorder?.release() }; tts?.shutdown() }
}

@Composable
fun rememberSaathiVoice(): SaathiVoice {
    val ctx = LocalContext.current
    val v = remember { SaathiVoice(ctx) }
    DisposableEffect(Unit) { onDispose { v.release() } }
    return v
}
