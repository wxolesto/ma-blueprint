package app.blueprint.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/** iOS rubber-band overscroll. */
class RubberBandOverscroll(private val scope: CoroutineScope) : OverscrollEffect {
    private val offset = Animatable(0f)
    private var orientation: Orientation? = null
    private val limit = 900f

    private fun Offset.axis(): Float = if (orientation == Orientation.Horizontal) x else y
    private fun Float.toOffset(): Offset = if (orientation == Orientation.Horizontal) Offset(this, 0f) else Offset(0f, this)

    override fun applyToScroll(delta: Offset, source: NestedScrollSource, performScroll: (Offset) -> Offset): Offset {
        if (orientation == null) orientation = if (abs(delta.x) > abs(delta.y)) Orientation.Horizontal else Orientation.Vertical
        val d = delta.axis()
        val cur = offset.value
        // first, relax existing overscroll when scrolling back
        var consumedByRelax = 0f
        if (cur != 0f && sign(d) != sign(cur) && source == NestedScrollSource.UserInput) {
            val newV = if (abs(d) >= abs(cur)) 0f else cur + d
            consumedByRelax = newV - cur
            scope.launch { offset.snapTo(newV) }
        }
        val remaining = (d - consumedByRelax)
        val consumed = performScroll(remaining.toOffset()).axis()
        val leftover = remaining - consumed
        if (leftover != 0f && source == NestedScrollSource.UserInput) {
            val resistance = (1f - abs(offset.value) / limit).coerceIn(0.05f, 1f) * 0.55f
            val nv = offset.value + leftover * resistance
            scope.launch { offset.snapTo(nv) }
        }
        return delta
    }

    override suspend fun applyToFling(velocity: Velocity, performFling: suspend (Velocity) -> Velocity) {
        val left = performFling(velocity)
        val v = if (orientation == Orientation.Horizontal) left.x else left.y
        if (offset.value != 0f || abs(v) > 0f) {
            offset.animateTo(0f, spring(1f, 170f), initialVelocity = if (offset.value == 0f) v * 0.18f else v * 0.1f)
        }
    }

    override val isInProgress: Boolean get() = offset.value != 0f

    override val node: DelegatableNode = object : Modifier.Node(), LayoutModifierNode {
        override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
            val p = measurable.measure(constraints)
            return layout(p.width, p.height) {
                p.placeWithLayer(0, 0) {
                    if (orientation == Orientation.Horizontal) translationX = offset.value else translationY = offset.value
                }
            }
        }
    }
}

class RubberBandFactory : OverscrollFactory {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    override fun createOverscrollEffect(): OverscrollEffect = RubberBandOverscroll(scope)
    override fun equals(other: Any?) = other is RubberBandFactory
    override fun hashCode() = 1
}
