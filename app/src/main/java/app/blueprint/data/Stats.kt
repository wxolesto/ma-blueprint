package app.blueprint.data

import java.time.LocalDate

fun Habit.isDueOn(day: Long): Boolean =
    frequency == Frequency.Scheduled && LocalDate.ofEpochDay(day).dayOfWeek.value in days

/** 0-based week index within the blueprint */
fun Blueprint.weekIndex(day: Long): Int = ((day - startDay).coerceAtLeast(0) / 7).toInt()
fun Blueprint.weekRange(week: Int): LongRange {
    val s = startDay + week * 7L
    return s..minOf(s + 6, endDay)
}
fun Blueprint.dayNumber(day: Long): Int = (day - startDay + 1).toInt()
fun Blueprint.totalWeeks(): Int = (lengthDays + 6) / 7

class DayStat(val done: Float, val total: Int) {
    val rate: Float? get() = if (total == 0) null else done / total
    val perfect get() = total > 0 && done >= total
}

fun AppData.dayStat(bp: Blueprint, day: Long): DayStat {
    var done = 0f
    var total = 0
    for (h in bp.habits) if (h.isDueOn(day)) {
        total++
        done += (count(h.id, day).coerceAtMost(h.timesPerDay).toFloat() / h.timesPerDay)
    }
    return DayStat(done, total)
}

fun AppData.weekCount(bp: Blueprint, h: Habit, week: Int): Int =
    bp.weekRange(week).count { count(h.id, it) > 0 }

data class Summary(
    val currentStreak: Int,
    val bestStreak: Int,
    val perfectDays: Int,
    val avgCompletion: Float,
    val totalCheckIns: Int,
    val elapsedDays: Int,
)

fun AppData.summary(bp: Blueprint, today: Long): Summary {
    val last = minOf(today, bp.endDay)
    var best = 0
    var run = 0
    var perfect = 0
    var sum = 0f
    var n = 0
    var checkIns = 0
    var d = bp.startDay
    while (d <= last) {
        val s = dayStat(bp, d)
        if (s.total > 0) {
            val r = s.rate!!
            if (d < today || r > 0f) { sum += r; n++ }
            if (s.perfect) { perfect++; run++; best = maxOf(best, run) } else if (d < today) run = 0
        }
        for (h in bp.habits) checkIns += count(h.id, d)
        d++
    }
    // current streak counting back from today (today counts only if perfect)
    var cur = 0
    d = last
    if (d >= bp.startDay && !dayStat(bp, d).perfect) d--
    while (d >= bp.startDay) {
        val s = dayStat(bp, d)
        if (s.total == 0) { d--; continue }
        if (!s.perfect) break
        cur++; d--
    }
    return Summary(cur, best, perfect, if (n == 0) 0f else sum / n, checkIns, (last - bp.startDay + 1).coerceAtLeast(0).toInt())
}

data class HabitSummary(val rate: Float, val streak: Int, val best: Int, val total: Int)

fun AppData.habitSummary(bp: Blueprint, h: Habit, today: Long): HabitSummary {
    val last = minOf(today, bp.endDay)
    if (h.frequency == Frequency.PerWeek) {
        val weeks = (0..bp.weekIndex(last))
        var hit = 0; var run = 0; var best = 0; var cur = 0
        for (w in weeks) {
            val ok = weekCount(bp, h, w) >= h.perWeek
            if (ok) { hit++; run++; best = maxOf(best, run) } else if (w < bp.weekIndex(last)) run = 0
        }
        cur = run
        val total = (bp.startDay..last).sumOf { count(h.id, it) }
        return HabitSummary(if (weeks.count() == 0) 0f else hit.toFloat() / weeks.count(), cur, best, total)
    }
    var due = 0; var done = 0f; var run = 0; var best = 0; var total = 0
    var d = bp.startDay
    while (d <= last) {
        val c = count(h.id, d); total += c
        if (h.isDueOn(d)) {
            due++
            done += c.coerceAtMost(h.timesPerDay).toFloat() / h.timesPerDay
            if (c >= h.timesPerDay) { run++; best = maxOf(best, run) } else if (d < today) run = 0
        }
        d++
    }
    return HabitSummary(if (due == 0) 0f else done / due, run, best, total)
}
