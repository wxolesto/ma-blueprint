package app.blueprint.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import app.blueprint.ui.components.fieldBg
import app.blueprint.ui.components.pressableDone
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Blueprint
import app.blueprint.data.Frequency
import app.blueprint.data.Repository
import app.blueprint.data.ThemeMode
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.Divider
import app.blueprint.ui.components.HabitIcons
import app.blueprint.ui.components.ListRow
import app.blueprint.ui.components.SectionHeader
import app.blueprint.ui.components.Stepper
import app.blueprint.ui.components.T
import app.blueprint.ui.glass.LiquidToggle
import app.blueprint.ui.glass.SegmentedControl
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.nav.Route
import app.blueprint.ui.theme.Theme
import app.blueprint.i18n.f
import app.blueprint.ui.theme.habit
import com.kyant.backdrop.backdrops.emptyBackdrop

@Composable
fun SettingsScreen(data: AppData, bp: Blueprint) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    val ctx = LocalContext.current
    val st = data.settings

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            ctx.contentResolver.openOutputStream(uri)?.use { it.write(Repository.exportJson().toByteArray()) }
            overlay.showToast(s.done)
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching { ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
            overlay.showToast(if (text != null && Repository.importJson(text)) s.importDone else s.importFailed)
        }
    }

    LargeTitleScreen(title = s.settings) { _ ->
        item(key = "bp_h") { SectionHeader(s.yourBlueprint) }
        item(key = "bp") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(s.name, icon = Icons.Rounded.Edit, iconColor = c.blue, value = bp.name, chevron = true, onClick = {
                    overlay.showSheet { dismiss -> RenameSheet(bp.name) { n -> Repository.updateActive { it.copy(name = n) }; dismiss() } }
                })
                Divider(60.dp)
                ListRow(s.length, icon = Icons.Rounded.Timelapse, iconColor = c.orange) {
                    T(s.nDays(bp.lengthDays), t.body, color = c.label2)
                    Spacer(Modifier.width(10.dp))
                    Stepper(bp.lengthDays, 7..365) { n -> Repository.updateActive { it.copy(lengthDays = n) } }
                }
                Divider(60.dp)
                ListRow(s.startDate, icon = Icons.Rounded.CalendarMonth, iconColor = c.red, value = s.dateShort(bp.startDay))
            }
        }
        item(key = "habits_h") { SectionHeader(s.habits) }
        item(key = "habits") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                bp.habits.forEachIndexed { i, h ->
                    if (i > 0) Divider(60.dp)
                    ListRow(
                        h.name, icon = HabitIcons.get(h.icon), iconColor = c.habit(h.color),
                        value = s.freqLabel(h.isDaily, h.frequency == Frequency.PerWeek, h.perWeek, h.days.size),
                        chevron = true, onClick = { editHabit(nav, h) }
                    )
                }
                if (bp.habits.isNotEmpty()) Divider(60.dp)
                ListRow(s.addHabit, icon = Icons.Rounded.Add, iconColor = c.green, titleColor = c.blue, onClick = {
                    nav.push(Route.EditHabit(null, true, onSave = { Repository.upsertHabit(it) }))
                })
            }
        }
        item(key = "focus_h") { SectionHeader(s.f.focus.uppercase()) }
        item(key = "focus") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(s.f.focusSettings, icon = Icons.Rounded.Timelapse, iconColor = Color(0xFFFF453A), value = "${s.f.n(data.pomo.focusMin)} · ${s.f.n(data.pomo.shortMin)} · ${s.f.n(data.pomo.longMin)}", chevron = true, onClick = { nav.push(Route.FocusSettings) })
                Divider(60.dp)
                ListRow(s.f.history, icon = Icons.Rounded.History, iconColor = Color(0xFF5E5CE6), chevron = true, onClick = { nav.push(Route.FocusHistory) })
            }
        }
        item(key = "app_h") { SectionHeader(s.appearance) }
        item(key = "app") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    T(s.theme, t.subhead.copy(fontWeight = FontWeight.Medium), color = c.label2)
                    Spacer(Modifier.height(10.dp))
                    SegmentedControl(listOf(s.system, s.light, s.dark), st.theme.ordinal, { i -> Repository.settings { it.copy(theme = ThemeMode.entries[i]) } })
                    Spacer(Modifier.height(16.dp))
                    T(s.language, t.subhead.copy(fontWeight = FontWeight.Medium), color = c.label2)
                    Spacer(Modifier.height(10.dp))
                    SegmentedControl(listOf("English", "فارسی"), if (st.language == "fa") 1 else 0, { i -> Repository.settings { it.copy(language = if (i == 1) "fa" else "en") } })
                }
                Divider(16.dp)
                ListRow(s.haptics, icon = Icons.Rounded.Vibration, iconColor = Color(0xFF5E5CE6)) {
                    LiquidToggle({ st.haptics }, { v -> Repository.settings { it.copy(haptics = v) } }, emptyBackdrop())
                }
                Divider(60.dp)
                ListRow(s.quotes, icon = Icons.Rounded.FormatQuote, iconColor = c.orange) {
                    LiquidToggle({ st.quotes }, { v -> Repository.settings { it.copy(quotes = v) } }, emptyBackdrop())
                }
                Divider(60.dp)
                ListRow(s.celebrate, icon = Icons.Rounded.Celebration, iconColor = Color(0xFFFF375F)) {
                    LiquidToggle({ st.celebrate }, { v -> Repository.settings { it.copy(celebrate = v) } }, emptyBackdrop())
                }
            }
        }
        item(key = "data_h") { SectionHeader(s.data) }
        item(key = "data") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(s.exportBackup, icon = Icons.Rounded.Upload, iconColor = c.blue, chevron = true, onClick = {
                    exporter.launch("blueprint-backup-${java.time.LocalDate.now()}.json")
                })
                Divider(60.dp)
                ListRow(s.importBackup, icon = Icons.Rounded.Download, iconColor = c.green, chevron = true, onClick = {
                    importer.launch(arrayOf("application/json", "text/plain", "*/*"))
                })
                Divider(60.dp)
                ListRow(s.pastBlueprints, icon = Icons.Rounded.History, iconColor = Color(0xFF8E8E93), value = s.num(data.blueprints.size), chevron = true, onClick = { nav.push(Route.History) })
                Divider(60.dp)
                ListRow(s.newBlueprint, icon = Icons.Rounded.RestartAlt, iconColor = c.red, titleColor = c.red, onClick = {
                    overlay.alert(s.newBlueprint, s.newBlueprintMsg, listOf(AlertAction(s.cancel), AlertAction(s.continueTxt, bold = true) { nav.push(Route.NewBlueprint) }))
                })
            }
        }
        item(key = "about_h") { SectionHeader(s.about) }
        item(key = "about") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                ListRow(s.version, icon = Icons.Rounded.Info, iconColor = Color(0xFF8E8E93), value = "1.0.0")
                Divider(60.dp)
                ListRow("Liquid Glass", icon = Icons.Rounded.AutoAwesome, iconColor = Color(0xFF64D2FF), value = if (android.os.Build.VERSION.SDK_INT >= 33) "✓" else (if (s.isFa) "ساده" else "Basic"))
            }
        }
    }
}

@Composable
fun RenameSheet(initial: String, onDone: (String) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    var text by remember { mutableStateOf(initial) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    androidx.compose.runtime.LaunchedEffect(Unit) { kotlinx.coroutines.delay(350); runCatching { focus.requestFocus() } }
    Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp)) {
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            T(s.name, t.title3, Modifier.weight(1f))
            Box(Modifier.pressableDone { if (text.isNotBlank()) onDone(text.trim()) }) { T(s.done, t.headline, color = Color.White) }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().then(Modifier.fieldBg()).padding(14.dp)) {
            androidx.compose.foundation.text.BasicTextField(text, { text = it.take(40) }, Modifier.fillMaxWidth().focusRequester(focus), singleLine = true,
                textStyle = t.body.copy(color = c.label), cursorBrush = androidx.compose.ui.graphics.SolidColor(c.blue))
        }
        Spacer(Modifier.height(8.dp))
    }
}
