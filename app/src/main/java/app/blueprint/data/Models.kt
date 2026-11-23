package app.blueprint.data

import kotlinx.serialization.Serializable

@Serializable
enum class Frequency { Scheduled, PerWeek }

@Serializable
data class Habit(
    val id: String,
    val name: String,
    val icon: String = "star",
    val color: Int = 0,
    val description: String = "",
    val frequency: Frequency = Frequency.Scheduled,
    /** ISO day of week 1 (Mon) .. 7 (Sun) */
    val days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val perWeek: Int = 3,
    val timesPerDay: Int = 1,
    /** minutes after midnight, null = no reminder */
    val reminder: Int? = null,
) {
    val isDaily get() = frequency == Frequency.Scheduled && days.size == 7
}

@Serializable
data class Blueprint(
    val id: String,
    val name: String,
    val startDay: Long,
    val lengthDays: Int,
    val habits: List<Habit>,
    val createdAt: Long = System.currentTimeMillis(),
    val finished: Boolean = false,
) {
    val endDay get() = startDay + lengthDays - 1
}

@Serializable
enum class ThemeMode { System, Light, Dark }

@Serializable
data class Settings(
    val theme: ThemeMode = ThemeMode.System,
    val language: String = "en",
    val haptics: Boolean = true,
    val quotes: Boolean = true,
    val celebrate: Boolean = true,
)

@Serializable
data class AppData(
    val version: Int = 1,
    val blueprints: List<Blueprint> = emptyList(),
    val activeId: String? = null,
    /** habitId -> (epochDay -> count) */
    val completions: Map<String, Map<Long, Int>> = emptyMap(),
    /** epochDay -> note */
    val notes: Map<Long, String> = emptyMap(),
    val settings: Settings = Settings(),
) {
    val active: Blueprint? get() = blueprints.firstOrNull { it.id == activeId }
    fun count(habitId: String, day: Long): Int = completions[habitId]?.get(day) ?: 0
}
