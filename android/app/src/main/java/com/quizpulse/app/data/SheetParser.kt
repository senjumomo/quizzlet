package com.quizpulse.app.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SheetException(message: String) : Exception(message)

/**
 * Parses the Google Visualization (gviz) response for the question sheet.
 *
 * Columns: Category | Subcategory | Difficulty | Question | Correct | Incorrect 1..3
 */
object SheetParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(raw: String): SheetData {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) throw SheetException("Could not read Google Sheet data.")
        val root = try {
            json.parseToJsonElement(raw.substring(start, end + 1)).jsonObject
        } catch (e: Exception) {
            throw SheetException("Could not read Google Sheet data.")
        }

        val status = root["status"]?.jsonPrimitive?.contentOrNull
        if (status != "ok") {
            val msg = (root["errors"] as? JsonArray)?.firstOrNull()
                ?.let { (it as? JsonObject)?.get("message")?.jsonPrimitive?.contentOrNull }
            throw SheetException(msg ?: "Could not read Google Sheet data.")
        }
        val rows = (root["table"] as? JsonObject)?.get("rows") as? JsonArray
            ?: throw SheetException("Could not read Google Sheet data.")

        val cells: List<List<String?>> = rows.map { row ->
            val c = ((row as? JsonObject)?.get("c") as? JsonArray) ?: JsonArray(emptyList())
            c.map { cell -> cellValue(cell) }
        }
        return build(cells)
    }

    private fun cellValue(cell: kotlinx.serialization.json.JsonElement): String? {
        if (cell is JsonNull) return null
        val v = (cell as? JsonObject)?.get("v") ?: return null
        if (v is JsonNull) return null
        val p = v as? JsonPrimitive ?: return v.toString()
        val content = p.content
        // Render whole numbers without a trailing ".0" (gviz returns numbers as doubles).
        if (!p.isString) {
            content.toDoubleOrNull()?.let { d ->
                if (d == Math.floor(d) && !d.isInfinite() && kotlin.math.abs(d) < 1e15) return d.toLong().toString()
            }
        }
        return content
    }

    fun build(cells: List<List<String?>>): SheetData {
        val catMap = sortedMapOf<String, MutableSet<String>>()
        var totalRows = 0
        val questions = mutableListOf<Question>()

        for (c in cells) {
            fun at(i: Int) = c.getOrNull(i)?.trim()
            val cat = at(0).orEmpty()
            val sub = at(1).orEmpty()
            if (cat.isNotEmpty()) {
                totalRows++
                val set = catMap.getOrPut(cat) { sortedSetOf() }
                if (sub.isNotEmpty() && !sub.equals("general", ignoreCase = true)) set.add(sub)
            }
            val q = Question(
                category = cat.ifEmpty { "General" },
                subcategory = sub,
                difficulty = at(2)?.lowercase()?.ifEmpty { null } ?: "medium",
                question = at(3).orEmpty(),
                correctAnswer = at(4).orEmpty(),
                incorrectAnswers = listOf(at(5), at(6), at(7)).filterNotNull().filter { it.isNotEmpty() },
            )
            if (q.question.isNotEmpty() && q.correctAnswer.isNotEmpty() && q.incorrectAnswers.isNotEmpty()) {
                questions += q
            }
        }
        return SheetData(questions, totalRows, catMap.mapValues { it.value.toList() })
    }
}

object QuestionPicker {
    /** Same matching rules as the web app. */
    fun filter(pool: List<Question>, f: Filters): List<Question> {
        var out = pool
        if (f.category.isNotEmpty()) {
            val t = f.category.lowercase()
            out = out.filter { val c = it.category.lowercase(); c.contains(t) || t.contains(c) }
        }
        if (f.subcategory.isNotEmpty()) {
            val t = f.subcategory.lowercase()
            out = out.filter { it.subcategory.isNotEmpty() && it.subcategory.lowercase() == t }
        }
        if (f.difficulty.isNotEmpty()) {
            val t = f.difficulty.lowercase()
            out = out.filter { it.difficulty == t }
        }
        return out
    }

    fun pick(pool: List<Question>, f: Filters, random: kotlin.random.Random = kotlin.random.Random): List<Question> {
        val matches = filter(pool, f)
        if (matches.isEmpty()) {
            throw SheetException("No questions found matching your chosen filters. Try another category or difficulty!")
        }
        return matches.shuffled(random).take(f.amount)
    }
}
