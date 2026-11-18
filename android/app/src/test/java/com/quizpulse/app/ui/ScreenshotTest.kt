package com.quizpulse.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.drawToBitmap
import androidx.compose.ui.unit.dp
import com.quizpulse.app.QuizState
import com.quizpulse.app.ResultState
import com.quizpulse.app.SheetStatus
import com.quizpulse.app.UiState
import com.quizpulse.app.data.Filters
import com.quizpulse.app.data.Question
import com.quizpulse.app.ui.theme.QuizPulseTheme
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Renders each screen on the JVM and writes PNGs. Skipped unless -Dscreenshots.dir is set.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val outDir = System.getProperty("screenshots.dir").orEmpty()

    private fun shot(name: String, content: @Composable () -> Unit) {
        assumeTrue(outDir.isNotEmpty())
        rule.setContent {
            QuizPulseTheme {
                GlowBackground {
                    Box(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 16.dp)) {
                        content()
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
        val bmp = rule.activity.window.decorView.drawToBitmap()
        File(outDir).mkdirs()
        File(outDir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private val cats = mapOf(
        "Cryptids" to emptyList(),
        "General Knowledge" to emptyList(),
        "History" to listOf("Ancient", "Modern"),
        "Mythology" to listOf("Egyptian", "Greek", "Norse"),
        "Science" to emptyList(),
        "Video Games" to emptyList(),
    )

    private val q = Question(
        "Mythology", "Norse", "easy",
        "What is the name of Thor's hammer in Norse mythology?",
        "Mjolnir", listOf("Gungnir", "Gram", "Hrunting"),
    )

    @Test fun setup() = shot("1-setup") {
        SetupScreen(
            UiState(
                filters = Filters(amount = 10, category = "Mythology", subcategory = "Greek"),
                sheetStatus = SheetStatus.Ready(128),
                categories = cats,
            ), {}, {}, {}, {}, {}, {},
        )
    }

    @Test fun setupError() = shot("2-setup-error") {
        SetupScreen(
            UiState(
                sheetStatus = SheetStatus.Offline,
                error = com.quizpulse.app.ErrorBanner("Failed to reach Google Sheet. Check your connection or share permissions.", com.quizpulse.app.RetryAction.LoadMetadata),
            ), {}, {}, {}, {}, {}, {},
        )
    }

    @Test fun quizAnswered() = shot("3-quiz") {
        QuizScreen(
            QuizState(
                questions = List(10) { q }, index = 3,
                choices = listOf("Gungnir", "Mjolnir", "Gram", "Hrunting"),
                selected = "Gram", score = 2, streak = 0, maxStreak = 2, startedAt = 0,
            ), {}, {},
        )
    }

    @Test fun results() = shot("4-results") {
        Column {
            ResultsScreen(
                ResultState(9, 10, 90, 6, 74, "Mastermind! 🌟",
                    "Remarkable trivia mastery across your Google Sheet questions!",
                    "🏅 New personal best: 9/10 (90%)", true),
                {}, {},
            )
        }
    }
}
