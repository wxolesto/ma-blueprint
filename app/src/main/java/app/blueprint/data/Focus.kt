package app.blueprint.data

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class SubTask(val id: String, val title: String, val done: Boolean = false)

@Serializable
data class Todo(
    val id: String,
    val title: String,
    val notes: String = "",
    val done: Boolean = false,
    /** 0 none, 1 low, 2 medium, 3 high */
    val priority: Int = 0,
    val dueDay: Long? = null,
    /** estimated pomodoros, 0 = none */
    val estimate: Int = 0,
    val color: Int = 1,
    val subtasks: List<SubTask> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val doneAt: Long? = null,
)

@Serializable
enum class Phase { Focus, Short, Long }

@Serializable
data class FocusSession(
    val id: String,
    val start: Long,
    val end: Long,
    val phase: Phase,
    val todoId: String? = null,
    val label: String = "",
    /** true when the phase ran to the end (false = paused / stopped / skipped part) */
    val completed: Boolean = false,
) {
    val minutes get() = ((end - start) / 60000.0)
    val day get() = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
}

@Serializable
data class PomoSettings(
    val focusMin: Int = 25,
    val shortMin: Int = 5,
    val longMin: Int = 15,
    val longEvery: Int = 4,
    val autoBreaks: Boolean = false,
    val autoFocus: Boolean = false,
    val dailyGoal: Int = 8,
    val notify: Boolean = true,
    val sound: Boolean = true,
    val vibrate: Boolean = true,
    val ongoing: Boolean = true,
    val keepScreenOn: Boolean = true,
    val logBreaks: Boolean = true,
    val minLogMin: Int = 1,
    val autoCompleteTask: Boolean = false,
    val lastSecondsHaptic: Boolean = true,
)

@Serializable
data class TimerState(
    val phase: Phase = Phase.Focus,
    val running: Boolean = false,
    /** wall clock end time while running */
    val endAt: Long = 0L,
    /** remaining ms while paused, 0 = full duration */
    val remainingMs: Long = 0L,
    /** start of the current uninterrupted segment */
    val segStart: Long = 0L,
    /** focus sessions finished in the current set */
    val cycle: Int = 0,
    val todoId: String? = null,
    val label: String = "",
)

fun PomoSettings.minutesFor(p: Phase) = when (p) { Phase.Focus -> focusMin; Phase.Short -> shortMin; Phase.Long -> longMin }
fun PomoSettings.durationMs(p: Phase) = minutesFor(p) * 60_000L

fun TimerState.remaining(now: Long, ps: PomoSettings): Long = when {
    running -> (endAt - now).coerceAtLeast(0)
    remainingMs > 0 -> remainingMs
    else -> ps.durationMs(phase)
}

fun AppData.subjectTitle(todoId: String?, label: String): String =
    todoId?.let { id -> todos.firstOrNull { it.id == id }?.title } ?: label

fun AppData.pomosFor(todoId: String) = sessions.count { it.todoId == todoId && it.phase == Phase.Focus && it.completed }
fun AppData.focusMsFor(todoId: String) = sessions.filter { it.todoId == todoId && it.phase == Phase.Focus }.sumOf { it.end - it.start }
fun AppData.sessionsOn(day: Long) = sessions.filter { it.day == day }.sortedBy { it.start }
fun AppData.pomosOn(day: Long) = sessions.count { it.day == day && it.phase == Phase.Focus && it.completed }
fun AppData.focusMsOn(day: Long) = sessions.filter { it.day == day && it.phase == Phase.Focus }.sumOf { it.end - it.start }

fun epochDayOf(ms: Long): Long = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
fun minuteOfDay(ms: Long): Int = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalTime().let { it.hour * 60 + it.minute }

// ---------- todo operations ----------
fun Repository.addTodo(t: Todo) = update { it.copy(todos = listOf(t) + it.todos) }
fun Repository.upsertTodo(t: Todo) = update { d ->
    if (d.todos.any { it.id == t.id }) d.copy(todos = d.todos.map { if (it.id == t.id) t else it }) else d.copy(todos = listOf(t) + d.todos)
}
fun Repository.toggleTodo(id: String) = update { d ->
    d.copy(todos = d.todos.map { if (it.id == id) it.copy(done = !it.done, doneAt = if (!it.done) System.currentTimeMillis() else null) else it })
}
fun Repository.deleteTodo(id: String) = update { d ->
    d.copy(todos = d.todos.filterNot { it.id == id }, timer = if (d.timer.todoId == id) d.timer.copy(todoId = null, label = d.todos.firstOrNull { it.id == id }?.title ?: "") else d.timer)
}
fun Repository.clearCompleted() = update { d -> d.copy(todos = d.todos.filterNot { it.done }) }
fun Repository.deleteSession(id: String) = update { d -> d.copy(sessions = d.sessions.filterNot { it.id == id }) }
fun Repository.updateSession(s: FocusSession) = update { d -> d.copy(sessions = d.sessions.map { if (it.id == s.id) s else it }) }
fun Repository.pomo(block: (PomoSettings) -> PomoSettings) = update { it.copy(pomo = block(it.pomo)) }

fun todayEpoch(): Long = LocalDate.now().toEpochDay()
