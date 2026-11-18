package com.quizpulse.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/** Position/velocity are offsets from the burst origin, in dp. */
private class Particle(val r: Float, val color: Color, var vx: Float, var vy: Float) {
    var dx = 0f
    var dy = 0f
}

private val COLORS = listOf(Color(0xFF6366F1), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFFF59E0B), Color(0xFFEC4899))
private const val FRAMES = 110

/** 80-particle burst from 40% screen height, same physics as the web canvas version. */
@Composable
fun ConfettiBurst(trigger: Any, modifier: Modifier = Modifier) {
    val particles = remember(trigger) {
        List(80) {
            Particle(
                r = Random.nextFloat() * 5f + 3f,
                color = COLORS.random(),
                vx = (Random.nextFloat() - 0.5f) * 12f,
                vy = (Random.nextFloat() - 0.5f) * 12f - 3f,
            )
        }
    }
    var frame by remember(trigger) { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        var last = 0L
        var step = 0f
        while (frame < FRAMES) {
            withFrameNanos { now ->
                if (last == 0L) last = now
                // Advance in 60fps steps so high-refresh displays don't speed it up.
                step += (now - last) / 16_666_667f
                last = now
                var advanced = 0
                while (step >= 1f && frame + advanced < FRAMES) {
                    particles.forEach { p ->
                        p.dx += p.vx; p.dy += p.vy
                        p.vy += 0.35f
                        p.vx *= 0.96f; p.vy *= 0.96f
                    }
                    advanced++
                    step -= 1f
                }
                frame += advanced
            }
        }
    }

    Canvas(modifier.fillMaxSize()) {
        if (frame >= FRAMES) return@Canvas
        val origin = Offset(size.width / 2, size.height * 0.4f)
        particles.forEach { p ->
            drawCircle(p.color, p.r * density * 0.75f, Offset(origin.x + p.dx * density, origin.y + p.dy * density))
        }
    }
}
