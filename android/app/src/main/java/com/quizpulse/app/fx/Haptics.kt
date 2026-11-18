package com.quizpulse.app.fx

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class Haptic(val pattern: LongArray) {
    // Web vibrate() patterns, prefixed with a 0ms initial delay for Android waveforms.
    Tap(longArrayOf(0, 25)),
    Correct(longArrayOf(0, 30, 40, 30)),
    Wrong(longArrayOf(0, 70, 40, 70)),
    Finish(longArrayOf(0, 50, 50, 50, 50, 100)),
}

class Haptics(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun trigger(type: Haptic) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            v.vibrate(VibrationEffect.createWaveform(type.pattern, -1))
        } catch (_: Exception) {
        }
    }
}
