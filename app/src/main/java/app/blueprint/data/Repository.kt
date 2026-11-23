package app.blueprint.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.util.UUID

object Repository {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeLock = Mutex()
    private lateinit var file: File
    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()
    var onChanged: ((Context) -> Unit)? = null
    private lateinit var appContext: Context

    @Synchronized
    fun init(context: Context) {
        if (::file.isInitialized) return
        appContext = context.applicationContext
        file = File(appContext.filesDir, "blueprint.json")
        _data.value = runCatching { json.decodeFromString<AppData>(file.readText()) }.getOrElse {
            runCatching { json.decodeFromString<AppData>(File(file.path + ".bak").readText()) }.getOrDefault(AppData())
        }
    }

    fun update(block: (AppData) -> AppData) {
        _data.update(block)
        val snapshot = _data.value
        scope.launch {
            writeLock.withLock {
                val tmp = File(file.path + ".tmp")
                tmp.writeText(json.encodeToString(AppData.serializer(), snapshot))
                if (file.exists()) file.copyTo(File(file.path + ".bak"), overwrite = true)
                tmp.renameTo(file)
            }
            onChanged?.invoke(appContext)
        }
    }

    fun exportJson(): String = Json { prettyPrint = true; encodeDefaults = true }.encodeToString(AppData.serializer(), _data.value)

    fun importJson(text: String): Boolean = runCatching {
        val d = json.decodeFromString<AppData>(text)
        update { d }
    }.isSuccess

    // ---- operations ----
    fun newId() = UUID.randomUUID().toString().take(12)

    fun today(): Long = LocalDate.now().toEpochDay()

    fun setCount(habitId: String, day: Long, count: Int) = update { d ->
        val m = d.completions[habitId].orEmpty().toMutableMap()
        if (count <= 0) m.remove(day) else m[day] = count
        d.copy(completions = d.completions + (habitId to m))
    }

    /** tap behaviour: increments up to timesPerDay, then resets to 0 */
    fun tap(habit: Habit, day: Long): Int {
        val cur = _data.value.count(habit.id, day)
        val next = if (cur >= habit.timesPerDay) 0 else cur + 1
        setCount(habit.id, day, next)
        return next
    }

    fun createBlueprint(name: String, start: Long, length: Int, habits: List<Habit>) = update { d ->
        val bp = Blueprint(newId(), name, start, length, habits)
        val old = d.blueprints.map { if (it.id == d.activeId) it.copy(finished = true) else it }
        d.copy(blueprints = old + bp, activeId = bp.id)
    }

    fun updateActive(block: (Blueprint) -> Blueprint) = update { d ->
        d.copy(blueprints = d.blueprints.map { if (it.id == d.activeId) block(it) else it })
    }

    fun upsertHabit(h: Habit) = updateActive { bp ->
        if (bp.habits.any { it.id == h.id }) bp.copy(habits = bp.habits.map { if (it.id == h.id) h else it })
        else bp.copy(habits = bp.habits + h)
    }

    fun removeHabit(id: String) = updateActive { bp -> bp.copy(habits = bp.habits.filterNot { it.id == id }) }

    fun moveHabit(from: Int, to: Int) = updateActive { bp ->
        val l = bp.habits.toMutableList()
        if (from in l.indices && to in l.indices) l.add(to, l.removeAt(from))
        bp.copy(habits = l)
    }

    fun setNote(day: Long, text: String) = update { d ->
        d.copy(notes = if (text.isBlank()) d.notes - day else d.notes + (day to text))
    }

    fun settings(block: (Settings) -> Settings) = update { it.copy(settings = block(it.settings)) }

    fun activate(id: String) = update { it.copy(activeId = id) }

    fun deleteBlueprint(id: String) = update { d ->
        val bps = d.blueprints.filterNot { it.id == id }
        d.copy(blueprints = bps, activeId = if (d.activeId == id) bps.lastOrNull()?.id else d.activeId)
    }
}
