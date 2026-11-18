package com.quizpulse.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object QP {
    val Bg = Color(0xFF07090E)
    val CardBg = Color(0xC7121826)
    val CardBorder = Color(0x14FFFFFF)
    val Surface3 = Color(0x08FFFFFF)
    val Surface5 = Color(0x0DFFFFFF)
    val Primary = Color(0xFF6366F1)
    val Violet = Color(0xFF8B5CF6)
    val Accent = Color(0xFF06B6D4)
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFF43F5E)
    val Amber = Color(0xFFF59E0B)
    val Text = Color(0xFFF8FAFC)
    val TextMuted = Color(0xFF94A3B8)
    val TextDim = Color(0xFF64748B)

    val PrimaryGradient = Brush.linearGradient(listOf(Primary, Violet))

    val RadiusXl = RoundedCornerShape(28.dp)
    val RadiusLg = RoundedCornerShape(20.dp)
    val RadiusMd = RoundedCornerShape(14.dp)
    val RadiusSm = RoundedCornerShape(10.dp)
    val Pill = RoundedCornerShape(50)
}

@Composable
fun QuizPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = QP.Primary,
            secondary = QP.Accent,
            background = QP.Bg,
            surface = Color(0xFF121826),
            onBackground = QP.Text,
            onSurface = QP.Text,
            error = QP.Error,
        ),
        content = content,
    )
}
