package com.quizpulse.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizpulse.app.ui.theme.QP

@Composable
fun LoadingScreen() {
    val spin = rememberInfiniteTransition(label = "spin")
    val angle by spin.animateFloat(0f, 360f, infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart), label = "angle")
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(68.dp).rotate(angle)) {
            val w = 3.dp.toPx()
            val st = Stroke(w)
            val inset = androidx.compose.ui.geometry.Offset(w / 2, w / 2)
            val sz = androidx.compose.ui.geometry.Size(size.width - w, size.height - w)
            drawArc(QP.Primary.copy(alpha = 0.2f), 0f, 360f, false, inset, sz, style = st)
            drawArc(QP.Primary, -135f, 90f, false, inset, sz, style = st)
            drawArc(QP.Accent, -45f, 90f, false, inset, sz, style = st)
        }
        Spacer(Modifier.height(20.dp))
        Text("Fetching Questions", color = QP.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Preparing your trivia challenge...",
            color = QP.TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}
