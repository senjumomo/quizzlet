package com.quizpulse.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class SheetRepository(private val sheetId: String = Config.SPREADSHEET_ID) {
    private val mutex = Mutex()
    @Volatile private var cached: SheetData? = null

    suspend fun load(forceRefresh: Boolean = false): SheetData = mutex.withLock {
        cached?.takeIf { !forceRefresh }?.let { return it }
        val data = withContext(Dispatchers.IO) { SheetParser.parse(fetch()) }
        if (data.questions.isEmpty()) throw SheetException("Your Google Sheet appears to be empty.")
        cached = data
        data
    }

    private fun fetch(): String {
        val url = URL(
            "https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:json&headers=1&t=${System.currentTimeMillis()}"
        )
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 9_000
            readTimeout = 9_000
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw SheetException("Failed to reach Google Sheet (HTTP $code). Check its share permissions.")
            }
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: SocketTimeoutException) {
            throw SheetException("Connection timed out. Check that your Google Sheet has view permissions.")
        } catch (e: IOException) {
            throw SheetException("Failed to reach Google Sheet. Check your connection or share permissions.")
        } finally {
            conn.disconnect()
        }
    }
}
