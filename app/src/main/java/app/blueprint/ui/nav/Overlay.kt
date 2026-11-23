package app.blueprint.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.blueprint.ui.components.T
import app.blueprint.ui.components.pressable
import app.blueprint.ui.glass.glassPanel
import app.blueprint.ui.theme.Theme
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlertAction(val text: String, val destructive: Boolean = false, val bold: Boolean = false, val onClick: () -> Unit = {})

@Stable
class Overlay {
    var sheet by mutableStateOf<(@Composable ColumnScope.(dismiss: () -> Unit) -> Unit)?>(null)
        private set
    var sheetKey by mutableStateOf(0)
    var alertTitle by mutableStateOf<String?>(null)
    var alertMessage by mutableStateOf<String?>(null)
    var alertActions by mutableStateOf<List<AlertAction>>(emptyList())
    var toast by mutableStateOf<String?>(null)
    var toastKey by mutableStateOf(0)
    internal var closeSheet: (() -> Unit)? = null

    fun showSheet(content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit) { sheet = content; sheetKey++ }
    internal fun clearSheet() { sheet = null }
    fun dismissSheet() { closeSheet?.invoke() }
    fun alert(title: String, message: String?, actions: List<AlertAction>) { alertTitle = title; alertMessage = message; alertActions = actions }
    fun showToast(text: String) { toast = text; toastKey++ }
}

val LocalOverlay = compositionLocalOf<Overlay> { error("no overlay") }

@Composable
fun OverlayHost(overlay: Overlay, backdrop: Backdrop) {
    SheetLayer(overlay, backdrop)
    AlertLayer(overlay, backdrop)
    ToastLayer(overlay, backdrop)
}

@Composable
private fun SheetLayer(overlay: Overlay, backdrop: Backdrop) {
    val content = overlay.sheet ?: return
    val c = Theme.colors
    val scope = rememberCoroutineScope()
    val progress = remember(overlay.sheetKey) { Animatable(0f) }
    val drag = remember(overlay.sheetKey) { Animatable(0f) }
    var height by remember { mutableStateOf(1000f) }
    val dismiss: () -> Unit = {
        scope.launch {
            launch { drag.animateTo(0f, spring(1f, 400f)) }
            progress.animateTo(0f, spring(1f, 420f))
            overlay.clearSheet()
        }
    }
    overlay.closeSheet = dismiss
    LaunchedEffect(overlay.sheetKey) { progress.animateTo(1f, spring(0.88f, 300f)) }
    BackHandler { dismiss() }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { alpha = progress.value.coerceIn(0f, 1f) * (1f - (drag.value / height).coerceIn(0f, 1f)) }
                .background(Color.Black.copy(alpha = 0.35f))
                .pointerInput(Unit) { detectTapGestures { dismiss() } }
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding()
                .padding(8.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .onSizeChanged { height = it.height.toFloat() + 200f }
                .graphicsLayer {
                    translationY = (1f - progress.value) * height + drag.value
                }
                .glassPanel(backdrop, 38.dp, if (c.isDark) Color(0xFF1C1C1E).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.8f), blurDp = 28f, lensDp = 14f, shadowAlpha = 0.3f)
                .pointerInput(Unit) { detectTapGestures { } }
                .draggable(
                    rememberDraggableState { d -> scope.launch { drag.snapTo((drag.value + if (drag.value + d < 0) d * 0.2f else d).coerceAtLeast(-60f)) } },
                    Orientation.Vertical,
                    onDragStopped = { v ->
                        if (drag.value > 140f || v > 1800f) dismiss() else drag.animateTo(0f, spring(0.75f, 400f), v)
                    }
                )
                .padding(bottom = 12.dp)
        ) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(36.dp).height(5.dp).clip(Capsule()).background(c.label3))
            }
            content(dismiss)
        }
    }
}

@Composable
private fun AlertLayer(overlay: Overlay, backdrop: Backdrop) {
    val title = overlay.alertTitle ?: return
    val c = Theme.colors
    val t = Theme.type
    val scope = rememberCoroutineScope()
    val p = remember(title) { Animatable(0f) }
    LaunchedEffect(title) { p.animateTo(1f, spring(0.78f, 420f)) }
    val close: (() -> Unit) -> Unit = { after ->
        scope.launch { p.animateTo(0f, tween(160)); overlay.alertTitle = null; after() }
    }
    BackHandler { close {} }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = p.value.coerceIn(0f, 1f) }.background(Color.Black.copy(0.3f)).pointerInput(Unit) { detectTapGestures { } })
        Column(
            Modifier
                .width(300.dp)
                .graphicsLayer {
                    val s = 1.18f - 0.18f * p.value
                    scaleX = s; scaleY = s; alpha = p.value.coerceIn(0f, 1f)
                }
                .glassPanel(backdrop, 32.dp, if (c.isDark) Color(0xFF2C2C2E).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.86f), blurDp = 30f, lensDp = 12f)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            T(title, t.headline, Modifier.fillMaxWidth(), color = c.label)
            overlay.alertMessage?.let {
                Spacer(Modifier.height(6.dp))
                androidx.compose.foundation.text.BasicText(it, style = t.subhead.copy(color = c.label2, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(18.dp))
            val actions = overlay.alertActions
            val vertical = actions.size > 2
            val btn: @Composable (AlertAction, Modifier) -> Unit = { a, m ->
                Box(
                    m.height(48.dp).pressable(0.96f) { close(a.onClick) }.clip(Capsule())
                        .background(if (a.destructive) c.red else if (a.bold) c.blue else c.fill.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    T(a.text, t.headline.copy(fontWeight = if (a.bold || a.destructive) FontWeight.SemiBold else FontWeight.Medium), color = if (a.destructive || a.bold) Color.White else c.label)
                }
            }
            if (vertical) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { actions.forEach { btn(it, Modifier.fillMaxWidth()) } }
            else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { actions.forEach { btn(it, Modifier.weight(1f)) } }
        }
    }
}

@Composable
private fun ToastLayer(overlay: Overlay, backdrop: Backdrop) {
    val text = overlay.toast ?: return
    val c = Theme.colors
    val p = remember(overlay.toastKey) { Animatable(0f) }
    LaunchedEffect(overlay.toastKey) {
        p.animateTo(1f, spring(0.7f, 380f)); delay(1800); p.animateTo(0f, spring(1f, 380f)); overlay.toast = null
    }
    Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 8.dp), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier.graphicsLayer { translationY = (p.value - 1f) * 120f; alpha = p.value.coerceIn(0f, 1f); val s = 0.8f + 0.2f * p.value; scaleX = s; scaleY = s }
                .glassPanel(backdrop, 24.dp, c.glassTint, blurDp = 12f, lensDp = 10f)
                .heightIn(min = 48.dp).padding(horizontal = 22.dp, vertical = 13.dp)
        ) { T(text, Theme.type.subhead.copy(fontWeight = FontWeight.SemiBold), color = c.label) }
    }
}
