package app.blueprint.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private class Piece(val x: Float, var vx: Float, var vy: Float, val color: Color, val rot: Float, val spin: Float, val w: Float, val h: Float)

/** Lightweight physics confetti burst. */
@Composable
fun Confetti(key: Int, onEnd: () -> Unit) {
    val colors = listOf(Color(0xFF30D158), Color(0xFF0A84FF), Color(0xFFFF9F0A), Color(0xFFBF5AF2), Color(0xFFFF375F), Color(0xFFFFD60A), Color(0xFF64D2FF))
    val pieces = remember(key) {
        List(110) {
            Piece(Random.nextFloat(), (Random.nextFloat() - 0.5f) * 0.9f, -(0.9f + Random.nextFloat() * 0.9f), colors.random(), Random.nextFloat() * 360f, (Random.nextFloat() - 0.5f) * 720f, 6f + Random.nextFloat() * 6f, 10f + Random.nextFloat() * 8f)
        }
    }
    var time by remember(key) { mutableLongStateOf(0L) }
    LaunchedEffect(key) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            time = now - start
            if (time > 2_600_000_000L) break
        }
        onEnd()
    }
    Canvas(Modifier.fillMaxSize()) {
        val ts = time / 1e9f
        val g = 1.6f
        pieces.forEach { p ->
            val x = (p.x + p.vx * ts) * size.width
            val y = size.height * 0.55f + (p.vy * ts + 0.5f * g * ts * ts) * size.height * 0.6f
            val alpha = (1f - ((ts - 1.8f) / 0.8f)).coerceIn(0f, 1f)
            rotate(p.rot + p.spin * ts, Offset(x, y)) {
                drawRect(p.color.copy(alpha = alpha), Offset(x - p.w / 2, y - p.h / 2), Size(p.w * density / 2.5f, p.h * density / 2.5f))
            }
        }
    }
}
