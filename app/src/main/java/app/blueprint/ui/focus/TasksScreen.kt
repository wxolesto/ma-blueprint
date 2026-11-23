package app.blueprint.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Phase
import app.blueprint.data.Repository
import app.blueprint.data.Todo
import app.blueprint.data.addTodo
import app.blueprint.data.clearCompleted
import app.blueprint.data.epochDayOf
import app.blueprint.data.pomosFor
import app.blueprint.data.toggleTodo
import app.blueprint.focus.Pomodoro
import app.blueprint.i18n.f
import app.blueprint.ui.components.CheckCircle
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.T
import app.blueprint.ui.components.pressable
import app.blueprint.ui.glass.GlassIconButton
import app.blueprint.ui.glass.SegmentedControl
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.nav.Route
import app.blueprint.ui.screens.LargeTitleScreen
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun priorityColor(p: Int): Color = when (p) { 3 -> Theme.colors.red; 2 -> Theme.colors.orange; 1 -> Theme.colors.blue; else -> Theme.colors.label3 }

fun startFocusOn(ctx: android.content.Context, todo: Todo, tabs: (Int) -> Unit) {
    val d = Repository.data.value
    Pomodoro.setSubject(ctx, todo.id, "")
    if (!d.timer.running) {
        if (d.timer.phase != Phase.Focus && d.timer.remainingMs == 0L) Pomodoro.setPhase(ctx, Phase.Focus)
        Pomodoro.start(ctx)
    }
    tabs(2)
}

@Composable
fun TasksScreen(data: AppData, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    val ctx = LocalContext.current
    val tabs = LocalTabSelect.current
    val hap = Theme.haptics
    val scope = rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf(0) }
    // tasks that were just checked stay in place briefly (like iOS Reminders)
    val checking = remember { mutableStateListOf<String>() }

    fun onCheck(td: Todo) {
        if (td.done) { hap.tick(); Repository.toggleTodo(td.id); return }
        if (td.id in checking) { checking.remove(td.id); return }
        hap.confirm()
        checking.add(td.id)
        scope.launch {
            delay(900)
            if (td.id in checking) { checking.remove(td.id); Repository.toggleTodo(td.id) }
        }
    }

    val active = data.todos.filter { !it.done }
    val sorter = compareByDescending<Todo> { it.priority }.thenBy { it.dueDay ?: Long.MAX_VALUE }.thenByDescending { it.createdAt }
    val groups: List<Pair<String, List<Todo>>> = if (mode == 0) listOf(
        f.overdue to active.filter { (it.dueDay ?: Long.MAX_VALUE) < today },
        f.todaySec to active.filter { it.dueDay == today },
        f.tomorrow to active.filter { it.dueDay == today + 1 },
        f.upcoming to active.filter { (it.dueDay ?: -1) > today + 1 },
        f.someday to active.filter { it.dueDay == null },
    ).map { (k, v) -> k to v.sortedWith(sorter) }.filter { it.second.isNotEmpty() }
    else data.todos.filter { it.done }.sortedByDescending { it.doneAt ?: 0 }.groupBy { epochDayOf(it.doneAt ?: it.createdAt) }
        .map { (d, v) -> (if (d == today) f.todaySec else s.dateLong(d)) to v }

    LargeTitleScreen(
        title = f.tasks,
        subtitle = if (active.isEmpty()) null else "${f.n(active.size)} ${f.active}",
        actions = { b -> GlassIconButton(b, Icons.Rounded.Add, { nav.push(Route.EditTodo(null)) }) },
    ) { _ ->
        item(key = "mode") {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SegmentedControl(listOf(f.active, f.completed), mode, { mode = it })
            }
        }
        if (mode == 0) item(key = "quick") { QuickAdd(today) }
        if (groups.isEmpty()) {
            item(key = "empty") {
                if (mode == 0) EmptyState(f.noTasks, f.noTasksSub, Icons.Rounded.TaskAlt, c.blue)
                else EmptyState(f.noCompleted, "", Icons.Rounded.Checklist, c.green)
            }
        }
        groups.forEach { (title, list) ->
            item(key = "h_$title") {
                Header(title, trailing = { T(f.n(list.size), t.footnote, color = if (title == f.overdue) c.red else c.label2) })
            }
            list.forEachIndexed { i, td ->
                item(key = td.id) {
                    TodoRow(
                        data, td, today, checked = td.done || td.id in checking,
                        first = i == 0, last = i == list.lastIndex,
                        onCheck = { onCheck(td) },
                        onClick = { nav.push(Route.EditTodo(td.id)) },
                        onPlay = { hap.confirm(); startFocusOn(ctx, td, tabs) },
                    )
                }
            }
        }
        if (mode == 1 && groups.isNotEmpty()) item(key = "clear") {
            Box(Modifier.fillMaxWidth().padding(top = 18.dp), contentAlignment = Alignment.Center) {
                T(f.clearCompleted, t.body, Modifier.pressable(0.95f) {
                    overlay.alert(f.clearCompletedQ, null, listOf(AlertAction(s.cancel), AlertAction(f.delete, destructive = true) { Repository.clearCompleted() }))
                }.padding(12.dp), color = c.red)
            }
        }
    }
}

