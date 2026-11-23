package app.blueprint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.blueprint.ui.theme.Theme
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.cos

/** iOS-style 3D wheel picker (cylinder projection, snap, haptic ticks). */
@Composable
fun WheelPicker(
    count: Int,
    selected: Int,
    onSelect: (Int) -> Unit,
    label: (Int) -> String,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    cyclic: Boolean = true,
) {
    val itemH = 40.dp
    val visible = 5
    val c = Theme.colors
    val t = Theme.type
    val h = Theme.haptics
    val total = if (cyclic) count * 400 else count
    val base = if (cyclic) count * 200 else 0
    val state = rememberLazyListState(initialFirstVisibleItemIndex = base + selected)
    val fling = rememberSnapFlingBehavior(state)
    val density = LocalDensity.current
    val itemPx = with(density) { itemH.toPx() }
    val center by remember {
        derivedStateOf { state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > itemPx / 2) 1 else 0 }
    }
    LaunchedEffect(state) {
        snapshotFlow { center }.distinctUntilChanged().collect { h.tick() }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.collect { scrolling -> if (!scrolling) onSelect(center.mod(count)) }
    }
    Box(modifier.width(width).height(itemH * visible), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(itemH).clip(RoundedRectangle(10.dp)).background(c.fill.copy(alpha = 0.3f)))
        LazyColumn(
            Modifier.fillMaxWidth().height(itemH * visible),
            state = state,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = itemH * (visible / 2)),
        ) {
            items(total) { i ->
                Box(
                    Modifier.fillMaxWidth().height(itemH).graphicsLayer {
                        val info = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == i }
                        if (info != null) {
                            val viewportCenter = (state.layoutInfo.viewportEndOffset + state.layoutInfo.viewportStartOffset) / 2f
                            val itemCenter = info.offset + info.size / 2f + state.layoutInfo.beforeContentPadding * 0f
                            val d = ((itemCenter - viewportCenter + state.layoutInfo.beforeContentPadding) / (itemPx * 2.6f)).coerceIn(-1f, 1f)
                            rotationX = -d * 62f
                            alpha = (cos(d * 1.4f)).coerceIn(0.15f, 1f)
                            val sc = 1f - abs(d) * 0.12f
                            scaleX = sc; scaleY = sc
                            cameraDistance = 6f * density.density
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    T(label(i.mod(count)), t.title3.copy(fontWeight = FontWeight.Normal), color = c.label)
                }
            }
        }
    }
}
