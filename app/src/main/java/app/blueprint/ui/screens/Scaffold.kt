package app.blueprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.blueprint.ui.components.T
import app.blueprint.ui.glass.GlassIconButton
import app.blueprint.ui.theme.Theme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/** Background used by every screen: deep black/soft gray with a faint tinted glow at the top. */
@Composable
fun ScreenBackground(modifier: Modifier = Modifier) {
    val c = Theme.colors
    Box(modifier.fillMaxSize().background(c.bg).drawBehind {
        drawRect(Brush.verticalGradient(listOf(c.bgTop, c.bg), startY = 0f, endY = size.height * 0.45f))
        drawCircle(Brush.radialGradient(listOf(c.blue.copy(alpha = if (c.isDark) 0.10f else 0.07f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, 0f), radius = size.width * 0.9f), radius = size.width * 0.9f, center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, 0f))
    })
}

class ScreenScope(val backdrop: Backdrop, val list: LazyListState)

/**
 * iOS large-title screen. The scrollable content is recorded into its own backdrop so that the
 * floating glass toolbar buttons refract it.
 */
@Composable
fun LargeTitleScreen(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    bottomPadding: Dp = 120.dp,
    largeTitle: Boolean = true,
    actions: @Composable RowScope.(Backdrop) -> Unit = {},
    overlay: @Composable (Backdrop) -> Unit = {},
    content: LazyListScope.(ScreenScope) -> Unit,
) {
    val c = Theme.colors
    val t = Theme.type
    val backdrop = rememberLayerBackdrop()
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val density = LocalDensity.current
    val collapse by remember {
        derivedStateOf {
            if (!largeTitle) 1f
            else if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / with(density) { 40.dp.toPx() }).coerceIn(0f, 1f)
        }
    }
    val scope = remember(backdrop, listState) { ScreenScope(backdrop, listState) }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            ScreenBackground()
            LazyColumn(
                Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(top = top + 56.dp, bottom = bottom + bottomPadding),
            ) {
                if (largeTitle) item(key = "__title") {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 6.dp).graphicsLayer { alpha = 1f - collapse }) {
                        if (subtitle != null) T(subtitle.uppercase(), t.footnote, color = c.label2)
                        T(title, t.largeTitle, color = c.label)
                    }
                }
                content(scope)
            }
        }
        // scroll-edge effect + toolbar
        Box(
            Modifier.fillMaxWidth().height(top + 70.dp).graphicsLayer { alpha = collapse }
                .background(Brush.verticalGradient(listOf(c.bg.copy(alpha = 0.92f), c.bg.copy(alpha = 0.7f), c.bg.copy(alpha = 0f))))
        )
        Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp).height(56.dp)) {
            if (onBack != null) GlassIconButton(backdrop, Icons.Rounded.ChevronLeft, onBack, Modifier.align(Alignment.CenterStart), iconTint = c.label, mirror = true)
            T(title, t.headline.copy(textAlign = TextAlign.Center), Modifier.align(Alignment.Center).padding(horizontal = 96.dp).graphicsLayer { alpha = collapse; translationY = (1f - collapse) * 12f }, maxLines = 1)
            Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { actions(backdrop) }
        }
        overlay(backdrop)
    }
}
