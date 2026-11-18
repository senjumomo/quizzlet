package com.quizpulse.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizpulse.app.ResultState
import com.quizpulse.app.ui.theme.QP

@Composable
fun ResultsScreen(result: ResultState, onReplay: () -> Unit, onShare: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(10.dp))
        ScoreGauge(result)
        Spacer(Modifier.height(20.dp))
        Text(result.title, color = QP.Text, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(result.description, color = QP.TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 12.dp))
        if (result.bestLine.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(result.bestLine, color = QP.Text, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.alpha(0.75f))
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox("${result.accuracy}%", "Accuracy", Modifier.weight(1f))
            StatBox("${result.maxStreak}", "Streak 🔥", Modifier.weight(1f))
            StatBox("${result.seconds}s", "Time", Modifier.weight(1f))
        }
        Spacer(Modifier.height(28.dp))
        PrimaryButton("Play Another Round", onReplay, trailing = { ReplayIcon() })
        Spacer(Modifier.height(12.dp))
        SecondaryButton("Share Score", onShare, leading = { ShareIcon(color = QP.Text) })
    }
}

@Composable
private fun ScoreGauge(result: ResultState) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(result) {
        sweep.snapTo(0f)
        sweep.animateTo(result.accuracy / 100f, tween(1000, delayMillis = 100, easing = FastOutSlowInEasing))
    }
    Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = 10.dp.toPx() // circle r=70 in a 160 box
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val tl = Offset(inset, inset)
            drawArc(Color(0x0FFFFFFF), 0f, 360f, false, tl, arcSize, style = Stroke(stroke))
            rotate(-90f) {
                drawArc(
                    brush = Brush.linearGradient(listOf(QP.Success, QP.Accent), Offset.Zero, Offset(size.width, size.height)),
                    startAngle = 0f,
                    sweepAngle = 360f * sweep.value,
                    useCenter = false,
                    topLeft = tl,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${result.score}/${result.total}", color = QP.Text, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("SCORE", color = QP.TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun StatBox(value: String, title: String, modifier: Modifier = Modifier) {
    Column(
        modifier.glassCard(QP.RadiusMd).padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = QP.Text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(2.dp))
        Text(title, color = QP.TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
