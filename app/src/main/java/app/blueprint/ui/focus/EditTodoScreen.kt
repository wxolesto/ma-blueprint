package app.blueprint.ui.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Phase
import app.blueprint.data.Repository
import app.blueprint.data.SubTask
import app.blueprint.data.Todo
import app.blueprint.data.deleteSession
import app.blueprint.data.deleteTodo
import app.blueprint.data.focusMsFor
import app.blueprint.data.pomosFor
import app.blueprint.data.upsertTodo
import app.blueprint.i18n.f
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.CheckCircle
import app.blueprint.ui.components.Chip
import app.blueprint.ui.components.Divider
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.ListRow
import app.blueprint.ui.components.PrimaryButton
import app.blueprint.ui.components.Stepper
import app.blueprint.ui.components.T
import app.blueprint.ui.components.WheelPicker
import app.blueprint.ui.components.pressable
import app.blueprint.ui.components.pressableDone
import app.blueprint.ui.glass.GlassTextButton
import app.blueprint.ui.glass.SegmentedControl
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.screens.LargeTitleScreen
import app.blueprint.ui.theme.HabitColorsDark
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTodoScreen(data: AppData, todoId: String?, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    val ctx = LocalContext.current
    val tabs = LocalTabSelect.current
    val hap = Theme.haptics
    val existing = data.todos.firstOrNull { it.id == todoId }
    val isNew = existing == null
    var td by remember { mutableStateOf(existing ?: Todo(Repository.newId(), "", color = 1)) }
    var newSub by remember { mutableStateOf("") }
    val color = c.habit(td.color)

    fun commit(): Boolean {
        if (td.title.isBlank()) return false
        // keep the latest done flag (it may have been changed from elsewhere)
        val cur = Repository.data.value.todos.firstOrNull { it.id == td.id }
        Repository.upsertTodo(td.copy(title = td.title.trim(), notes = td.notes.trim(), done = cur?.done ?: td.done, doneAt = cur?.doneAt ?: td.doneAt))
        return true
    }

    LargeTitleScreen(
        title = if (isNew) f.newTask else f.editTask,
        onBack = { if (!isNew) commit(); nav.pop() },
        bottomPadding = 40.dp,
        actions = { b ->
            GlassTextButton(b, if (isNew) s.add else s.done, {
                if (commit()) { hap.confirm(); nav.pop() } else hap.reject()
            }, tint = c.blue)
        },
    ) { _ ->
        item(key = "title") {
            Card(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Box {
                        if (td.title.isEmpty()) T(f.taskTitle, t.title3, color = c.label3)
                        BasicTextField(td.title, { td = td.copy(title = it.take(120)) }, Modifier.fillMaxWidth(), textStyle = t.title3.copy(color = c.label), cursorBrush = SolidColor(color))
                    }
                    Spacer(Modifier.height(10.dp))
                    Divider(0.dp)
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.heightIn(min = 60.dp)) {
                        if (td.notes.isEmpty()) T(f.notesHint, t.body, color = c.label3)
                        BasicTextField(td.notes, { td = td.copy(notes = it.take(2000)) }, Modifier.fillMaxWidth(), textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(color))
                    }
                }
            }
        }
        item(key = "details") {
            Card(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                ListRow(
                    f.dueDate, icon = Icons.Rounded.CalendarMonth, iconColor = c.red,
                    value = td.dueDay?.let { d -> when (d) { today -> f.todaySec; today + 1 -> f.tomorrow; else -> s.dateShort(d) } } ?: f.none,
                    chevron = true,
                    onClick = { overlay.showSheet { dismiss -> DateSheet(td.dueDay, today) { td = td.copy(dueDay = it); dismiss() } } }
                )
                Divider(60.dp)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        app.blueprint.ui.components.IconTile(Icons.Rounded.Flag, c.orange)
                        Spacer(Modifier.width(14.dp))
                        T(f.priority, t.body)
                    }
                    Spacer(Modifier.height(10.dp))
                    SegmentedControl(f.priorities, td.priority, { hap.tick(); td = td.copy(priority = it) })
                }
                Divider(60.dp)
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    app.blueprint.ui.components.IconTile(Icons.Rounded.Timer, c.phase(Phase.Focus))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        T(f.estimate, t.body, maxLines = 2)
                        T(if (td.estimate == 0) f.none else f.pomos(td.estimate), t.footnote, color = c.label2)
                    }
                    Stepper(td.estimate, 0..30) { td = td.copy(estimate = it) }
                }
            }
        }
        item(key = "color_h") { Header(f.color) }
        item(key = "color") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                FlowRow(Modifier.padding(14.dp), maxItemsInEachRow = 6, horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitColorsDark.indices.forEach { i ->
                        val sel = i == td.color
                        val sc by animateFloatAsState(if (sel) 1f else 0.86f, spring(0.5f, 500f), label = "s")
                        Box(Modifier.size(42.dp).graphicsLayer { scaleX = sc; scaleY = sc }.pressable(0.85f) { hap.tick(); td = td.copy(color = i) }, contentAlignment = Alignment.Center) {
                            if (sel) Box(Modifier.size(42.dp).border(2.5.dp, c.habit(i), CircleShape))
                            Box(Modifier.size(32.dp).clip(CircleShape).background(c.habit(i)), contentAlignment = Alignment.Center) {
                                if (sel) Icon(Icons.Rounded.Check, null, Modifier.size(18.dp), tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
        item(key = "sub_h") { Header(f.subtasks, trailing = if (td.subtasks.isEmpty()) null else ({ T("${f.n(td.subtasks.count { it.done })}/${f.n(td.subtasks.size)}", t.footnote, color = c.label2) })) }
        item(key = "sub") {
            Card(Modifier.padding(horizontal = 16.dp)) {
                td.subtasks.forEach { st ->
                    Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.pressable(0.85f) { hap.tick(); td = td.copy(subtasks = td.subtasks.map { if (it.id == st.id) it.copy(done = !it.done) else it }) }) {
                            CheckCircle(if (st.done) 1 else 0, 1, color, size = 22.dp)
                        }
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(
                            st.title, { v -> td = td.copy(subtasks = td.subtasks.map { if (it.id == st.id) it.copy(title = v.take(100)) else it }) },
                            Modifier.weight(1f), singleLine = true,
                            textStyle = t.body.copy(color = if (st.done) c.label2 else c.label, textDecoration = if (st.done) TextDecoration.LineThrough else null), cursorBrush = SolidColor(color)
                        )
                        Box(Modifier.size(32.dp).pressable(0.85f) { td = td.copy(subtasks = td.subtasks.filterNot { it.id == st.id }) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, null, Modifier.size(18.dp), tint = c.label3)
                        }
                    }
                    Divider(48.dp)
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(22.dp), tint = c.blue)
                    Spacer(Modifier.width(12.dp))
                    fun addSub() { if (newSub.isNotBlank()) { td = td.copy(subtasks = td.subtasks + SubTask(Repository.newId(), newSub.trim())); newSub = "" } }
                    Box(Modifier.weight(1f)) {
                        if (newSub.isEmpty()) T(f.addSubtask, t.body, color = c.blue)
                        BasicTextField(newSub, { newSub = it.take(100) }, Modifier.fillMaxWidth(), singleLine = true, textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(c.blue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { addSub() }))
                    }
                    if (newSub.isNotBlank()) Box(Modifier.pressable(0.9f) { addSub() }.padding(6.dp)) { T(s.add, t.subhead.copy(fontWeight = FontWeight.SemiBold), color = c.blue) }
                }
            }
        }
        if (!isNew) {
            item(key = "focus_h") { Header(f.focus) }
            item(key = "focus") {
                val pom = data.pomosFor(td.id)
                val ms = data.focusMsFor(td.id)
                Card(Modifier.padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row {
                            Column(Modifier.weight(1f)) { T(f.dur(ms), t.title3); T(f.totalFocus, t.footnote, color = c.label2) }
                            Column(Modifier.weight(1f)) {
                                T(if (td.estimate > 0) "${f.n(pom)} / ${f.n(td.estimate)}" else f.n(pom), t.title3, color = c.phase(Phase.Focus))
                                T(f.pomodoros, t.footnote, color = c.label2)
                            }
                        }
                        if (td.estimate > 0) {
                            Spacer(Modifier.height(10.dp))
                            app.blueprint.ui.components.ProgressBar(pom.toFloat() / td.estimate, c.phase(Phase.Focus))
                        }
                        if (!td.done) {
                            Spacer(Modifier.height(14.dp))
                            PrimaryButton(f.focusOnTask, gradient = listOf(c.phase(Phase.Focus), Color(0xFFFF9F0A))) {
                                commit(); hap.confirm(); nav.pop(); startFocusOn(ctx, td, tabs)
                            }
                        }
                    }
                }
            }
            val sess = data.sessions.filter { it.todoId == td.id }.sortedByDescending { it.start }
            if (sess.isNotEmpty()) {
                item(key = "sess_h") { Header(f.sessionsTitle) }
                item(key = "sess") {
                    Card(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(4.dp))
                        sess.take(50).forEachIndexed { i, ss ->
                            TimelineRow(ss, s.dateLong(ss.day), c.phase(ss.phase), i == minOf(sess.size, 50) - 1, onLongPress = {
                                overlay.alert(f.deleteSessionQ, null, listOf(AlertAction(s.cancel), AlertAction(f.delete, destructive = true) { Repository.deleteSession(ss.id) }))
                            })
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
            item(key = "delete") {
                Card(Modifier.padding(horizontal = 16.dp, vertical = 22.dp)) {
                    ListRow(f.deleteTask, icon = Icons.Rounded.Delete, iconColor = c.red, titleColor = c.red, onClick = {
                        overlay.alert(f.deleteTaskQ, td.title, listOf(AlertAction(s.cancel), AlertAction(f.delete, destructive = true) {
                            Repository.deleteTodo(td.id); nav.pop()
                        }))
                    })
                }
            }
        }
    }
}

@Composable
fun DateSheet(initial: Long?, today: Long, onDone: (Long?) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    var v by remember { mutableStateOf(initial) }
    var key by remember { mutableStateOf(0) }
    Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(f.dueDate, t.title3, Modifier.weight(1f))
            Box(Modifier.pressableDone { onDone(v) }) { T(s.done, t.headline, color = Color.White) }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(f.none, v == null, Modifier.weight(1f)) { v = null }
            Chip(f.todaySec, v == today, Modifier.weight(1f)) { v = today; key++ }
            Chip(f.tomorrow, v == today + 1, Modifier.weight(1f)) { v = today + 1; key++ }
            Chip(f.nextWeek, v == today + 7, Modifier.weight(1.2f)) { v = today + 7; key++ }
        }
        Spacer(Modifier.height(8.dp))
        val start = minOf(today, initial ?: today) - 0
        val sel = ((v ?: today) - start).toInt().coerceIn(0, 729)
        val a by animateFloatAsState(if (v == null) 0.35f else 1f, label = "a")
        androidx.compose.runtime.key(key) {
            var first by remember { mutableStateOf(true) }
            WheelPicker(730, sel, { if (first) first = false else v = start + it }, { i ->
                when (val d = start + i) { today -> f.todaySec; today + 1 -> f.tomorrow; else -> s.dateLong(d) }
            }, Modifier.graphicsLayer { alpha = a }, width = 300.dp, cyclic = false)
        }
        Spacer(Modifier.height(8.dp))
    }
}
