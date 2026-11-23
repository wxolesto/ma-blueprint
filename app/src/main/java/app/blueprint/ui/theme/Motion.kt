package app.blueprint.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** iOS-like springs (SwiftUI .smooth / .snappy / .bouncy) */
object Motion {
    val iosEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    fun <T> smooth(visibility: T? = null) = spring<T>(dampingRatio = 1f, stiffness = 260f, visibilityThreshold = visibility)
    fun <T> snappy(visibility: T? = null) = spring<T>(dampingRatio = 0.86f, stiffness = 520f, visibilityThreshold = visibility)
    fun <T> bouncy(visibility: T? = null) = spring<T>(dampingRatio = 0.62f, stiffness = 420f, visibilityThreshold = visibility)
    fun <T> gentle(visibility: T? = null) = spring<T>(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow, visibilityThreshold = visibility)
    fun <T> push() = tween<T>(480, easing = iosEase)
}
