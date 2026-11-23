package app.blueprint.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.blueprint.data.Frequency
import app.blueprint.data.Habit
import app.blueprint.data.Repository
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.Divider
import app.blueprint.ui.components.HabitIcons
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.IconTile
import app.blueprint.ui.components.ListRow
import app.blueprint.ui.components.SectionHeader
import app.blueprint.ui.components.Stepper
import app.blueprint.ui.components.T
import app.blueprint.ui.components.WheelPicker
import app.blueprint.ui.components.pressable
import app.blueprint.ui.glass.GlassTextButton
import app.blueprint.ui.glass.LiquidToggle
import app.blueprint.ui.glass.SegmentedControl
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.nav.Route
import app.blueprint.ui.theme.HabitColorsDark
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditHabitScreen(route: Route.EditHabit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val hap = Theme.haptics
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    var h by remember { mutableStateOf(route.initial ?: Habit(Repository.newId(), "", icon = "star", color = (0..11).random())) }
    val color = c.habit(h.color)
    val ctx = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun save() {
        if (h.name.isBlank()) { hap.reject(); return }
        route.onSave(h.copy(name = h.name.trim(), description = h.description.trim()))
        hap.confirm()
        nav.pop()
    }

    LargeTitleScreen(
        title = if (route.isNew) s.newHabit else s.editHabit,
        onBack = { if (!route.isNew && h.name.isNotBlank()) route.onSave(h.copy(name = h.name.trim())); nav.pop() },
        bottomPadding = 40.dp,
        actions = { b -> GlassTextButton(b, if (route.isNew) s.add else s.done, { save() }, tint = c.blue) },
    ) { _ ->
        // preview
        item(key = "preview") {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val bg by animateColorAsState(color, spring(1f, 300f), label = "c")
                Box(Modifier.size(92.dp).clip(RoundedRectangle(28.dp)).background(bg.copy(alpha = 0.2f)).pressable(0.92f) {
                    overlay.showSheet { dismiss -> IconSheet(h.icon, color) { h = h.copy(icon = it); dismiss() } }
                }, contentAlignment = Alignment.Center) {
                    AnimatedContent(h.icon, transitionSpec = { (fadeIn(spring(stiffness = 500f)) togetherWith fadeOut(spring(stiffness = 500f))) }, label = "icon") { key ->
                        Icon(HabitIcons.get(key), null, Modifier.size(48.dp), tint = bg)
                    }
                }
            }
        }
        item(key = "name_h") { SectionHeader(s.nameAndIcon) }
        item(key = "name") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.pressable(0.9f) { overlay.showSheet { dismiss -> IconSheet(h.icon, color) { h = h.copy(icon = it); dismiss() } } }) {
                        IconTile(HabitIcons.get(h.icon), color, 38.dp, 11.dp, 22.dp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (h.name.isEmpty()) T(s.habitName, t.body, color = c.label3)
                        BasicTextField(h.name, { h = h.copy(name = it.take(40)) }, Modifier.fillMaxWidth(), singleLine = true, textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(c.blue))
                    }
                }
            }
        }
        item(key = "desc_h") { SectionHeader(s.description) }
        item(key = "desc") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                Box(Modifier.padding(16.dp).heightIn(min = 44.dp)) {
                    if (h.description.isEmpty()) T(s.descriptionHint, t.body, color = c.label3)
                    BasicTextField(h.description, { h = h.copy(description = it.take(200)) }, Modifier.fillMaxWidth(), textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(c.blue))
                }
            }
        }
        item(key = "color_h") { SectionHeader(s.color) }
        item(key = "color") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                FlowRow(Modifier.padding(14.dp), maxItemsInEachRow = 6, horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitColorsDark.indices.forEach { i ->
                        val sel = i == h.color
                        val sc by animateFloatAsState(if (sel) 1f else 0.86f, spring(0.5f, 500f), label = "s")
                        Box(Modifier.size(44.dp).graphicsLayer { scaleX = sc; scaleY = sc }.pressable(0.85f) { hap.tick(); h = h.copy(color = i) }, contentAlignment = Alignment.Center) {
                            if (sel) Box(Modifier.size(44.dp).border(2.5.dp, c.habit(i), CircleShape))
                            Box(Modifier.size(34.dp).clip(CircleShape).background(c.habit(i)), contentAlignment = Alignment.Center) {
                                if (sel) Icon(Icons.Rounded.Check, null, Modifier.size(18.dp), tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
        item(key = "freq_h") { SectionHeader(s.frequency) }
        item(key = "freq") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    SegmentedControl(listOf(s.scheduled, s.perWeek), if (h.frequency == Frequency.Scheduled) 0 else 1, {
                        h = h.copy(frequency = if (it == 0) Frequency.Scheduled else Frequency.PerWeek)
                    })
                    Spacer(Modifier.height(14.dp))
                    AnimatedContent(h.frequency, transitionSpec = {
                        (fadeIn(spring(stiffness = 400f)) togetherWith fadeOut(spring(stiffness = 600f)))
                    }, label = "freq") { f ->
                        if (f == Frequency.Scheduled) {
                            Column {
                                T(s.activeDays, t.caption.copy(fontWeight = FontWeight.SemiBold), color = c.label2)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val order = (0 until 7).map { ((s.weekStart - 1 + it) % 7) + 1 }
                                    order.forEach { d ->
                                        val on = d in h.days
                                        val bg by animateColorAsState(if (on) color else c.fill.copy(alpha = 0.3f), spring(1f, 500f), label = "d")
                                        Box(
                                            Modifier.weight(1f).aspectRatio(1f).pressable(0.86f) {
                                                hap.tick()
                                                val nd = if (on) h.days - d else h.days + d
                                                if (nd.isNotEmpty()) h = h.copy(days = nd)
                                            }.clip(CircleShape).background(bg),
                                            contentAlignment = Alignment.Center
                                        ) { T(s.weekdayLetter(d), t.subhead.copy(fontWeight = FontWeight.SemiBold), color = if (on) Color.White else c.label2) }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QuickChip(s.daily) { h = h.copy(days = (1..7).toSet()) }
                                    QuickChip(if (s.isFa) "روزهای کاری" else "Weekdays") { h = h.copy(days = if (s.isFa) setOf(6, 7, 1, 2, 3) else (1..5).toSet()) }
                                    QuickChip(if (s.isFa) "آخر هفته" else "Weekends") { h = h.copy(days = if (s.isFa) setOf(4, 5) else setOf(6, 7)) }
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                T(s.timesAWeek(h.perWeek), t.body, Modifier.weight(1f))
                                Stepper(h.perWeek, 1..7) { h = h.copy(perWeek = it) }
                            }
                        }
                    }
                }
            }
        }
        item(key = "tpd_h") { AnimatedVisibility(h.frequency == Frequency.Scheduled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { SectionHeader(s.timesPerDay) } }
        item(key = "tpd") {
            AnimatedVisibility(h.frequency == Frequency.Scheduled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Card(Modifier.padding(horizontal = 16.dp)) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        T(s.timesADay(h.timesPerDay), t.body, Modifier.weight(1f))
                        Stepper(h.timesPerDay, 1..12) { h = h.copy(timesPerDay = it) }
                    }
                }
            }
        }
        item(key = "rem_h") { SectionHeader(s.reminder) }
        item(key = "rem") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(s.reminderTime, icon = Icons.Rounded.Notifications, iconColor = c.red) {
                    LiquidToggle({ h.reminder != null }, { on ->
                        if (on && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        h = h.copy(reminder = if (on) (h.reminder ?: (9 * 60)) else null)
                    }, emptyBackdrop())
                }
                AnimatedVisibility(h.reminder != null, enter = fadeIn() + expandVertically(spring(0.9f, 400f)), exit = fadeOut() + shrinkVertically(spring(1f, 500f))) {
                    Column {
                        Divider(60.dp)
                        ListRow(if (s.isFa) "ساعت" else "Time", value = h.reminder?.let { s.time(it) } ?: "", onClick = {
                            overlay.showSheet { dismiss -> TimeSheet(h.reminder ?: 540) { h = h.copy(reminder = it); dismiss() } }
                        })
                    }
                }
            }
        }
        if (!route.isNew && route.onDelete != null) {
            item(key = "remove") {
                Spacer(Modifier.height(28.dp))
                Box(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(54.dp).pressable(0.97f) {
                        hap.heavy()
                        overlay.alert(s.removeHabitQ, s.removeHabitMsg, listOf(
                            AlertAction(s.cancel),
                            AlertAction(s.remove, destructive = true) { route.onDelete.invoke(); nav.pop() }
                        ))
                    }.clip(Capsule()).background(c.red.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Delete, null, Modifier.size(20.dp), tint = c.red)
                        Spacer(Modifier.width(8.dp))
                        T(s.removeHabit, t.headline, color = c.red)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    val c = Theme.colors
    Box(Modifier.pressable(0.92f) { onClick() }.clip(Capsule()).background(c.fill.copy(alpha = 0.3f)).padding(horizontal = 12.dp, vertical = 7.dp)) {
        T(text, Theme.type.footnote.copy(fontWeight = FontWeight.Medium), color = c.label)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconSheet(current: String, color: Color, onPick: (String) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val hap = Theme.haptics
    Column(Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)) {
        T(s.icon, t.title3, Modifier.padding(horizontal = 4.dp))
        Spacer(Modifier.height(14.dp))
        FlowRow(Modifier.fillMaxWidth(), maxItemsInEachRow = 6, horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HabitIcons.all.forEach { (k, v) ->
                val sel = k == current
                Box(
                    Modifier.size(50.dp).pressable(0.85f) { hap.tick(); onPick(k) }.clip(RoundedRectangle(15.dp))
                        .background(if (sel) color else c.fill.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) { Icon(v, null, Modifier.size(26.dp), tint = if (sel) Color.White else color) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun TimeSheet(initial: Int, onDone: (Int) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    var hh by remember { mutableStateOf(initial / 60) }
    var mm by remember { mutableStateOf(initial % 60) }
    Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(s.reminderTime, t.title3, Modifier.weight(1f))
            Box(Modifier.clip(Capsule()).background(c.blue).pressable(0.94f) { onDone(hh * 60 + mm) }.padding(horizontal = 18.dp, vertical = 9.dp)) {
                T(s.done, t.headline, color = Color.White)
            }
        }
        Spacer(Modifier.height(10.dp))
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WheelPicker(24, hh, { hh = it }, { s.num(it).padStart(2, if (s.isFa) '۰' else '0') })
                T(":", t.title2)
                WheelPicker(60, mm, { mm = it }, { s.num(it).padStart(2, if (s.isFa) '۰' else '0') })
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
