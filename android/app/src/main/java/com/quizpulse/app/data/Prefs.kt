package com.quizpulse.app.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("quizpulse", Context.MODE_PRIVATE)

    fun loadFilters(): Filters = Filters(
        amount = sp.getInt("amount", 10).takeIf { it in Config.AMOUNTS } ?: 10,
        difficulty = sp.getString("diff", "")?.takeIf { d -> Config.DIFFICULTIES.any { it.first == d } } ?: "",
        category = sp.getString("cat", "").orEmpty(),
        subcategory = sp.getString("sub", "").orEmpty(),
    )

    fun saveFilters(f: Filters) {
        sp.edit()
            .putInt("amount", f.amount)
            .putString("diff", f.difficulty)
            .putString("cat", f.category)
            .putString("sub", f.subcategory)
            .apply()
    }

    var soundEnabled: Boolean
        get() = sp.getBoolean("sound", true)
        set(v) { sp.edit().putBoolean("sound", v).apply() }

    var best: BestScore?
        get() = if (!sp.contains("best_total")) null else BestScore(
            accuracy = sp.getInt("best_acc", 0),
            score = sp.getInt("best_score", 0),
            total = sp.getInt("best_total", 0),
        )
        set(v) {
            val e = sp.edit()
            if (v == null) e.remove("best_acc").remove("best_score").remove("best_total")
            else e.putInt("best_acc", v.accuracy).putInt("best_score", v.score).putInt("best_total", v.total)
            e.apply()
        }
}
