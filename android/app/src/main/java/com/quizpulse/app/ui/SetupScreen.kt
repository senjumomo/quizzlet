package com.quizpulse.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizpulse.app.SheetStatus
import com.quizpulse.app.UiState
import com.quizpulse.app.data.Config
import com.quizpulse.app.ui.theme.QP

private val CATEGORY_EMOJIS = mapOf(
    "mythology" to "⚡",
    "science" to "🔬",
    "history" to "🏛️",
    "general knowledge" to "💡",
    "entertainment" to "🎬",
    "video games" to "🎮",
    "series" to "📺",
    "cryptids" to "👾",
)

fun categoryEmoji(name: String) = CATEGORY_EMOJIS[name.lowercase().trim()] ?: "🎯"

@Composable
fun SetupScreen(
    state: UiState,
    onAmount: (Int) -> Unit,
    onDifficulty: (String) -> Unit,
    onCategory: (String) -> Unit,
    onSubcategory: (String) -> Unit,
    onRetry: () -> Unit,
    onStart: () -> Unit,
) {
    val f = state.filters
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp)
        ) {
            // Hero
            Spacer(Modifier.height(12.dp))
            Text("Challenge Your Brain", color = QP.Text, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp, lineHeight = 34.sp)
            Spacer(Modifier.height(6.dp))
            Text("Real-time trivia pulled straight from your Google Sheet.", color = QP.TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            StatusPill(state.sheetStatus)
            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(state.error != null, enter = fadeIn(), exit = fadeOut()) {
                ErrorBannerView(state.error?.message.orEmpty(), onRetry)
            }

            SectionTitle("Question Count")
            SegmentedPills(
                options = Config.AMOUNTS.map { it to it.toString() },
                selected = f.amount,
                onSelect = onAmount,
            )

            SectionTitle("Difficulty")
            SegmentedPills(options = Config.DIFFICULTIES, selected = f.difficulty, onSelect = onDifficulty)

            val badge = when (val s = state.sheetStatus) {
                SheetStatus.Syncing -> "Auto Syncing..."
                is SheetStatus.Ready -> "${s.totalQuestions} Questions"
                SheetStatus.Offline -> "Not connected"
            }
            SectionTitle("Category", badge)
            val chips = listOf("" to "Any Category") + state.categories.keys.map { it to it }
            chips.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { (key, label) ->
                        CategoryChip(
                            emoji = if (key.isEmpty()) "🌟" else categoryEmoji(key),
                            name = label,
                            active = f.category == key,
                            onClick = { onCategory(key) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            val subs = state.categories[f.category].orEmpty()
            AnimatedVisibility(f.category.isNotEmpty() && subs.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Column {
                    SectionTitle("Subcategory")
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SubcatPill("All Subcategories", f.subcategory.isEmpty()) { onSubcategory("") }
                        subs.forEach { sub -> SubcatPill(sub, f.subcategory == sub) { onSubcategory(sub) } }
                    }
                }
            }
        }

        // Sticky CTA with fade into background
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .bleedHorizontal(20.dp)
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.25f to QP.Bg, 1f to QP.Bg))
                .padding(start = 20.dp, end = 20.dp).padding(top = 24.dp, bottom = 8.dp)
        ) {
            PrimaryButton("Start Trivia", onStart)
        }
    }
}

@Composable
private fun StatusPill(status: SheetStatus) {
    val (text, color, fg) = when (status) {
        SheetStatus.Syncing -> Triple("Syncing Sheet Database…", QP.Amber, Color(0xFFFCD34D))
        is SheetStatus.Ready -> Triple("Connected to Sheet Database", QP.Success, Color(0xFF6EE7B7))
        SheetStatus.Offline -> Triple("Sheet Database Offline", QP.Error, Color(0xFFFDA4AF))
    }
    val pulse = rememberInfiniteTransition(label = "pulse")
    val a by pulse.animateFloat(0.45f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    Row(
        Modifier
            .clip(QP.Pill)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), QP.Pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(6.dp)
                .alpha(if (status is SheetStatus.Syncing) a else 1f)
                .shadow(4.dp, CircleShape, ambientColor = color, spotColor = color)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.size(6.dp))
        Text(text, color = fg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorBannerView(message: String, onRetry: () -> Unit) {
    val shape = QP.RadiusMd
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(shape)
            .background(Color(0x1FEF4444))
            .border(1.dp, Color(0x66EF4444), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, color = Color(0xFFFECACA), fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .clip(QP.RadiusSm)
                .background(Color(0xFFEF4444))
                .pressable(onClick = onRetry)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("Retry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, info: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title.uppercase(), color = QP.TextMuted, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        if (info != null) Text(info, color = QP.Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun <T> SegmentedPills(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .glassCard(QP.RadiusMd, bg = QP.Surface3)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            val textColor by animateColorAsState(if (active) Color.White else QP.TextMuted, label = "pillText")
            Box(
                Modifier
                    .weight(1f)
                    .then(if (active) Modifier.shadow(8.dp, QP.RadiusSm, ambientColor = QP.Primary, spotColor = QP.Primary) else Modifier)
                    .clip(QP.RadiusSm)
                    .then(if (active) Modifier.background(QP.PrimaryGradient) else Modifier)
                    .pressable(pressedScale = 0.95f, role = Role.RadioButton) { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryChip(emoji: String, name: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = QP.RadiusMd
    Box(
        modifier
            .heightIn(min = 60.dp)
            .then(if (active) Modifier.shadow(12.dp, shape, ambientColor = QP.Primary, spotColor = QP.Primary) else Modifier)
            .glassCard(
                shape,
                bg = if (active) Color(0x296366F1) else QP.CardBg,
                border = if (active) QP.Violet else QP.CardBorder,
            )
            .pressable(role = Role.RadioButton, onClick = onClick)
    ) {
        if (active) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(QP.PrimaryGradient).align(Alignment.TopCenter))
        }
        Row(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(QP.Surface5),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 20.sp) }
            Spacer(Modifier.size(10.dp))
            Text(name, color = QP.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun SubcatPill(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(QP.Pill)
            .background(if (active) Color(0x3306B6D4) else QP.Surface5)
            .border(1.dp, if (active) Color(0x6606B6D4) else QP.CardBorder, QP.Pill)
            .pressable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, color = if (active) Color(0xFF67E8F9) else QP.TextMuted, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
