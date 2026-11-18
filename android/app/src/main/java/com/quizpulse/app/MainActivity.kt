package com.quizpulse.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizpulse.app.data.Config
import com.quizpulse.app.ui.BoltIcon
import com.quizpulse.app.ui.ConfettiBurst
import com.quizpulse.app.ui.GlowBackground
import com.quizpulse.app.ui.LoadingScreen
import com.quizpulse.app.ui.QuizScreen
import com.quizpulse.app.ui.ResultsScreen
import com.quizpulse.app.ui.SetupScreen
import com.quizpulse.app.ui.SheetIcon
import com.quizpulse.app.ui.SoundIcon
import com.quizpulse.app.ui.pressable
import com.quizpulse.app.ui.theme.QP
import com.quizpulse.app.ui.theme.QuizPulseTheme

class MainActivity : ComponentActivity() {
    private val vm: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bar = SystemBarStyle.dark(QP.Bg.copy(alpha = 0f).toArgb())
        enableEdgeToEdge(statusBarStyle = bar, navigationBarStyle = bar)
        setContent {
            QuizPulseTheme {
                val state by vm.state.collectAsStateWithLifecycle()
                App(
                    state = state,
                    vm = vm,
                    openSheet = { openUrl(Config.SHEET_URL) },
                    share = { share(vm.shareText()) },
                )
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "QuizPulse — Trivia Challenge")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, "Share Score"))
    }
}

@Composable
private fun App(state: UiState, vm: QuizViewModel, openSheet: () -> Unit, share: () -> Unit) {
    var confirmQuit by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = state.screen != Screen.Setup) {
        when (state.screen) {
            Screen.Quiz -> confirmQuit = true
            else -> vm.backToSetup()
        }
    }

    GlowBackground {
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Header(soundOn = state.soundOn, onToggleSound = vm::toggleSound, onOpenSheet = openSheet)
                AnimatedContent(
                    targetState = state.screen,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 40 }) togetherWith fadeOut(tween(150))
                    },
                    label = "screen",
                ) { screen ->
                    when (screen) {
                        Screen.Setup -> SetupScreen(
                            state = state,
                            onAmount = vm::setAmount,
                            onDifficulty = vm::setDifficulty,
                            onCategory = vm::setCategory,
                            onSubcategory = vm::setSubcategory,
                            onRetry = vm::retry,
                            onStart = vm::startQuiz,
                        )
                        Screen.Loading -> LoadingScreen()
                        Screen.Quiz -> state.quiz?.let { QuizScreen(it, vm::choose, vm::next) } ?: Box(Modifier.fillMaxSize())
                        Screen.Results -> state.result?.let { ResultsScreen(it, vm::backToSetup, share) } ?: Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        val result = state.result
        if (state.screen == Screen.Results && result != null && result.celebrate) {
            ConfettiBurst(trigger = result)
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            containerColor = Color(0xFF121826),
            title = { Text("Quit this round?") },
            text = { Text("Your progress in this round will be lost.", color = QP.TextMuted) },
            confirmButton = {
                TextButton(onClick = { confirmQuit = false; vm.backToSetup() }) { Text("Quit", color = QP.Error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmQuit = false }) { Text("Keep Playing", color = QP.Text) }
            },
        )
    }
}

@Composable
private fun Header(soundOn: Boolean, onToggleSound: () -> Unit, onOpenSheet: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = QP.Primary, spotColor = QP.Primary)
                .clip(RoundedCornerShape(12.dp))
                .background(QP.PrimaryGradient),
            contentAlignment = Alignment.Center,
        ) { BoltIcon() }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "QuizPulse",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color.White, Color.White, Color(0xFFC7D2FE))),
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp,
                ),
            )
            Text("SHEET ENGINE", color = QP.Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp)
        }
        IconCircle(
            description = "View Google Sheet database",
            bg = Color(0x1A10B981), border = Color(0x4D10B981), onClick = onOpenSheet,
        ) { SheetIcon(color = Color(0xFF34D399)) }
        Spacer(Modifier.size(8.dp))
        IconCircle(
            description = if (soundOn) "Mute sound effects" else "Unmute sound effects",
            bg = QP.Surface5, border = QP.CardBorder, onClick = onToggleSound,
        ) { SoundIcon(on = soundOn, color = QP.TextMuted) }
    }
}

@Composable
private fun IconCircle(description: String, bg: Color, border: Color, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .pressable(pressedScale = 0.92f, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { icon() }
}
