package app.blueprint.ui.focus

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Phase
import app.blueprint.data.PomoSettings
import app.blueprint.data.Repository
import app.blueprint.data.deleteSession
import app.blueprint.data.focusMsOn
import app.blueprint.data.pomo
import app.blueprint.data.pomosOn
import app.blueprint.data.sessionsOn
import app.blueprint.focus.Pomodoro
import app.blueprint.i18n.f
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.Chip
import app.blueprint.ui.components.Divider
import app.blueprint.ui.components.ListRow
import app.blueprint.ui.components.Stepper
import app.blueprint.ui.components.T
import app.blueprint.ui.components.WheelPicker
import app.blueprint.ui.components.pressable
import app.blueprint.ui.components.pressableDone
import app.blueprint.ui.glass.LiquidToggle
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.screens.LargeTitleScreen
import app.blueprint.ui.theme.Theme
import com.kyant.backdrop.backdrops.emptyBackdrop

// ------------------------------------------------------------------ history

@Composable
fun FocusHistoryScreen(data: AppData, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    var selected by remember { mutableLongStateOf(today) }
    val days = remember(data.sessions) { data.sessions.map { it.day }.distinct().sortedDescending() }
    LargeTitleScreen(title = f.history, onBack = { nav.pop() }, bottomPadding = 40.dp) { _ ->
        item(key = "chart_h") { Header(f.last7) }
        item(key = "chart") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                WeekBars(data, today, selected) { selected = it }
            }
        }
        item(key = "sel_h") {
            Header(s.dateLong(selected), trailing = {
                T("${f.dayTotal}: ${f.dur(data.focusMsOn(selected))}", t.footnote.copy(fontWeight = FontWeight.Medium), color = c.label2)
            })
        }
        item(key = "sel") {
            val list = data.sessionsOn(selected)
            Card(Modifier.padding(horizontal = 16.dp)) {
                if (list.isEmpty()) EmptyState(f.noSessions, f.noSessionsSub, Icons.Rounded.Timeline, c.phase(Phase.Focus))
                else {
                    Spacer(Modifier.height(4.dp))
                    list.forEachIndexed { i, ss ->
                        TimelineRow(ss, ss.label.ifBlank { f.phaseName(ss.phase) }, c.phase(ss.phase), i == list.lastIndex, onLongPress = {
                            overlay.alert(f.deleteSessionQ, "${f.clock(ss.start)} – ${f.clock(ss.end)}", listOf(AlertAction(s.cancel), AlertAction(f.delete, destructive = true) { Repository.deleteSession(ss.id) }))
                        })
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
        val older = days.filter { it != selected }.take(30)
        items(older, key = { "d$it" }) { d ->
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth().pressable(0.98f) { selected = d }
                    .clip(com.kyant.shapes.RoundedRectangle(20.dp)).background(c.card).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    T(s.dateLong(d), t.headline)
                    T("${f.pomos(data.pomosOn(d))} · ${f.dur(data.focusMsOn(d))}", t.footnote, color = c.label2)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(data.pomosOn(d).coerceAtMost(8)) { Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(c.phase(Phase.Focus))) }
                }
            }
        }
    }
}