@Composable
private fun QuickAdd(today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val f = Theme.strings.f
    val hap = Theme.haptics
    var text by remember { mutableStateOf("") }
    fun add() {
        val v = text.trim()
        if (v.isEmpty()) return
        hap.confirm()
        Repository.addTodo(Todo(Repository.newId(), v, color = 1, dueDay = null))
        text = ""
    }
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().clip(com.kyant.shapes.RoundedRectangle(22.dp)).background(c.card).padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(c.blue), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Add, null, Modifier.size(18.dp), tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) T(f.newTask, t.body, color = c.label3)
            BasicTextField(
                text, { text = it.take(120) }, Modifier.fillMaxWidth(), singleLine = true,
                textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(c.blue),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { add() }),
            )
        }
        AnimatedVisibility(text.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.pressable(0.9f) { add() }.clip(com.kyant.shapes.Capsule()).background(c.blue).padding(horizontal = 12.dp, vertical = 6.dp)) {
                T(Theme.strings.add, t.subhead.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            }
        }
    }
}

@Composable
private fun LazyItemScope.TodoRow(
    data: AppData, td: Todo, today: Long, checked: Boolean, first: Boolean, last: Boolean,
    onCheck: () -> Unit, onClick: () -> Unit, onPlay: () -> Unit,
) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val r: Dp = 22.dp
    val shape = RoundedCornerShape(topStart = if (first) r else 0.dp, topEnd = if (first) r else 0.dp, bottomStart = if (last) r else 0.dp, bottomEnd = if (last) r else 0.dp)
    val color = c.habit(td.color)
    val fade by animateFloatAsState(if (checked && !td.done) 0.55f else 1f, spring(1f, 300f), label = "f")
    val current = data.timer.todoId == td.id
    Column(Modifier.animateItem(fadeInSpec = spring(1f, 400f), placementSpec = spring(0.85f, 380f), fadeOutSpec = spring(1f, 500f)).padding(horizontal = 16.dp).clip(shape).background(c.card)) {
        Row(
            Modifier.fillMaxWidth().pressable(0.99f, 0.7f, onClick = onClick).padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp).graphicsLayer { alpha = fade },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.pressable(0.85f) { onCheck() }) { CheckCircle(if (checked) 1 else 0, 1, color, size = 26.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (td.priority > 0) {
                        T("!".repeat(td.priority), t.body.copy(fontWeight = FontWeight.Bold), color = priorityColor(td.priority))
                        Spacer(Modifier.width(4.dp))
                    }
                    T(td.title, t.body.copy(textDecoration = if (checked) TextDecoration.LineThrough else null), color = if (checked) c.label2 else c.label, maxLines = 2)
                }
                val pom = data.pomosFor(td.id)
                val subDone = td.subtasks.count { it.done }
                val hasMeta = td.dueDay != null || pom > 0 || td.estimate > 0 || td.subtasks.isNotEmpty() || td.notes.isNotBlank()
                if (hasMeta) {
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        td.dueDay?.let { d ->
                            val late = d < today && !td.done
                            Meta(Icons.Rounded.CalendarToday, when (d) { today -> f.todaySec; today + 1 -> f.tomorrow; else -> s.dateShort(d) }, if (late) c.red else c.label2)
                        }
                        if (pom > 0 || td.estimate > 0) Meta(Icons.Rounded.Timer, if (td.estimate > 0) "${f.n(pom)}/${f.n(td.estimate)}" else f.n(pom), c.phase(Phase.Focus))
                        if (td.subtasks.isNotEmpty()) Meta(Icons.Rounded.Checklist, "${f.n(subDone)}/${f.n(td.subtasks.size)}", c.label2)
                        if (td.notes.isNotBlank()) Icon(Icons.Rounded.Notes, null, Modifier.size(14.dp), tint = c.label3)
                    }
                }
            }
            if (!td.done) {
                Spacer(Modifier.width(8.dp))
                val bg by animateColorAsState(if (current && data.timer.running) c.phase(Phase.Focus) else c.phase(Phase.Focus).copy(alpha = 0.14f), label = "pb")
                Box(Modifier.size(36.dp).pressable(0.85f) { onPlay() }.clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                    if (current && data.timer.running) LiveBars(Color.White)
                    else Icon(Icons.Rounded.PlayArrow, null, Modifier.size(22.dp), tint = c.phase(Phase.Focus))
                }
            }
        }
        if (!last) app.blueprint.ui.components.Divider(52.dp)
    }
}

@Composable
private fun Meta(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(13.dp), tint = color)
        Spacer(Modifier.width(3.dp))
        T(text, Theme.type.footnote, color = color, maxLines = 1)
    }
}

/** small animated equalizer shown on the task that is being focused on */
@Composable
fun LiveBars(color: Color) {
    val tr = androidx.compose.animation.core.rememberInfiniteTransition(label = "lb")
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(14.dp)) {
        listOf(0, 180, 360).forEach { d ->
            val h by tr.animateFloat(0.3f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(520, delayMillis = d), androidx.compose.animation.core.RepeatMode.Reverse), label = "b$d")
            Box(Modifier.width(3.dp).height((14 * h).dp).clip(com.kyant.shapes.Capsule()).background(color))
        }
    }
}
