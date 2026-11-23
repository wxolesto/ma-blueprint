package app.blueprint.ui.nav

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.blueprint.data.Habit
import app.blueprint.ui.theme.Motion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface Route {
    data object Tabs : Route
    data object Onboarding : Route
    data class EditHabit(
        val initial: Habit?,
        val isNew: Boolean,
        val onSave: (Habit) -> Unit,
        val onDelete: (() -> Unit)? = null,
    ) : Route
    data object History : Route
    data class HabitDetail(val habitId: String) : Route
    data object NewBlueprint : Route
}

@Stable
class Navigator(root: Route) {
    class Entry(val route: Route, val key: Int, initial: Float = 1f) {
        val offset = Animatable(initial)
    }
    private var counter = 0
    val stack = mutableStateListOf(Entry(root, counter++, 0f))
    lateinit var scope: CoroutineScope

    fun push(route: Route) {
        val e = Entry(route, counter++)
        stack.add(e)
        scope.launch { e.offset.animateTo(0f, tween(500, easing = Motion.iosEase)) }
    }

    fun pop() {
        if (stack.size <= 1) return
        val e = stack.last()
        scope.launch {
            e.offset.animateTo(1f, tween(420, easing = Motion.iosEase))
            stack.remove(e)
        }
    }

    fun replaceRoot(route: Route) {
        val e = Entry(route, counter++, 0f)
        stack.clear()
        stack.add(e)
    }
}

val LocalNav = compositionLocalOf<Navigator> { error("no nav") }

@Composable
fun NavHost(nav: Navigator, content: @Composable (Route) -> Unit) {
    nav.scope = rememberCoroutineScope()
    val holder = rememberSaveableStateHolder()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val dir = if (rtl) -1f else 1f
    val scope = rememberCoroutineScope()

    PredictiveBackHandler(enabled = nav.stack.size > 1) { progress ->
        val e = nav.stack.last()
        try {
            progress.collect { ev -> e.offset.snapTo(ev.progress * 0.9f) }
            e.offset.animateTo(1f, spring(1f, 500f))
            nav.stack.remove(e)
        } catch (ce: CancellationException) {
            scope.launch { e.offset.animateTo(0f, spring(1f, 500f)) }
            throw ce
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val n = nav.stack.size
        nav.stack.forEachIndexed { i, e ->
            if (i < n - 2) {
                return@forEachIndexed
            }
            key(e.key) {
                val above = nav.stack.getOrNull(i + 1)
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val own = e.offset.value
                            val cover = 1f - (above?.offset?.value ?: 1f)
                            translationX = dir * (own * w - cover * 0.28f * w)
                        }
                        .drawBehind {
                            // soft shadow on the leading edge of pushed screens
                            if (i > 0) {
                                val sw = 14.dp.toPx()
                                val x = if (rtl) size.width else -sw
                                drawRect(
                                    Brush.horizontalGradient(
                                        if (rtl) listOf(Color.Black.copy(0.18f), Color.Transparent) else listOf(Color.Transparent, Color.Black.copy(0.18f)),
                                        startX = x, endX = x + sw
                                    ),
                                    topLeft = Offset(x, 0f), size = Size(sw, size.height)
                                )
                            }
                        }
                ) {
                    holder.SaveableStateProvider(e.key) { content(e.route) }
                    if (above != null) {
                        Box(Modifier.fillMaxSize().graphicsLayer { alpha = 1f - above.offset.value }.background(Color.Black.copy(alpha = 0.22f)))
                    }
                    if (i == n - 1 && i > 0) {
                        // interactive edge swipe (useful with 3-button navigation)
                        Box(
                            Modifier.width(18.dp).fillMaxHeight().align(if (rtl) androidx.compose.ui.Alignment.CenterEnd else androidx.compose.ui.Alignment.CenterStart)
                                .pointerInput(e.key) {
                                    var v = 0f
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            scope.launch {
                                                if (e.offset.value > 0.35f || v > 0f) {
                                                    e.offset.animateTo(1f, spring(1f, 500f)); nav.stack.remove(e)
                                                } else e.offset.animateTo(0f, spring(1f, 500f))
                                            }
                                        },
                                        onDragCancel = { scope.launch { e.offset.animateTo(0f, spring(1f, 500f)) } },
                                    ) { _, dx ->
                                        v = dx * dir
                                        scope.launch { e.offset.snapTo((e.offset.value + dx * dir / w).coerceIn(0f, 1f)) }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
