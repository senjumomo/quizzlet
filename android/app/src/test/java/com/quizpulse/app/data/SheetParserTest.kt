package com.quizpulse.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SheetParserTest {
    private val sample = """
        /*O_o*/
        google.visualization.Query.setResponse({"version":"0.6","status":"ok","table":{"cols":[],"rows":[
          {"c":[{"v":"Mythology"},{"v":"Greek"},{"v":"Easy"},{"v":"King of the gods?"},{"v":"Zeus"},{"v":"Poseidon"},{"v":"Hades"},{"v":"Apollo"}]},
          {"c":[{"v":"Mythology"},{"v":"Norse"},{"v":"Hard"},{"v":"Thor's hammer?"},{"v":"Mjolnir"},{"v":"Gungnir"},null,{"v":" "}]},
          {"c":[{"v":"Science"},{"v":"General"},{"v":"Medium"},{"v":"Atomic number of H?"},{"v":1.0},{"v":2.0},{"v":3.0},{"v":4.0}]},
          {"c":[{"v":"Science"},null,null,{"v":"No wrong answers"},{"v":"x"},null,null,null]},
          {"c":[null,null,null,{"v":"Orphan question"},{"v":"a"},{"v":"b"}]}
        ]}});
    """.trimIndent()

    @Test fun parsesRowsAndCategories() {
        val data = SheetParser.parse(sample)
        assertEquals(4, data.totalRows)
        assertEquals(listOf("Mythology", "Science"), data.categories.keys.toList())
        assertEquals(listOf("Greek", "Norse"), data.categories["Mythology"])
        assertEquals(emptyList<String>(), data.categories["Science"]) // "General" is excluded
        // Row 4 dropped (no incorrect answers); orphan row kept with "General" category.
        assertEquals(4, data.questions.size)
        val thor = data.questions[1]
        assertEquals("Thor's hammer?", thor.question)
        assertEquals(listOf("Gungnir"), thor.incorrectAnswers)
        assertEquals("hard", thor.difficulty)
        val h = data.questions[2]
        assertEquals("1", h.correctAnswer)
        assertEquals(listOf("2", "3", "4"), h.incorrectAnswers)
        assertEquals("General", data.questions[3].category)
        assertEquals("medium", data.questions[3].difficulty)
    }

    @Test fun reportsSheetErrors() {
        val err = """google.visualization.Query.setResponse({"status":"error","errors":[{"message":"Access denied"}]});"""
        val e = assertThrows(SheetException::class.java) { SheetParser.parse(err) }
        assertEquals("Access denied", e.message)
        assertThrows(SheetException::class.java) { SheetParser.parse("<html>sign in</html>") }
    }

    @Test fun filtersLikeWebApp() {
        val pool = SheetParser.parse(sample).questions
        assertEquals(2, QuestionPicker.filter(pool, Filters(category = "Mythology")).size)
        assertEquals(1, QuestionPicker.filter(pool, Filters(category = "Mythology", subcategory = "norse")).size)
        assertEquals(1, QuestionPicker.filter(pool, Filters(difficulty = "easy")).size)
        assertEquals(0, QuestionPicker.filter(pool, Filters(category = "Mythology", difficulty = "medium")).size)
    }

    @Test fun pickLimitsAndThrowsWhenEmpty() {
        val pool = SheetParser.parse(sample).questions
        val picked = QuestionPicker.pick(pool, Filters(amount = 2), Random(1))
        assertEquals(2, picked.size)
        assertTrue(picked.all { it in pool })
        assertThrows(SheetException::class.java) {
            QuestionPicker.pick(pool, Filters(category = "History"))
        }
    }
}
