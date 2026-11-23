package app.blueprint.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import app.blueprint.ui.components.Icon
import app.blueprint.ui.theme.Theme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tanh

/** Generic interactive Liquid Glass capsule / circle. */
@Composable
fun GlassButton(
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    height: Dp = 44.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val hl = remember(scope) { InteractiveHighlight(scope) }
    val c = Theme.colors
    val haptics = Theme.haptics
    Row(
        modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(3f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
                highlight = { Highlight.Default.copy(alpha = 0.9f) },
                shadow = { Shadow(radius = 12.dp, color = Color.Black.copy(alpha = if (c.isDark) 0.3f else 0.08f)) },
                layerBlock = {
                    val w = size.width
                    val h = size.height
                    val progress = hl.pressProgress
                    val scale = lerp(1f, 1f + 8f.dp.toPx() / h, progress)
                    val maxOffset = size.minDimension
                    val o = hl.offset
                    translationX = maxOffset * tanh(0.05f * o.x / maxOffset)
                    translationY = maxOffset * tanh(0.05f * o.y / maxOffset)
                    val maxDrag = 4f.dp.toPx() / h
                    val ang = atan2(o.y, o.x)
                    scaleX = scale + maxDrag * abs(cos(ang) * o.x / size.maxDimension) * (w / h).coerceAtMost(1f)
                    scaleY = scale + maxDrag * abs(sin(ang) * o.y / size.maxDimension) * (h / w).coerceAtMost(1f)
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.8f))
                    } else drawRect(c.glassTint)
                }
            )
            .then(hl.modifier)
            .then(hl.gestureModifier)
            .pointerInput(onClick) { detectTapGestures { haptics.light(); onClick() } }
            .semantics { role = Role.Button }
            .height(height)
            .padding(horizontal = if (height >= 44.dp) 0.dp else 0.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun GlassIconButton(backdrop: Backdrop, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Color.Unspecified, iconTint: Color? = null, mirror: Boolean = false) {
    GlassButton(backdrop, onClick, modifier.width(44.dp), tint = tint) {
        Icon(icon, null, Modifier.size(24.dp), tint = iconTint ?: (if (tint.isSpecified) Color.White else Theme.colors.label), mirror = mirror)
    }
}

@Composable
fun GlassTextButton(backdrop: Backdrop, text: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Color.Unspecified, bold: Boolean = true) {
    GlassButton(backdrop, onClick, modifier, tint = tint) {
        BasicText(
            text,
            Modifier.padding(horizontal = 16.dp),
            style = Theme.type.headline.copy(
                color = if (tint.isSpecified) Color.White else Theme.colors.label,
                fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

/** Static glass panel (no interaction) */
fun Modifier.glassPanel(backdrop: Backdrop, radius: Dp, surface: Color, blurDp: Float = 16f, lensDp: Float = 18f, shadowAlpha: Float = 0.25f): Modifier =
    this.drawBackdrop(
        backdrop = backdrop,
        shape = { RoundedRectangle(radius) },
        effects = {
            vibrancy()
            blur(blurDp.dp.toPx())
            if (lensDp > 0f) lens(lensDp.dp.toPx(), lensDp.dp.toPx() * 1.6f)
        },
        highlight = { Highlight.Default.copy(alpha = 0.7f) },
        shadow = { Shadow(radius = 24.dp, color = Color.Black.copy(alpha = shadowAlpha)) },
        onDrawSurface = { drawRect(surface) }
    )

/** iOS 26 style segmented control with a springy sliding thumb you can drag. */
@Composable
fun SegmentedControl(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Theme.colors
    val t = Theme.type
    val haptics = Theme.haptics
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val pos = remember { Animatable(selected.toFloat()) }
    val press = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(selected) { pos.animateTo(selected.toFloat(), spring(0.78f, 420f)) }
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(Capsule())
            .background(c.fill.copy(alpha = if (c.isDark) 0.45f else 0.25f))
            .padding(2.dp)
    ) {
        val n = options.size
        val density = LocalDensity.current
        val segW = with(density) { constraints.maxWidth.toFloat() / n }
        var dragging by remember { mutableStateOf(false) }
        Box(
            Modifier
                .graphicsLayer {
                    val x = pos.value * segW
                    translationX = if (isRtl) -x else x
                    val s = lerp(1f, 1.06f, press.value)
                    scaleX = s; scaleY = s
                }
                .width(with(density) { segW.toDp() })
                .fillMaxHeight()
                .shadow(if (c.isDark) 0.dp else 3.dp, Capsule(), ambientColor = Color.Black.copy(0.15f), spotColor = Color.Black.copy(0.15f))
                .clip(Capsule())
                .background(if (c.isDark) Color(0xFF636366) else Color.White)
        )
        Row(
            Modifier
                .fillMaxSize()
                .pointerInput(n, selected) {
                    detectTapGestures(
                        onPress = {
                            scope.launch { press.animateTo(1f, spring(1f, 600f)) }
                            tryAwaitRelease()
                            scope.launch { press.animateTo(0f, spring(0.6f, 400f)) }
                        },
                        onTap = { o ->
                            val raw = (o.x / segW).toInt().coerceIn(0, n - 1)
                            val i = if (isRtl) n - 1 - raw else raw
                            if (i != selected) { haptics.tick(); onSelect(i) }
                        }
                    )
                }
                .pointerInput(n, selected) {
                    var p = selected.toFloat()
                    detectHorizontalDragGestures(
                        onDragStart = { dragging = true; p = pos.value; scope.launch { press.animateTo(1f, spring(1f, 600f)) } },
                        onDragEnd = {
                            dragging = false
                            val i = p.roundToInt().coerceIn(0, n - 1)
                            scope.launch { press.animateTo(0f, spring(0.6f, 400f)) }
                            scope.launch { pos.animateTo(i.toFloat(), spring(0.78f, 420f)) }
                            if (i != selected) { haptics.tick(); onSelect(i) }
                        },
                        onDragCancel = { dragging = false; scope.launch { press.animateTo(0f) ; pos.animateTo(selected.toFloat()) } },
                    ) { _, dx ->
                        p = (p + (if (isRtl) -dx else dx) / segW).coerceIn(0f, (n - 1).toFloat())
                        scope.launch { pos.snapTo(p) }
                    }
                },
        ) {
            options.forEachIndexed { i, label ->
                val active = (pos.value - i).let { 1f - abs(it).coerceAtMost(1f) }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    BasicText(
                        label, maxLines = 1,
                        style = t.subhead.copy(
                            color = c.label,
                            fontWeight = if (active > 0.5f) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
