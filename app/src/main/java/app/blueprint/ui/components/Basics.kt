package app.blueprint.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.blueprint.ui.theme.Theme
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

@Composable
fun Icon(icon: ImageVector, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = Theme.colors.label, mirror: Boolean = false) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Image(icon, contentDescription, modifier.then(if (mirror && rtl) Modifier.graphicsLayer { scaleX = -1f } else Modifier), colorFilter = ColorFilter.tint(tint))
}

@Composable
fun T(text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = Theme.colors.label, maxLines: Int = Int.MAX_VALUE) {
    BasicText(text, modifier, style = style.copy(color = color), maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

val CardShape = RoundedRectangle(26.dp)

@Composable
fun Card(modifier: Modifier = Modifier, color: Color = Theme.colors.card, radius: Dp = 26.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedRectangle(radius)).background(color), content = content)
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        T(text, Theme.type.footnote.copy(fontWeight = FontWeight.Medium), color = Theme.colors.label2, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun Divider(start: Dp = 16.dp) {
    Box(Modifier.padding(start = start).fillMaxWidth().height(0.5.dp).background(Theme.colors.separator))
}

@Composable
fun IconTile(icon: ImageVector, color: Color, size: Dp = 30.dp, radius: Dp = 8.dp, iconSize: Dp = size * 0.6f, filled: Boolean = true) {
    Box(
        Modifier.size(size).clip(RoundedRectangle(radius)).background(if (filled) color else color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, Modifier.size(iconSize), tint = if (filled) Color.White else color) }
}

/** iOS grouped list row */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconColor: Color = Theme.colors.blue,
    value: String? = null,
    titleColor: Color = Theme.colors.label,
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val m = if (onClick != null) modifier.pressable(scale = 0.985f, dim = 0.6f, onClick = onClick) else modifier
    Row(m.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) { IconTile(icon, iconColor); Spacer(Modifier.width(14.dp)) }
        T(title, Theme.type.body, color = titleColor, modifier = Modifier.weight(1f), maxLines = 1)
        if (value != null) T(value, Theme.type.body, color = Theme.colors.label2, maxLines = 1)
        trailing?.invoke(this)
        if (chevron) { Spacer(Modifier.width(6.dp)); Icon(Icons.Rounded.ChevronRight, null, Modifier.size(20.dp), tint = Theme.colors.label3, mirror = true) }
    }
}

/** Animated check circle (supports multiple completions per day). */
@Composable
fun CheckCircle(count: Int, target: Int, color: Color, modifier: Modifier = Modifier, size: Dp = 30.dp) {
    val c = Theme.colors
    val frac by animateFloatAsState((count.toFloat() / target).coerceIn(0f, 1f), spring(0.8f, 300f), label = "ring")
    val full = count >= target
    val fill by animateFloatAsState(if (full) 1f else 0f, spring(0.7f, 420f), label = "fill")
    val check by animateFloatAsState(if (full) 1f else 0f, spring(1f, if (full) 220f else 900f), label = "check")
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(count) {
        if (count > 0) { bounce.snapTo(0.82f); bounce.animateTo(1f, spring(0.45f, 500f)) }
    }
    Canvas(modifier.size(size).scale(bounce.value)) {
        val sw = 2.2.dp.toPx()
        val r = this.size.minDimension / 2
        drawCircle(c.label3.copy(alpha = 0.55f * (1 - fill)), r - sw / 2, style = Stroke(sw))
        if (target > 1 && !full) {
            drawArc(color, -90f, 360f * frac, false, topLeft = Offset(sw / 2, sw / 2), size = Size(this.size.width - sw, this.size.height - sw), style = Stroke(sw, cap = StrokeCap.Round))
        }
        if (fill > 0f) drawCircle(color, r * fill)
        if (check > 0f) {
            val p = Path().apply {
                moveTo(this@Canvas.size.width * 0.28f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.44f, this@Canvas.size.height * 0.67f)
                lineTo(this@Canvas.size.width * 0.73f, this@Canvas.size.height * 0.36f)
            }
            val pm = PathMeasure().apply { setPath(p, false) }
            val seg = Path()
            pm.getSegment(0f, pm.length * check, seg, true)
            drawPath(seg, Color.White, style = Stroke(2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Progress ring for the week strip */
@Composable
fun ProgressRing(progress: Float?, color: Color, modifier: Modifier = Modifier, stroke: Dp = 3.dp, track: Color = Theme.colors.fill) {
    val p by animateFloatAsState(progress ?: 0f, spring(0.9f, 160f), label = "ring")
    Canvas(modifier) {
        val sw = stroke.toPx()
        val inset = sw / 2
        drawArc(track, 0f, 360f, false, Offset(inset, inset), Size(size.width - sw, size.height - sw), style = Stroke(sw))
        if (p > 0f) drawArc(color, -90f, 360f * p, false, Offset(inset, inset), Size(size.width - sw, size.height - sw), style = Stroke(sw, cap = StrokeCap.Round))
    }
}

@Composable
fun ProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), spring(0.85f, 140f), label = "bar")
    Box(modifier.fillMaxWidth().height(height).clip(Capsule()).background(Theme.colors.fill)) {
        Box(Modifier.fillMaxWidth(p.coerceAtLeast(0.0001f)).height(height).clip(Capsule()).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.75f), color))))
    }
}

