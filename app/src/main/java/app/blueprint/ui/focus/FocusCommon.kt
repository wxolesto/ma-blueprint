package app.blueprint.ui.focus

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.blueprint.data.FocusSession
import app.blueprint.data.Phase
import app.blueprint.i18n.f
import app.blueprint.ui.components.T
import app.blueprint.ui.components.pressable
import app.blueprint.ui.theme.Palette
import app.blueprint.ui.theme.Theme
import com.kyant.shapes.Capsule

/** lets any screen switch the main tab (0 today, 1 tasks, 2 focus, 3 progress, 4 settings) */
val LocalTabSelect = compositionLocalOf<(Int) -> Unit> { {} }

fun Palette.phase(p: Phase): Color = when (p) {
    Phase.Focus -> if (isDark) Color(0xFFFF5E57) else Color(0xFFFF3B30)
    Phase.Short -> green
    Phase.Long -> blue
}

/** wall-clock that updates every frame while [running] */
@Composable
fun rememberTicker(running: Boolean): State<Long> {
    val st = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(running) {
        st.longValue = System.currentTimeMillis()
        while (running) withFrameMillis { st.longValue = System.currentTimeMillis() }
    }
    return st
}

/**
 * A timeline entry "09:00 – 09:25  ●  Writing report · Focus · 25m".
 * When [liveEnd] is given the entry is the running segment.
 */
@Composable
fun TimelineRow(
    session: FocusSession,
    title: String,
    color: Color,
    isLast: Boolean,
    live: Boolean = false,
    showDate: Boolean = false,
    onLongPress: (() -> Unit)? = null,
) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val pulse = if (live) {
        val tr = rememberInfiniteTransition(label = "p")
        tr.animateFloat(0.35f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pa").value
    } else 1f
    val m = if (onLongPress != null) Modifier.pressable(0.985f, 0.7f, onLongPress = onLongPress) {} else Modifier
    Row(m.fillMaxWidth().height(IntrinsicSize.Min).padding(start = 16.dp, end = 16.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(76.dp).padding(top = 12.dp)) {
            T(f.clock(session.start), t.subhead.copy(fontWeight = FontWeight.SemiBold), color = c.label, maxLines = 1)
            T(if (live) f.now else f.clock(session.end), t.footnote, color = if (live) color else c.label2, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.width(22.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            Canvas(Modifier.width(22.dp).fillMaxHeight()) {
                val x = size.width / 2
                val dotY = 20.dp.toPx()
                if (!isLast) drawLine(c.separator, Offset(x, dotY), Offset(x, size.height), 2.dp.toPx())
                drawLine(c.separator, Offset(x, 0f), Offset(x, dotY), 2.dp.toPx())
                if (session.completed || live) drawCircle(color.copy(alpha = pulse), 6.dp.toPx(), Offset(x, dotY))
                else {
                    drawCircle(c.card, 6.dp.toPx(), Offset(x, dotY))
                    drawCircle(color, 5.dp.toPx(), Offset(x, dotY), style = Stroke(2.dp.toPx()))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f).heightIn(min = 56.dp).padding(top = 10.dp, bottom = 12.dp)) {
            T(title, t.body.copy(fontWeight = FontWeight.Medium), maxLines = 2)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.clip(Capsule()).background(color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    T(f.phaseName(session.phase), t.caption.copy(fontWeight = FontWeight.SemiBold), color = color, maxLines = 1)
                }
                T(f.dur(session.end - session.start), t.footnote, color = c.label2, maxLines = 1)
                if (!live) {
                    Box(Modifier.size(3.dp).clip(CircleShape).background(c.label3))
                    T(if (session.completed) f.finished else f.interrupted, t.footnote, color = if (session.completed) c.label2 else c.orange, maxLines = 1)
                }
                if (showDate) {
                    Box(Modifier.size(3.dp).clip(CircleShape).background(c.label3))
                    T(s.dateShort(session.day), t.footnote, color = c.label2, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, sub: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    val c = Theme.colors
    val t = Theme.type
    Column(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            app.blueprint.ui.components.Icon(icon, null, Modifier.size(32.dp), tint = color)
        }
        Spacer(Modifier.height(14.dp))
        T(title, t.headline)
        Spacer(Modifier.height(4.dp))
        T(sub, t.subhead.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center), color = c.label2)
    }
}

@Composable
fun Header(text: String, trailing: (@Composable () -> Unit)? = null) = app.blueprint.ui.components.SectionHeader(text.uppercase(), trailing = trailing)
