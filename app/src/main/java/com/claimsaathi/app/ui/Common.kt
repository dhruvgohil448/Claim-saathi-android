package com.claimsaathi.app.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claimsaathi.app.data.AppDocStatus
import com.claimsaathi.app.data.ClaimStatus
import com.claimsaathi.app.data.StepState
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.NumberFormat
import java.util.Locale

fun inr(amount: Int?): String {
    if (amount == null) return "–"
    return "₹" + NumberFormat.getNumberInstance(Locale("en", "IN")).format(amount)
}

fun prettyStatus(raw: String) = raw.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }

fun statusColor(status: ClaimStatus) = when (status) {
    ClaimStatus.APPROVED, ClaimStatus.SETTLED -> Success
    ClaimStatus.REJECTED -> Danger
    ClaimStatus.QUERY_RAISED, ClaimStatus.NEEDS_HUMAN, ClaimStatus.DOCS_PENDING -> Warning
    else -> Primary
}

fun docColor(status: AppDocStatus) = when (status) {
    AppDocStatus.verified -> Success
    AppDocStatus.uploaded -> Primary
    AppDocStatus.rejected -> Danger
    else -> Muted
}

fun stepColor(state: StepState) = when (state) {
    StepState.done -> Success
    StepState.current -> Primary
    StepState.failed -> Danger
    else -> Muted
}

fun Context.filePart(uri: Uri): MultipartBody.Part {
    val mime = contentResolver.getType(uri) ?: "application/octet-stream"
    val name = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: "upload"
    val bytes = contentResolver.openInputStream(uri)!!.use { it.readBytes() }
    return MultipartBody.Part.createFormData("file", name, bytes.toRequestBody(mime.toMediaType()))
}

fun String.plainBody() = toRequestBody("text/plain".toMediaType())

val NavyBrush = Brush.verticalGradient(listOf(Color(0xFF003A8C), Navy, Color(0xFF001F4D)))

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Navy.copy(0.08f), spotColor = Navy.copy(0.12f))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun StatusChip(text: String, color: Color) {
    Text(
        prettyStatus(text),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
fun PrimaryButton(title: String, enabled: Boolean = true, loading: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = Primary.copy(0.4f))
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(20.dp), Color.White, 2.dp)
        else Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GhostButton(title: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
    ) { Text(title, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value,
        onChange,
        modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Border,
            focusedLabelColor = Navy,
            cursorColor = Primary
        )
    )
}

@Composable
fun ErrorText(message: String?) {
    AnimatedVisibility(visible = !message.isNullOrBlank(), enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
        Text(message.orEmpty(), color = Danger, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProgressLine(done: Int, total: Int) {
    val fraction = if (total == 0) 0f else done.toFloat() / total
    LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
        color = Primary,
        trackColor = Pale
    )
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Navy, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) Text(subtitle, color = Muted, fontSize = 14.sp)
    }
}

@Composable
fun KeyRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
fun AppScreen(
    hero: @Composable (() -> Unit)? = null,
    safeBottom: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .then(if (hero == null) Modifier.statusBarsPadding() else Modifier)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .then(if (safeBottom) Modifier.navigationBarsPadding() else Modifier)
    ) {
        hero?.invoke()
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun FadeInColumn(safeBottom: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    AppScreen(safeBottom = safeBottom, content = content)
}

@Composable
fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Navy) }
        }
        Text(title, color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BrandMark(size: Int = 72) {
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.24).dp)).background(Primary),
        contentAlignment = Alignment.Center
    ) {
        Text("+", color = Color.White, fontSize = (size * 0.46).sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun NavyHero(title: String, subtitle: String, extra: @Composable ColumnScope.() -> Unit = {}) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(NavyBrush)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BrandMark(56)
        Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = Color.White.copy(0.78f), fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        extra()
    }
}

@Composable
fun OtpBoxes(value: String, onChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    BasicTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit).take(6)) },
        modifier = Modifier.fillMaxWidth().focusRequester(focus),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Primary),
        decorationBox = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { index ->
                    val filled = value.getOrNull(index)?.toString() ?: ""
                    val active = value.length == index
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(2.dp, if (active) Primary else Border, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(filled, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    )
}

@Composable
fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                Modifier
                    .height(6.dp)
                    .width(if (index == current) 22.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index <= current) Primary else Border)
            )
        }
    }
}

@Composable
fun ActionTile(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .heightIn(min = 108.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Pale), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Navy, modifier = Modifier.size(16.dp))
        }
        Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PulseDot(color: Color) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0.78f, 1.18f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "scale"
    )
    Box(Modifier.size(14.dp).scale(pulse).clip(CircleShape).background(color))
}

@Composable
fun LoadingScrim(visible: Boolean, label: String = "Validating…") {
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.fillMaxSize().background(Navy.copy(0.38f)), contentAlignment = Alignment.Center) {
            Column(
                Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = Primary)
                Text(label, color = Navy, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RowScope.QuickMetric(value: String) {
    Text(value, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
}

@Composable
fun RowScope.QuickStat(label: String, value: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
        Text(label, color = Color.White.copy(0.7f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
