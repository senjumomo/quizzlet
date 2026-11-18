package com.quizpulse.app

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quizpulse.app.data.BestScore
import com.quizpulse.app.data.Filters
import com.quizpulse.app.data.Prefs
import com.quizpulse.app.data.Question
import com.quizpulse.app.data.QuestionPicker
import com.quizpulse.app.data.SheetRepository
import com.quizpulse.app.fx.Haptic
import com.quizpulse.app.fx.Haptics
import com.quizpulse.app.fx.SoundEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class Screen { Setup, Loading, Quiz, Results }

sealed interface SheetStatus {
    data object Syncing : SheetStatus
    data class Ready(val totalQuestions: Int) : SheetStatus
    data object Offline : SheetStatus
}

enum class RetryAction { LoadMetadata, StartQuiz }

data class ErrorBanner(val message: String, val retry: RetryAction)

data class QuizState(
    val questions: List<Question>,
    val index: Int = 0,
    val choices: List<String>,
    val selected: String? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val streakPops: Int = 0,
    val startedAt: Long,
) {
    val current: Question get() = questions[index]
    val answered: Boolean get() = selected != null
    val isLast: Boolean get() = index + 1 >= questions.size
}

data class ResultState(
    val score: Int,
    val total: Int,
    val accuracy: Int,
    val maxStreak: Int,
    val seconds: Long,
    val title: String,
    val description: String,
    val bestLine: String,
    val celebrate: Boolean,
)

data class UiState(
    val screen: Screen = Screen.Setup,
    val filters: Filters = Filters(),
    val soundOn: Boolean = true,
    val sheetStatus: SheetStatus = SheetStatus.Syncing,
    val categories: Map<String, List<String>> = emptyMap(),
    val error: ErrorBanner? = null,
    val quiz: QuizState? = null,
    val result: ResultState? = null,
)

class QuizViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val repo = SheetRepository()
    private val sound = SoundEngine()
    private val haptics = Haptics(app)

    private val _state = MutableStateFlow(
        UiState(filters = prefs.loadFilters(), soundOn = prefs.soundEnabled)
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        sound.enabled = _state.value.soundOn
        loadMetadata()
    }

    fun tap() = haptics.trigger(Haptic.Tap)

    fun toggleSound() {
        val on = !_state.value.soundOn
        sound.enabled = on
        prefs.soundEnabled = on
        _state.update { it.copy(soundOn = on) }
        tap()
    }

    private fun updateFilters(block: (Filters) -> Filters) {
        _state.update { it.copy(filters = block(it.filters)) }
        prefs.saveFilters(_state.value.filters)
        tap()
    }

    fun setAmount(n: Int) = updateFilters { it.copy(amount = n) }
    fun setDifficulty(d: String) = updateFilters { it.copy(difficulty = d) }
    fun setCategory(c: String) = updateFilters { it.copy(category = c, subcategory = "") }
    fun setSubcategory(s: String) = updateFilters { it.copy(subcategory = s) }

    fun retry() {
        val action = _state.value.error?.retry ?: return
        _state.update { it.copy(error = null) }
        when (action) {
            RetryAction.LoadMetadata -> loadMetadata()
            RetryAction.StartQuiz -> startQuiz()
        }
    }

    fun loadMetadata() {
        _state.update { it.copy(sheetStatus = SheetStatus.Syncing) }
        viewModelScope.launch {
            try {
                val data = repo.load()
                _state.update { s ->
                    // Drop a saved category/subcategory that no longer exists in the sheet.
                    val f = s.filters
                    val subs = data.categories[f.category]
                    val fixed = when {
                        f.category.isEmpty() -> f.copy(subcategory = "")
                        subs == null -> f.copy(category = "", subcategory = "")
                        f.subcategory.isNotEmpty() && f.subcategory !in subs -> f.copy(subcategory = "")
                        else -> f
                    }
                    s.copy(
                        sheetStatus = SheetStatus.Ready(data.totalRows),
                        categories = data.categories,
                        filters = fixed,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        sheetStatus = SheetStatus.Offline,
                        error = ErrorBanner(e.message ?: "Could not load the Google Sheet.", RetryAction.LoadMetadata),
                    )
                }
            }
        }
    }

    fun startQuiz() {
        tap()
        _state.update { it.copy(screen = Screen.Loading, error = null) }
        viewModelScope.launch {
            try {
                val data = repo.load()
                if (_state.value.sheetStatus !is SheetStatus.Ready) {
                    _state.update { it.copy(sheetStatus = SheetStatus.Ready(data.totalRows), categories = data.categories) }
                }
                val picked = QuestionPicker.pick(data.questions, _state.value.filters)
                if (_state.value.screen != Screen.Loading) return@launch // user backed out
                _state.update {
                    it.copy(
                        screen = Screen.Quiz,
                        quiz = QuizState(
                            questions = picked,
                            choices = shuffledChoices(picked[0]),
                            startedAt = SystemClock.elapsedRealtime(),
                        ),
                        result = null,
                    )
                }
            } catch (e: Exception) {
                if (_state.value.screen != Screen.Loading) return@launch
                _state.update {
                    it.copy(
                        screen = Screen.Setup,
                        error = ErrorBanner(e.message ?: "Error connecting to Google Sheet.", RetryAction.StartQuiz),
                    )
                }
            }
        }
    }

    private fun shuffledChoices(q: Question) = (q.incorrectAnswers + q.correctAnswer).shuffled()

    fun choose(choice: String) {
        val q = _state.value.quiz ?: return
        if (q.answered) return
        val correct = choice == q.current.correctAnswer
        val next = if (correct) {
            val streak = q.streak + 1
            q.copy(
                selected = choice,
                score = q.score + 1,
                streak = streak,
                maxStreak = maxOf(q.maxStreak, streak),
                streakPops = q.streakPops + 1,
            )
        } else {
            q.copy(selected = choice, streak = 0)
        }
        _state.update { it.copy(quiz = next) }
        if (correct) {
            sound.playCorrect(); haptics.trigger(Haptic.Correct)
        } else {
            sound.playWrong(); haptics.trigger(Haptic.Wrong)
        }
    }

    fun next() {
        val q = _state.value.quiz ?: return
        if (!q.answered) return
        tap()
        if (q.isLast) {
            showResults(q)
        } else {
            val i = q.index + 1
            _state.update {
                it.copy(quiz = q.copy(index = i, selected = null, choices = shuffledChoices(q.questions[i])))
            }
        }
    }

    private fun showResults(q: QuizState) {
        val total = q.questions.size
        val accuracy = (q.score * 100.0 / total).roundToInt()
        val seconds = ((SystemClock.elapsedRealtime() - q.startedAt) / 1000.0).roundToInt().toLong()

        val best = prefs.best
        val isNewBest = best == null || accuracy > best.accuracy ||
            (accuracy == best.accuracy && total > best.total)
        val bestLine = if (isNewBest) {
            prefs.best = BestScore(accuracy, q.score, total)
            if (best != null) "🏅 New personal best: ${q.score}/$total ($accuracy%)" else ""
        } else {
            "Personal best: ${best!!.score}/${best.total} (${best.accuracy}%)"
        }

        val (title, desc) = when {
            accuracy == 100 -> "Perfection! 🏆" to "Remarkable trivia mastery across your Google Sheet questions!"
            accuracy >= 80 -> "Mastermind! 🌟" to "Remarkable trivia mastery across your Google Sheet questions!"
            accuracy >= 50 -> "Solid Run! 👍" to "Great effort! Practice makes perfect."
            else -> "Keep Training! 📚" to "Check your Google Sheet and come back stronger."
        }
        val celebrate = accuracy >= 80

        _state.update {
            it.copy(
                screen = Screen.Results,
                result = ResultState(q.score, total, accuracy, q.maxStreak, seconds, title, desc, bestLine, celebrate),
            )
        }
        if (celebrate) {
            sound.playFinish(); haptics.trigger(Haptic.Finish)
        }
    }

    fun backToSetup() {
        tap()
        _state.update { it.copy(screen = Screen.Setup, quiz = null) }
    }

    fun shareText(): String {
        val r = _state.value.result ?: return ""
        return "I just scored ${r.score}/${r.total} (${r.accuracy}%) on QuizPulse! " +
            "Can you beat my streak of ${r.maxStreak}? 🔥 Play here: ${com.quizpulse.app.data.Config.WEB_URL}"
    }
}
