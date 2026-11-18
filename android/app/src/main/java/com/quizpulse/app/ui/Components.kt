package com.quizpulse.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizpulse.app.ui.theme.QP

/** Ambient radial glows behind everything, mirroring the web's blurred blobs. */
@Composable
fun GlowBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(QP.Bg)) {
        Canvas(Modifier.fillMaxSize()) {
            fun glow(center: Offset, radius: Float, color: Color, alpha: Float) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(color.copy(alpha = alpha), Color.Transparent),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }
            val d = density
            glow(Offset(80 * d, 60 * d), 260 * d, QP.Primary, 0.40f)
            glow(Offset(size.width - 90 * d, size.height - 90 * d), 280 * d, QP.Accent, 0.38f)
            glow(Offset(size.width + 20 * d, size.height * 0.45f), 220 * d, QP.Violet, 0.22f)
        }
        content()
    }
}

/** Clickable with a press-scale effect instead of a ripple. */
@Composable
fun Modifier.pressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    role: Role = Role.Button,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) pressedScale else 1f, label = "press")
    return this
        .scale(scale)
        .clickable(interactionSource = source, indication = null, enabled = enabled, role = role, onClick = onClick)
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = { ChevronIcon() },
) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(18.dp, QP.RadiusLg, ambientColor = QP.Primary, spotColor = QP.Primary)
            .clip(QP.RadiusLg)
            .background(QP.PrimaryGradient)
            .pressable(pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(QP.RadiusLg)
            .background(QP.Surface5)
            .border(1.dp, QP.CardBorder, QP.RadiusLg)
            .pressable(pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = QP.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TagPill(text: String, bg: Color, border: Color, fg: Color) {
    Box(
        Modifier
            .clip(QP.Pill)
            .background(bg)
            .border(1.dp, border, QP.Pill)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
    }
}

fun Modifier.glassCard(shape: Shape, bg: Color = QP.CardBg, border: Color = QP.CardBorder) =
    this.clip(shape).background(bg).border(1.dp, border, shape)

// ---- Line icons drawn on Canvas (mirrors the inline SVGs in the web app) ----

@Composable
fun ChevronIcon(size: Dp = 20.dp, color: Color = Color.White) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 24f
        val p = Path().apply { moveTo(9 * s, 6 * s); lineTo(15 * s, 12 * s); lineTo(9 * s, 18 * s) }
        drawPath(p, color, style = Stroke(2.5f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun BoltIcon(size: Dp = 20.dp, color: Color = Color.White) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 24f
        val p = Path().apply {
            moveTo(13 * s, 2 * s); lineTo(3 * s, 14 * s); lineTo(12 * s, 14 * s); lineTo(11 * s, 22 * s)
            lineTo(21 * s, 10 * s); lineTo(12 * s, 10 * s); close()
        }
        drawPath(p, color)
    }
}

@Composable
fun SoundIcon(on: Boolean, size: Dp = 18.dp, color: Color) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 24f
        val st = Stroke(2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val speaker = Path().apply {
            moveTo(11 * s, 5 * s); lineTo(6 * s, 9 * s); lineTo(2 * s, 9 * s); lineTo(2 * s, 15 * s)
            lineTo(6 * s, 15 * s); lineTo(11 * s, 19 * s); close()
        }
        drawPath(speaker, color, style = st)
        if (on) {
            drawArc(color, -45f, 90f, false, Offset(5.5f * s, 5.5f * s), androidx.compose.ui.geometry.Size(13f * s, 13f * s), style = st)
            drawArc(color, -45f, 90f, false, Offset(1.5f * s, 1.5f * s), androidx.compose.ui.geometry.Size(21f * s, 21f * s), style = st)
        } else {
            drawLine(color, Offset(23 * s, 9 * s), Offset(17 * s, 15 * s), 2f * s, StrokeCap.Round)
            drawLine(color, Offset(17 * s, 9 * s), Offset(23 * s, 15 * s), 2f * s, StrokeCap.Round)
        }
    }
}

@Composable
fun SheetIcon(size: Dp = 18.dp, color: Color) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 24f
        drawRoundRect(color, Offset(3 * s, 3 * s), androidx.compose.ui.geometry.Size(18 * s, 18 * s),
            androidx.compose.ui.geometry.CornerRadius(2 * s))
        val bg = QP.Bg
        drawRect(bg, Offset(6 * s, 7 * s), androidx.compose.ui.geometry.Size(10 * s, 2 * s))
        drawRect(bg, Offset(6 * s, 11 * s), androidx.compose.ui.geometry.Size(10 * s, 2 * s))
        drawRect(bg, Offset(6 * s, 15 * s), androidx.compose.ui.geometry.Size(6 * s, 2 * s))
    }
}

@Composable
fun ShareIcon(size: Dp = 18.dp, color: Color) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 24f
        val st = Stroke(2f * s)
        drawCircle(color, 3 * s, Offset(18 * s, 5 * s), style = st)
        drawCircle(color, 3 * s, Offset(6 * s, 12 * s), style = st)
        drawCircle(color, 3 * s, Offset(18 * s, 19 * s), style = st)
        drawLine(color, Offset(8.59f * s, 13.51f * s), Offset(15.42f * s, 17.49f * s), 2f * s)
        drawLine(color, Offset(15.41f * s, 6.51f * s), Offset(8.59f * s, 10.49f * s), 2f * s)
    }
}

@Composable
fun ReplayIcon(size: Dp = 20.dp, color: Color = Color.White) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 24f
        val st = Stroke(2.5f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawArc(color, -30f, 300f, false, Offset(3 * s, 3 * s), androidx.compose.ui.geometry.Size(18 * s, 18 * s), style = st)
        val head = Path().apply { moveTo(20.5f * s, 2.5f * s); lineTo(20.5f * s, 8 * s); lineTo(15 * s, 8 * s) }
        drawPath(head, color, style = st)
    }
}

/** Extends a child's drawing horizontally past its parent's padding (for edge-to-edge scrims). */
fun Modifier.bleedHorizontal(amount: Dp) = this.layout { measurable, constraints ->
    val extra = amount.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra)
    )
    layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
}
