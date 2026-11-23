package app.blueprint.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/** iOS-style press feedback: shrink + dim with springs. */
fun Modifier.pressable(
    scale: Float = 0.96f,
    dim: Float = 0.75f,
    enabled: Boolean = true,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val s = remember { Animatable(1f) }
    val a = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer { scaleX = s.value; scaleY = s.value; alpha = a.value }
        .pointerInput(enabled, onClick, onLongPress) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    scope.launch { s.animateTo(scale, spring(1f, 900f)) }
                    scope.launch { a.animateTo(dim, spring(1f, 900f)) }
                    tryAwaitRelease()
                    scope.launch { s.animateTo(1f, spring(0.55f, 380f)) }
                    scope.launch { a.animateTo(1f, spring(1f, 300f)) }
                },
                onLongPress = onLongPress?.let { lp -> { lp() } },
                onTap = { onClick() },
            )
        }
}