@Composable
fun Stepper(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    val c = Theme.colors
    val h = Theme.haptics
    Row(Modifier.clip(Capsule()).background(c.fill.copy(alpha = 0.35f)).height(34.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(46.dp).height(34.dp).pressable(0.9f, enabled = value > range.first) { h.tick(); onChange(value - 1) }, contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Remove, null, Modifier.size(18.dp), tint = if (value > range.first) c.label else c.label3)
        }
        Box(Modifier.width(1.dp).height(18.dp).background(c.separator))
        Box(Modifier.width(46.dp).height(34.dp).pressable(0.9f, enabled = value < range.last) { h.tick(); onChange(value + 1) }, contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Add, null, Modifier.size(18.dp), tint = if (value < range.last) c.label else c.label3)
        }
    }
}

/** Big filled call-to-action capsule */
@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, gradient: List<Color>? = null, trailing: ImageVector? = null, onClick: () -> Unit) {
    val c = Theme.colors
    val bgColors = gradient ?: listOf(c.blue, c.blue)
    val alpha by animateFloatAsState(if (enabled) 1f else 0.4f, label = "a")
    Row(
        modifier.fillMaxWidth().height(56.dp).graphicsLayer { this.alpha = alpha }
            .pressable(0.97f, 0.85f, enabled) { onClick() }
            .clip(Capsule()).background(Brush.horizontalGradient(bgColors)),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        T(text, Theme.type.headline, color = Color.White)
        if (trailing != null) { Spacer(Modifier.width(8.dp)); Icon(trailing, null, Modifier.size(20.dp), tint = Color.White, mirror = true) }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, color: Color = Theme.colors.blue, onClick: () -> Unit) {
    val c = Theme.colors
    val bg by animateColorAsState(if (selected) color else c.card2, spring(1f, 500f), label = "chip")
    val fg by animateColorAsState(if (selected) Color.White else c.label, label = "chipfg")
    Box(modifier.pressable(0.94f) { onClick() }.clip(Capsule()).background(bg).padding(horizontal = 16.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
        T(text, Theme.type.subhead.copy(fontWeight = FontWeight.Medium), color = fg, maxLines = 1)
    }
}

@Composable
fun Dot(color: Color, size: Dp = 8.dp) { Box(Modifier.size(size).clip(CircleShape).background(color)) }

/** blue "Done" capsule used in sheets */
@Composable
fun Modifier.pressableDone(onClick: () -> Unit): Modifier =
    this.pressable(0.94f) { onClick() }.clip(Capsule()).background(Theme.colors.blue).padding(horizontal = 18.dp, vertical = 9.dp)

@Composable
fun Modifier.fieldBg(): Modifier = this.clip(RoundedRectangle(18.dp)).background(Theme.colors.fill.copy(alpha = 0.25f))
