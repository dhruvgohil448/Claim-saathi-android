package com.claimsaathi.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val SaathiBrush = Brush.linearGradient(listOf(Primary, Navy))

@Composable
fun shimmerAlpha(): Float {
    val a by rememberInfiniteTransition(label = "shimmer").animateFloat(
        0.45f, 1f, infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse), label = "a"
    )
    return a
}

@Composable
fun SkeletonCard(lines: Int = 3) {
    val a = shimmerAlpha()
    AppCard(Modifier.alpha(a)) {
        Box(Modifier.width(110.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Border))
        repeat(lines) { i ->
            Box(Modifier.then(if (i == lines - 1) Modifier.width(170.dp) else Modifier.fillMaxWidth()).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Border))
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(60.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(28.dp))
        }
        Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(message, color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun UploadProgressBar(done: Int, total: Int) {
    val complete = total > 0 && done >= total
    val fraction by animateFloatAsState(if (total == 0) 0f else (done.coerceAtMost(total)).toFloat() / total, tween(500), label = "p")
    val tint = if (complete) Success else Primary
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(if (complete) Icons.Filled.CheckCircle else Icons.Filled.UploadFile, null, tint = tint, modifier = Modifier.size(18.dp))
            Text(if (complete) "All documents verified" else "Documents verified", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$done/$total", color = tint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)).background(Border)) {
            Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(8.dp)).background(tint))
        }
    }
}

@Composable
fun SaathiAvatar(size: Int = 30) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(SaathiBrush), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size((size * 0.5).dp))
    }
}

/** Small bot bubble explaining the current step / next document. */
@Composable
fun SaathiTip(text: String, onAsk: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Primary.copy(alpha = 0.08f))
            .border(1.dp, Primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SaathiAvatar()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Saathi tip", color = Primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(text, color = Navy, fontSize = 13.sp)
            if (onAsk != null) Text("Ask Saathi →", color = Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onAsk))
        }
    }
}

@Composable
fun TypingDots() {
    val t = rememberInfiniteTransition(label = "typing")
    Row(
        Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(3) { i ->
            val a by t.animateFloat(0.25f, 1f, infiniteRepeatable(tween(500, delayMillis = i * 160), RepeatMode.Reverse), label = "d$i")
            Box(Modifier.size(7.dp).alpha(a).clip(CircleShape).background(Muted))
        }
    }
}

/** Floating "Ask Saathi" pill, to be placed inside a Box. */
@Composable
fun BoxScope.SaathiFab(onClick: () -> Unit) {
    Row(
        Modifier.align(Alignment.BottomEnd).padding(16.dp)
            .shadow(10.dp, CircleShape, ambientColor = Navy.copy(0.25f), spotColor = Navy.copy(0.3f))
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Primary, Color(0xFF0073D9))))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Text("Ask Saathi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

val QuickActions = listOf("Track my claim", "Explain deductions", "What documents are missing?", "Explain my policy in Hindi")
