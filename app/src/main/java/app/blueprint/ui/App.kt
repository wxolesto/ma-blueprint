package app.blueprint.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import app.blueprint.data.AppData
import app.blueprint.data.Repository
import app.blueprint.data.ThemeMode
import app.blueprint.i18n.En
import app.blueprint.i18n.Fa
import app.blueprint.ui.components.Confetti
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.RubberBandFactory
import app.blueprint.ui.components.T
import app.blueprint.ui.glass.LiquidBottomTab
import app.blueprint.ui.glass.LiquidBottomTabs
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.nav.NavHost
import app.blueprint.ui.nav.Navigator
import app.blueprint.ui.nav.Overlay
import app.blueprint.ui.nav.OverlayHost
import app.blueprint.ui.nav.Route
import app.blueprint.ui.screens.EditHabitScreen
import app.blueprint.ui.screens.HabitDetailScreen
import app.blueprint.ui.screens.HistoryScreen
import app.blueprint.ui.screens.OnboardingFlow
import app.blueprint.ui.screens.ProgressScreen
import app.blueprint.ui.screens.ScreenBackground
import app.blueprint.ui.screens.SettingsScreen
import app.blueprint.ui.screens.TodayScreen
import app.blueprint.ui.theme.BlueprintTheme
import app.blueprint.ui.theme.Theme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun rememberToday(): Long {
    var today by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                today = LocalDate.now().toEpochDay()
                val now = LocalDateTime.now()
                val next = now.toLocalDate().plusDays(1).atStartOfDay()
                delay(java.time.Duration.between(now, next).toMillis() + 500)
            }
        }
    }
    return today
}

@Composable
fun BlueprintRoot(onDarkChanged: (Boolean) -> Unit) {
    val data by Repository.data.collectAsStateWithLifecycle()
    val sysDark = isSystemInDarkTheme()
    val dark = when (data.settings.theme) { ThemeMode.System -> sysDark; ThemeMode.Light -> false; ThemeMode.Dark -> true }
    LaunchedEffect(dark) { onDarkChanged(dark) }
    val strings = if (data.settings.language == "fa") Fa else En
    val nav = remember { Navigator(if (data.active == null) Route.Onboarding else Route.Tabs) }
    val overlay = remember { Overlay() }
    val today = rememberToday()

    // if all blueprints get deleted, go back to onboarding
    LaunchedEffect(data.active == null) {
        if (data.active == null && nav.stack.firstOrNull()?.route != Route.Onboarding) nav.replaceRoot(Route.Onboarding)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (strings.isFa) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalOverscrollFactory provides remember { RubberBandFactory() },
        LocalNav provides nav,
        LocalOverlay provides overlay,
    ) {
        BlueprintTheme(dark, strings, data.settings.haptics) {
            val c = Theme.colors
            val rootBackdrop = rememberLayerBackdrop { drawRect(c.bg); drawContent() }
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().layerBackdrop(rootBackdrop)) {
                    ScreenBackground()
                    NavHost(nav) { route -> RouteContent(route, data, today) }
                }
                OverlayHost(overlay, rootBackdrop)
            }
        }
    }
}

@Composable
private fun RouteContent(route: Route, data: AppData, today: Long) {
    when (route) {
        Route.Tabs -> MainTabs(data, today)
        Route.Onboarding -> OnboardingFlow(showWelcome = true)
        Route.NewBlueprint -> OnboardingFlow(showWelcome = false)
        is Route.EditHabit -> EditHabitScreen(route)
        Route.History -> HistoryScreen(data, today)
        is Route.HabitDetail -> HabitDetailScreen(data, route.habitId, today)
    }
}

@Composable
private fun MainTabs(data: AppData, today: Long) {
    val bp = data.active ?: return
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val h = Theme.haptics
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val holder = rememberSaveableStateHolder()
    val backdrop = rememberLayerBackdrop { drawRect(c.bg); drawContent() }
    var confetti by remember { mutableStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            AnimatedContent(
                tab,
                transitionSpec = {
                    (fadeIn(tween(220, delayMillis = 40)) + scaleIn(spring(0.9f, 500f), initialScale = 0.985f))
                        .togetherWith(fadeOut(tween(140)))
                },
                label = "tabs"
            ) { tb ->
                holder.SaveableStateProvider(tb) {
                    when (tb) {
                        0 -> TodayScreen(data, bp, today) { if (data.settings.celebrate) { confetti++; showConfetti = true } }
                        1 -> ProgressScreen(data, bp, today)
                        else -> SettingsScreen(data, bp)
                    }
                }
            }
        }
        LiquidBottomTabs(
            selectedTabIndex = { tab },
            onTabSelected = { if (it != tab) { h.tick(); tab = it } },
            backdrop = backdrop,
            tabsCount = 3,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 36.dp).padding(bottom = 10.dp).widthIn(max = 420.dp).fillMaxWidth(),
        ) {
            val items = listOf(Icons.Rounded.CheckCircle to s.today, Icons.Rounded.BarChart to s.progress, Icons.Rounded.Settings to s.settings)
            items.forEachIndexed { i, (icon, label) ->
                LiquidBottomTab({ tab = i }) {
                    Icon(icon, label, Modifier.size(26.dp), tint = c.label)
                    T(label, t.caption2.copy(fontWeight = FontWeight.Medium), color = c.label, maxLines = 1)
                }
            }
        }
        if (showConfetti) Confetti(confetti) { showConfetti = false }
    }
}
