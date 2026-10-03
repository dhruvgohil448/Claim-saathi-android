package com.claimsaathi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Navy.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) { content() }
}

@Composable
fun StatusBadge(title: String, tint: Color) {
    Text(
        title,
        color = tint,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

fun statusTint(status: ClaimStatus) = when (status) {
    ClaimStatus.Submitted, ClaimStatus.UnderReview -> Cyan
    ClaimStatus.DocsVerified, ClaimStatus.Approved, ClaimStatus.Settled -> Success
    ClaimStatus.ActionRequired -> Warning
}

fun docTint(status: DocStatus) = when (status) {
    DocStatus.Verified -> Success
    DocStatus.NeedsReview -> Warning
    DocStatus.Missing -> Danger
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 15.sp)
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Cyan)
        )
    }
}

@Composable
fun PrimaryButton(title: String, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Navy)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Pale)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LogoMark() {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Cyan),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "Hi!",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Success)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ProgressBar(fraction: Float, color: Color = Cyan) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Pale)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
        )
    }
}

@Composable
fun KeyValue(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