@Composable
private fun WeekBars(data: AppData, today: Long, selected: Long, onSelect: (Long) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val hap = Theme.haptics
    val days = (6 downTo 0).map { today - it }
    val values = days.map { data.focusMsOn(it) / 60000f }
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(30f)
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, spring(0.8f, 120f)) }
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            T(f.dur((values.sum() * 60000).toLong()), t.title2)
            Spacer(Modifier.width(8.dp))
            T(f.pomos(days.sumOf { data.pomosOn(it) }), t.subhead, color = c.label2, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            days.forEachIndexed { i, d ->
                val sel = d == selected
                Column(Modifier.weight(1f).pressable(0.92f) { hap.tick(); onSelect(d) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (values[i] > 0) T(f.n(values[i].toInt()), t.caption2, color = if (sel) c.label else c.label3)
                    Spacer(Modifier.height(3.dp))
                    Canvas(Modifier.width(22.dp).height(110.dp)) {
                        val h = size.height * (values[i] / max) * grow.value
                        drawRoundRect(c.fill.copy(alpha = 0.25f), Offset.Zero, size, CornerRadius(7.dp.toPx()))
                        if (h > 0f) drawRoundRect(if (sel) c.phase(Phase.Focus) else c.phase(Phase.Focus).copy(alpha = 0.5f), Offset(0f, size.height - h), Size(size.width, h), CornerRadius(7.dp.toPx()))
                    }
                    Spacer(Modifier.height(6.dp))
                    T(s.weekdayShort(java.time.LocalDate.ofEpochDay(d).dayOfWeek.value), t.caption.copy(fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal), color = if (sel) c.label else c.label2)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ settings

private data class Preset(val focus: Int, val short: Int, val long: Int, val every: Int)
private val presets = listOf(Preset(25, 5, 15, 4), Preset(50, 10, 30, 3), Preset(15, 3, 10, 4), Preset(90, 20, 30, 2))

@Composable
fun FocusSettingsScreen(data: AppData) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    val ctx = LocalContext.current
    val ps = data.pomo
    fun set(block: (PomoSettings) -> PomoSettings) { Repository.pomo(block); Pomodoro.refreshNotification(ctx) }
    fun minutesSheet(title: String, value: Int, range: IntRange, apply: (Int) -> Unit) {
        overlay.showSheet { dismiss -> MinutesSheet(title, value, range) { apply(it); dismiss() } }
    }

    LargeTitleScreen(title = f.focusSettings, onBack = { nav.pop() }, bottomPadding = 40.dp) { _ ->
        item(key = "pre_h") { Header(f.presets) }
        item(key = "pre") {
            androidx.compose.foundation.lazy.LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(presets.size) { i ->
                    val p = presets[i]
                    val sel = ps.focusMin == p.focus && ps.shortMin == p.short && ps.longMin == p.long && ps.longEvery == p.every
                    Column(
                        Modifier.width(128.dp).pressable(0.95f) { set { it.copy(focusMin = p.focus, shortMin = p.short, longMin = p.long, longEvery = p.every) } }
                            .clip(com.kyant.shapes.RoundedRectangle(20.dp)).background(if (sel) c.phase(Phase.Focus) else c.card).padding(14.dp)
                    ) {
                        T(f.presetNames[i], t.headline, color = if (sel) Color.White else c.label)
                        Spacer(Modifier.height(4.dp))
                        T("${f.n(p.focus)} · ${f.n(p.short)} · ${f.n(p.long)}", t.subhead, color = if (sel) Color.White.copy(alpha = 0.85f) else c.label2)
                    }
                }
            }
        }
        item(key = "dur_h") { Header(f.durations) }
        item(key = "dur") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(f.focusLen, icon = Icons.Rounded.Timer, iconColor = c.phase(Phase.Focus), value = f.minutes(ps.focusMin), chevron = true, onClick = {
                    minutesSheet(f.focusLen, ps.focusMin, 1..180) { v -> set { it.copy(focusMin = v) } }
                })
                Divider(60.dp)
                ListRow(f.shortLen, icon = Icons.Rounded.Coffee, iconColor = c.green, value = f.minutes(ps.shortMin), chevron = true, onClick = {
                    minutesSheet(f.shortLen, ps.shortMin, 1..60) { v -> set { it.copy(shortMin = v) } }
                })
                Divider(60.dp)
                ListRow(f.longLen, icon = Icons.Rounded.Hotel, iconColor = c.blue, value = f.minutes(ps.longMin), chevron = true, onClick = {
                    minutesSheet(f.longLen, ps.longMin, 1..90) { v -> set { it.copy(longMin = v) } }
                })
                Divider(60.dp)
                ListRow(f.longEvery, icon = Icons.Rounded.Loop, iconColor = Color(0xFF5E5CE6)) {
                    T(f.n(ps.longEvery), t.body, color = c.label2); Spacer(Modifier.width(10.dp))
                    Stepper(ps.longEvery, 1..12) { v -> set { it.copy(longEvery = v) } }
                }
                Divider(60.dp)
                ListRow(f.dailyGoal, icon = Icons.Rounded.EmojiEvents, iconColor = c.orange) {
                    T(f.n(ps.dailyGoal), t.body, color = c.label2); Spacer(Modifier.width(10.dp))
                    Stepper(ps.dailyGoal, 1..30) { v -> set { it.copy(dailyGoal = v) } }
                }
            }
        }
        item(key = "auto_h") { Header(f.automation) }
        item(key = "auto") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(f.autoBreaks, Icons.Rounded.AutoMode, c.green, ps.autoBreaks) { v -> set { it.copy(autoBreaks = v) } }
                Divider(60.dp)
                ToggleRow(f.autoFocus, Icons.Rounded.PlayCircle, c.phase(Phase.Focus), ps.autoFocus) { v -> set { it.copy(autoFocus = v) } }
                Divider(60.dp)
                ToggleRow(f.autoComplete, Icons.Rounded.DoneAll, c.blue, ps.autoCompleteTask, f.autoCompleteSub) { v -> set { it.copy(autoCompleteTask = v) } }
            }
        }
        item(key = "not_h") { Header(f.notifications) }
        item(key = "not") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(f.notifyDone, Icons.Rounded.Notifications, c.red, ps.notify) { v -> set { it.copy(notify = v) } }
                Divider(60.dp)
                ToggleRow(f.sound, Icons.Rounded.MusicNote, Color(0xFFFF375F), ps.sound) { v -> set { it.copy(sound = v) } }
                Divider(60.dp)
                ToggleRow(f.vibration, Icons.Rounded.Vibration, Color(0xFF5E5CE6), ps.vibrate) { v -> set { it.copy(vibrate = v) } }
                Divider(60.dp)
                ToggleRow(f.ongoingNotif, Icons.Rounded.NotificationsActive, c.orange, ps.ongoing, f.ongoingSub) { v -> set { it.copy(ongoing = v) } }
                Divider(60.dp)
                ListRow(f.channelSettings, icon = Icons.Rounded.MusicNote, iconColor = Color(0xFF8E8E93), chevron = true, onClick = {
                    val id = Pomodoro.doneChannelId(ctx)
                    runCatching {
                        ctx.startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName).putExtra(Settings.EXTRA_CHANNEL_ID, id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                })
                if (Build.VERSION.SDK_INT >= 31) {
                    val am = ctx.getSystemService(AlarmManager::class.java)
                    if (!am.canScheduleExactAlarms()) {
                        Divider(60.dp)
                        ListRow(f.exactAlarm, icon = Icons.Rounded.AlarmOn, iconColor = c.red, value = f.exactAlarmSub, chevron = true, onClick = {
                            runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, android.net.Uri.parse("package:" + ctx.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                        })
                    }
                }
            }
        }
        item(key = "beh_h") { Header(f.behavior) }
        item(key = "beh") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(f.keepScreenOn, Icons.Rounded.LightMode, c.yellow, ps.keepScreenOn) { v -> set { it.copy(keepScreenOn = v) } }
                Divider(60.dp)
                ToggleRow(f.lastSecondsHaptic, Icons.Rounded.TouchApp, Color(0xFF64D2FF), ps.lastSecondsHaptic) { v -> set { it.copy(lastSecondsHaptic = v) } }
                Divider(60.dp)
                ToggleRow(f.logBreaks, Icons.Rounded.Timeline, c.green, ps.logBreaks) { v -> set { it.copy(logBreaks = v) } }
                Divider(60.dp)
                ListRow(f.minLog, icon = Icons.Rounded.FilterAlt, iconColor = Color(0xFFAC8E68)) {
                    T(f.minutes(ps.minLogMin), t.body, color = c.label2, maxLines = 1); Spacer(Modifier.width(10.dp))
                    Stepper(ps.minLogMin, 0..15) { v -> set { it.copy(minLogMin = v) } }
                }
            }
        }
        item(key = "data_h") { Header(f.dataSec) }
        item(key = "data") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(f.resetDefaults, icon = Icons.Rounded.RestartAlt, iconColor = c.blue, titleColor = c.blue, onClick = {
                    overlay.alert(f.resetDefaultsQ, null, listOf(AlertAction(s.cancel), AlertAction(f.reset, bold = true) { set { PomoSettings() } }))
                })
                Divider(60.dp)
                ListRow(f.clearHistory, icon = Icons.Rounded.DeleteSweep, iconColor = c.red, titleColor = c.red, onClick = {
                    overlay.alert(f.clearHistoryQ, null, listOf(AlertAction(s.cancel), AlertAction(f.delete, destructive = true) { Repository.update { it.copy(sessions = emptyList()) } }))
                })
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, value: Boolean, sub: String? = null, onChange: (Boolean) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        app.blueprint.ui.components.IconTile(icon, color)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            T(title, t.body, maxLines = 2)
            if (sub != null) T(sub, t.footnote, color = c.label2)
        }
        Spacer(Modifier.width(8.dp))
        LiquidToggle({ value }, onChange, emptyBackdrop())
    }
}

@Composable
fun MinutesSheet(title: String, initial: Int, range: IntRange, onDone: (Int) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    var v by remember { mutableStateOf(initial.coerceIn(range)) }
    var key by remember { mutableStateOf(0) }
    Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(title, t.title3, Modifier.weight(1f))
            Box(Modifier.pressableDone { onDone(v) }) { T(s.done, t.headline, color = Color.White) }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 15, 25, 45, 60).filter { it in range }.forEach { m ->
                Chip(f.n(m), v == m, Modifier.weight(1f)) { v = m; key++ }
            }
        }
        Spacer(Modifier.height(6.dp))
        androidx.compose.runtime.key(key) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WheelPicker(range.last - range.first + 1, v - range.first, { v = it + range.first }, { f.n(it + range.first) }, width = 110.dp, cyclic = false)
                T(if (f.fa) "دقیقه" else "min", t.title3.copy(fontWeight = FontWeight.Normal), color = c.label2)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
