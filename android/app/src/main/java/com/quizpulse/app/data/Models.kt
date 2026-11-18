package com.quizpulse.app.data

data class Question(
    val category: String,
    val subcategory: String,
    val difficulty: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
)

data class SheetData(
    val questions: List<Question>,
    /** Number of rows that have a category (matches the web app's badge count). */
    val totalRows: Int,
    /** Category -> sorted subcategories (excluding "General"). */
    val categories: Map<String, List<String>>,
)

data class Filters(
    val amount: Int = 10,
    val difficulty: String = "",
    val category: String = "",
    val subcategory: String = "",
)

data class BestScore(val accuracy: Int, val score: Int, val total: Int)

object Config {
    const val SPREADSHEET_ID = "1o3kNHih-35T5-ssvNaJ9i4TAs38zSQHKyhBqTZ9AYX4"
    const val SHEET_URL = "https://docs.google.com/spreadsheets/d/$SPREADSHEET_ID/edit"
    const val WEB_URL = "https://senjumomo.github.io/quizzlet/"
    val AMOUNTS = listOf(5, 10, 15, 25)
    val DIFFICULTIES = listOf("" to "All", "easy" to "Easy", "medium" to "Med", "hard" to "Hard")
}
