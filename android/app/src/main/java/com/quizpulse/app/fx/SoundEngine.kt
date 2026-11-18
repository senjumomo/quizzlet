package com.quizpulse.app.fx

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

/**
 * Synthesizes the same short cues as the web app's WebAudio SoundEngine
 * and plays them with a static AudioTrack. Buffers are rendered lazily once.
 */
class SoundEngine {
    var enabled: Boolean = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val correct by lazy { renderCorrect() }
    private val wrong by lazy { renderWrong() }
    private val finish by lazy { renderFinish() }

    fun playCorrect() = play { correct }
    fun playWrong() = play { wrong }
    fun playFinish() = play { finish }

    private fun play(buffer: () -> ShortArray) {
        if (!enabled) return
        scope.launch {
            val pcm = buffer()
            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(pcm.size * 2)
                    .build()
            } catch (e: Exception) {
                return@launch
            }
            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                delay(pcm.size * 1000L / RATE + 100)
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    private companion object {
        const val RATE = 44_100

        /** WebAudio-style exponentialRampToValueAtTime from g0 to g1 over [dur]. */
        fun expRamp(g0: Double, g1: Double, t: Double, dur: Double): Double =
            g0 * exp(ln(g1 / g0) * (t / dur).coerceIn(0.0, 1.0))

        fun toPcm(samples: DoubleArray) = ShortArray(samples.size) {
            (samples[it].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        fun renderCorrect(): ShortArray {
            val dur = 0.35
            val out = DoubleArray((dur * RATE).toInt())
            var phase = 0.0
            for (i in out.indices) {
                val t = i.toDouble() / RATE
                val f = when {
                    t < 0.08 -> 523.25
                    t < 0.16 -> 659.25
                    else -> 783.99
                }
                phase += 2 * PI * f / RATE
                out[i] = sin(phase) * expRamp(0.18, 0.001, t, dur)
            }
            return toPcm(out)
        }

        fun renderWrong(): ShortArray {
            val dur = 0.25
            val out = DoubleArray((dur * RATE).toInt())
            var phase = 0.0
            for (i in out.indices) {
                val t = i.toDouble() / RATE
                val f = if (t < 0.2) 160 + (90 - 160) * (t / 0.2) else 90.0
                phase += f / RATE
                val saw = 2 * (phase - Math.floor(phase + 0.5))
                out[i] = saw * expRamp(0.12, 0.001, t, dur)
            }
            return toPcm(out)
        }

        fun renderFinish(): ShortArray {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            val noteDur = 0.45
            val total = 0.09 * (notes.size - 1) + noteDur
            val out = DoubleArray((total * RATE).toInt())
            notes.forEachIndexed { idx, f ->
                val start = (idx * 0.09 * RATE).toInt()
                val len = (noteDur * RATE).toInt()
                for (j in 0 until len) {
                    val k = start + j
                    if (k >= out.size) break
                    val t = j.toDouble() / RATE
                    // WebAudio oscillators default to sine.
                    out[k] += sin(2 * PI * f * t) * expRamp(0.14, 0.001, t, noteDur)
                }
            }
            return toPcm(out)
        }
    }
}
