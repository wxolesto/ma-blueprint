package app.blueprint.ui.theme

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.blueprint.R
import app.blueprint.i18n.LocalStrings
import app.blueprint.i18n.Strings

val SFProDisplay = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.Normal),
    Font(R.font.sf_pro_display_medium, FontWeight.Medium),
    Font(R.font.sf_pro_display_bold, FontWeight.SemiBold),
    Font(R.font.sf_pro_display_bold, FontWeight.Bold),
)
val SFProText = FontFamily(
    Font(R.font.sf_pro_text_regular, FontWeight.Normal),
    Font(R.font.sf_pro_text_medium, FontWeight.Medium),
    Font(R.font.sf_pro_text_bold, FontWeight.SemiBold),
    Font(R.font.sf_pro_text_bold, FontWeight.Bold),
)
val SFArabic = FontFamily(
    Font(R.font.sf_arabic_regular, FontWeight.Normal),
    Font(R.font.sf_arabic_medium, FontWeight.Medium),
    Font(R.font.sf_arabic_bold, FontWeight.SemiBold),
    Font(R.font.sf_arabic_bold, FontWeight.Bold),
)

@Immutable
data class Palette(
    val isDark: Boolean,
    val bg: Color,
    val bgTop: Color,
    val card: Color,
    val card2: Color,
    val fill: Color,
    val label: Color,
    val label2: Color,
    val label3: Color,
    val separator: Color,
    val blue: Color,
    val green: Color,
    val red: Color,
    val orange: Color,
    val yellow: Color,
    val glassTint: Color,
)

val DarkPalette = Palette(
    isDark = true,
    bg = Color(0xFF000000),
    bgTop = Color(0xFF0B1426),
    card = Color(0xFF1C1C1E),
    card2 = Color(0xFF2C2C2E),
    fill = Color(0x5C787880),
    label = Color.White,
    label2 = Color(0x99EBEBF5),
    label3 = Color(0x4DEBEBF5),
    separator = Color(0x5454585C),
    blue = Color(0xFF0A84FF),
    green = Color(0xFF30D158),
    red = Color(0xFFFF453A),
    orange = Color(0xFFFF9F0A),
    yellow = Color(0xFFFFD60A),
    glassTint = Color(0xFF121212).copy(alpha = 0.42f),
)

val LightPalette = Palette(
    isDark = false,
    bg = Color(0xFFF2F2F7),
    bgTop = Color(0xFFE3EAFB),
    card = Color(0xFFFFFFFF),
    card2 = Color(0xFFF2F2F7),
    fill = Color(0x33787880),
    label = Color.Black,
    label2 = Color(0x993C3C43),
    label3 = Color(0x4D3C3C43),
    separator = Color(0x4A3C3C43),
    blue = Color(0xFF007AFF),
    green = Color(0xFF34C759),
    red = Color(0xFFFF3B30),
    orange = Color(0xFFFF9500),
    yellow = Color(0xFFFFCC00),
    glassTint = Color(0xFFFAFAFA).copy(alpha = 0.45f),
)

/** 12 habit colors (iOS system palette) */
val HabitColorsDark = listOf(
    0xFF30D158, 0xFF0A84FF, 0xFFFF9F0A, 0xFFBF5AF2, 0xFFFF375F, 0xFF64D2FF,
    0xFFFFD60A, 0xFFFF453A, 0xFF5E5CE6, 0xFF66D4CF, 0xFFAC8E68, 0xFFFF6482,
).map { Color(it) }
val HabitColorsLight = listOf(
    0xFF34C759, 0xFF007AFF, 0xFFFF9500, 0xFFAF52DE, 0xFFFF2D55, 0xFF32ADE6,
    0xFFFFCC00, 0xFFFF3B30, 0xFF5856D6, 0xFF00C7BE, 0xFFA2845E, 0xFFFF6482,
).map { Color(it) }

fun Palette.habit(i: Int): Color = (if (isDark) HabitColorsDark else HabitColorsLight)[i.mod(12)]

@Immutable
data class Type(
    val largeTitle: TextStyle,
    val title1: TextStyle,
    val title2: TextStyle,
    val title3: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val callout: TextStyle,
    val subhead: TextStyle,
    val footnote: TextStyle,
    val caption: TextStyle,
    val caption2: TextStyle,
    val display: FontFamily,
)

private fun style(f: FontFamily, size: Int, w: FontWeight, line: Int, track: TextUnit) = TextStyle(
    fontFamily = f, fontSize = size.sp, fontWeight = w, lineHeight = line.sp, letterSpacing = track,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

fun typeFor(fa: Boolean): Type {
    val d = if (fa) SFArabic else SFProDisplay
    val t = if (fa) SFArabic else SFProText
    val k = if (fa) 0f else 1f
    return Type(
        largeTitle = style(d, 34, FontWeight.Bold, 41, (0.011f * k).em),
        title1 = style(d, 28, FontWeight.Bold, 34, (0.012f * k).em),
        title2 = style(d, 22, FontWeight.Bold, 28, (-0.012f * k).em),
        title3 = style(d, 20, FontWeight.SemiBold, 25, (-0.023f * k).em),
        headline = style(t, 17, FontWeight.SemiBold, 22, (-0.025f * k).em),
        body = style(t, 17, FontWeight.Normal, 22, (-0.025f * k).em),
        callout = style(t, 16, FontWeight.Normal, 21, (-0.02f * k).em),
        subhead = style(t, 15, FontWeight.Normal, 20, (-0.015f * k).em),
        footnote = style(t, 13, FontWeight.Normal, 18, (-0.006f * k).em),
        caption = style(t, 12, FontWeight.Normal, 16, 0.em),
        caption2 = style(t, 11, FontWeight.Normal, 13, (0.006f * k).em),
        display = d,
    )
}

class Haptics(private val view: View, private val enabled: Boolean) {
    private fun perform(c: Int) { if (enabled) view.performHapticFeedback(c) }
    fun tap() = perform(HapticFeedbackConstants.CLOCK_TICK)
    fun light() = perform(HapticFeedbackConstants.KEYBOARD_TAP)
    fun confirm() = perform(if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
    fun reject() = perform(if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)
    fun tick() = perform(if (android.os.Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_FREQUENT_TICK else HapticFeedbackConstants.CLOCK_TICK)
    fun heavy() = perform(HapticFeedbackConstants.LONG_PRESS)
}

val LocalPalette = staticCompositionLocalOf { DarkPalette }
val LocalType = staticCompositionLocalOf { typeFor(false) }
val LocalIsDark = compositionLocalOf { true }
val LocalHaptics = staticCompositionLocalOf<Haptics> { error("no haptics") }

@Composable
fun BlueprintTheme(dark: Boolean, strings: Strings, haptics: Boolean, content: @Composable () -> Unit) {
    val view = LocalView.current
    val type = remember(strings.isFa) { typeFor(strings.isFa) }
    val h = remember(view, haptics) { Haptics(view, haptics) }
    CompositionLocalProvider(
        LocalPalette provides if (dark) DarkPalette else LightPalette,
        LocalIsDark provides dark,
        LocalType provides type,
        LocalStrings provides strings,
        LocalHaptics provides h,
        content = content,
    )
}

object Theme {
    val colors: Palette @Composable get() = LocalPalette.current
    val type: Type @Composable get() = LocalType.current
    val strings: Strings @Composable get() = LocalStrings.current
    val haptics: Haptics @Composable get() = LocalHaptics.current
}
