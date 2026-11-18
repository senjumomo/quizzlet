package com.quizpulse.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizpulse.app.QuizState
import com.quizpulse.app.ui.theme.QP

private data class DiffStyle(val bg: Color, val border: Color, val fg: Color)

private fun diffStyle(d: String) = when (d) {
    "easy" -> DiffStyle(Color(0x2610B981), Color(0x4D10B981), Color(0xFF6EE7B7))
    "hard" -> DiffStyle(Color(0x26F43F5E), Color(0x4DF43F5E), Color(0xFFFDA4AF))
    else -> DiffStyle(Color(0x26F59E0B), Color(0x4DF59E0B), Color(0xFFFCD34D))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(quiz: QuizState, onChoose: (String) -> Unit, onNext: () -> Unit) {
    val q = quiz.current
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (quiz.answered) 100.dp else 16.dp)
        ) {
            // HUD
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlowRow(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TagPill(q.category, Color(0x266366F1), Color(0x4D6366F1), Color(0xFFA5B4FC))
                    if (q.subcategory.isNotEmpty() && !q.subcategory.equals("general", true)) {
                        TagPill(q.subcategory, Color(0x2606B6D4), Color(0x4D06B6D4), Color(0xFF67E8F9))
                    }
                    val ds = diffStyle(q.difficulty)
                    TagPill(q.difficulty.replaceFirstChar { it.uppercase() }, ds.bg, ds.border, ds.fg)
                }
                Spacer(Modifier.size(8.dp))
                StreakMeter(quiz.streak, quiz.streakPops)
            }

            // Progress
            val progress by animateFloatAsState(
                quiz.index.toFloat() / quiz.questions.size,
                tween(350, easing = FastOutSlowInEasing), label = "progress",
            )
            Box(
                Modifier.fillMaxWidth().height(6.dp).clip(QP.RadiusSm).background(Color(0x0FFFFFFF))
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(QP.RadiusSm).background(QP.PrimaryGradient))
            }
            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = quiz.index,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 30 }) togetherWith fadeOut(tween(120))
                },
                label = "question",
            ) { idx ->
                // Use the state for this index only while it's current; AnimatedContent may render the old one briefly.
                val shown = if (idx == quiz.index) quiz else null
                Column {
                    QuestionCard(
                        label = "Question ${idx + 1} of ${quiz.questions.size}",
                        text = quiz.questions[idx].question,
                    )
                    Spacer(Modifier.height(20.dp))
                    if (shown != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            shown.choices.forEachIndexed { i, choice ->
                                val state = when {
                                    !shown.answered -> OptState.Idle
                                    choice == shown.current.correctAnswer -> OptState.Correct
                                    choice == shown.selected -> OptState.Wrong
                                    else -> OptState.Disabled
                                }
                                OptionCard(
                                    letter = ('A' + i).toString(),
                                    text = choice,
                                    state = state,
                                    onClick = { onChoose(choice) },
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = quiz.answered,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 4 },
            exit = fadeOut(tween(100)) + slideOutVertically { it / 4 },
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .bleedHorizontal(20.dp)
                    .background(Brush.verticalGradient(0f to Color.Transparent, 0.25f to QP.Bg, 1f to QP.Bg))
                    .padding(start = 20.dp, end = 20.dp).padding(top = 16.dp, bottom = 8.dp)
            ) {
                PrimaryButton(if (quiz.isLast) "View Results" else "Next Question", onNext)
            }
        }
    }
}

@Composable
private fun StreakMeter(streak: Int, pops: Int) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pops) {
        if (pops > 0) {
            scale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    Row(
        Modifier
            .scale(scale.value)
            .clip(QP.Pill)
            .background(Color(0x1AF59E0B))
            .border(1.dp, Color(0x40F59E0B), QP.Pill)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🔥", fontSize = 13.sp)
        Spacer(Modifier.size(4.dp))
        Text("$streak", color = QP.Amber, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun QuestionCard(label: String, text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(20.dp, QP.RadiusXl, ambientColor = Color.Black, spotColor = Color.Black)
            .glassCard(QP.RadiusXl)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(label.uppercase(), color = QP.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Text(text, color = QP.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp, letterSpacing = (-0.2).sp)
    }
}

private enum class OptState { Idle, Correct, Wrong, Disabled }

@Composable
private fun OptionCard(letter: String, text: String, state: OptState, onClick: () -> Unit) {
    val shape = QP.RadiusLg
    val bg by animateColorAsState(
        when (state) {
            OptState.Correct -> Color(0x2E10B981)
            OptState.Wrong -> Color(0x2EF43F5E)
            else -> QP.Surface3
        }, label = "optBg",
    )
    val border by animateColorAsState(
        when (state) {
            OptState.Correct -> QP.Success
            OptState.Wrong -> QP.Error
            else -> QP.CardBorder
        }, label = "optBorder",
    )
    val badgeBg by animateColorAsState(
        when (state) {
            OptState.Correct -> QP.Success
            OptState.Wrong -> QP.Error
            else -> Color(0x0FFFFFFF)
        }, label = "badgeBg",
    )

    // Shake on wrong answer (same keyframes as the CSS animation).
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == OptState.Wrong) {
            shake.animateTo(0f, keyframes {
                durationMillis = 400
                -2f at 40; 3f at 80; -4f at 120; 4f at 160; -4f at 200; 4f at 240; -4f at 280; 3f at 320; -2f at 360; 0f at 400
            })
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .offset { IntOffset((shake.value * density).toInt(), 0) }
            .then(if (state == OptState.Correct) Modifier.shadow(16.dp, shape, ambientColor = QP.Success, spotColor = QP.Success) else Modifier)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .pressable(enabled = state == OptState.Idle, pressedScale = 0.98f, onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(badgeBg)
                .border(1.dp, if (state == OptState.Correct || state == OptState.Wrong) Color.Transparent else QP.CardBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                letter,
                color = if (state == OptState.Correct || state == OptState.Wrong) Color.White else QP.TextMuted,
                fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(Modifier.size(14.dp))
        Text(text, color = QP.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}
