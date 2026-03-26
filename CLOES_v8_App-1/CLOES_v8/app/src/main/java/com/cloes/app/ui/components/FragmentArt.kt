package com.cloes.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.cloes.app.ui.theme.PALETTES
import kotlin.math.*

/**
 * Procedural "Fragment Light" art — replicates the HTML canvas drawFragment()
 * logic using Jetpack Compose Canvas.
 */
@Composable
fun FragmentArt(
    palette: List<Color>,
    seed: Float,
    modifier: Modifier = Modifier,
    animating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "frag")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "fragAnim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawFragment(palette, seed, if (animating) animOffset else 0f)
    }
}

private fun DrawScope.drawFragment(palette: List<Color>, seed: Float, t: Float) {
    val w = size.width
    val h = size.height
    val colors = if (palette.size >= 3) palette else
        listOf(Color(0xFFFF3385), Color(0xFF8B5CF6), Color(0xFF33A1FF))

    // Background gradient
    val bgShader = LinearGradientShader(
        from = Offset(0f, 0f), to = Offset(w, h),
        colors = listOf(colors[0].copy(alpha = 0.9f), colors.last().copy(alpha = 0.85f))
    )
    drawRect(brush = ShaderBrush(bgShader), size = size)

    // Animated orbs
    val numOrbs = minOf(colors.size + 2, 7)
    repeat(numOrbs) { i ->
        val r = Random2(seed + i * 0.17f)
        val angle = r.next() * PI.toFloat() * 2f + t * PI.toFloat() * 0.25f * (if (i % 2 == 0) 1f else -1f)
        val dist = 0.15f + r.next() * 0.35f
        val cx = w * (0.5f + cos(angle) * dist)
        val cy = h * (0.5f + sin(angle) * dist)
        val radius = (w * (0.18f + r.next() * 0.25f))
        val col = colors[i % colors.size].copy(alpha = 0.45f + r.next() * 0.3f)

        val radialBrush = RadialGradientShader(
            center = Offset(cx, cy),
            radius = radius,
            colors = listOf(col, Color.Transparent),
            colorStops = listOf(0f, 1f)
        )
        drawCircle(brush = ShaderBrush(radialBrush), radius = radius, center = Offset(cx, cy))
    }

    // Geometric lines / shards
    val numLines = 4 + (seed * 6).toInt()
    val paint = Paint().apply {
        style = PaintingStyle.Stroke
        strokeWidth = 0.8f
    }
    repeat(numLines) { i ->
        val r = Random2(seed + i * 0.31f + 0.5f)
        val x1 = w * r.next(); val y1 = h * r.next()
        val x2 = w * r.next(); val y2 = h * r.next()
        val col = colors[i % colors.size].copy(alpha = 0.2f + r.next() * 0.15f)
        drawLine(col, Offset(x1, y1), Offset(x2, y2), strokeWidth = 0.8f)
    }

    // Sparkle points
    val numSparkles = 6 + (seed * 10).toInt()
    repeat(numSparkles) { i ->
        val r = Random2(seed + i * 0.53f + 0.8f)
        val x = w * r.next(); val y = h * r.next()
        val sz = 1.2f + r.next() * 2.5f
        val col = colors[i % colors.size].copy(alpha = 0.5f + r.next() * 0.5f)
        drawCircle(col, sz, Offset(x, y))
    }
}

/** Deterministic pseudo-random from a float seed */
private class Random2(seed: Float) {
    private var s = seed * 9301f + 49297f
    fun next(): Float {
        s = (s * 9301f + 49297f) % 233280f
        return s / 233280f
    }
}

@Composable
fun FragmentAvatar(
    paletteIndex: Int,
    seed: Float,
    modifier: Modifier = Modifier,
    animating: Boolean = false
) {
    FragmentArt(
        palette = PALETTES.getOrElse(paletteIndex) { PALETTES[0] },
        seed = seed,
        modifier = modifier,
        animating = animating
    )
}

/** Generate a deterministic seed from a contact id */
fun fragSeed(id: Long): Float = ((id * 1234567L + 89) % 10000L) / 10000f
