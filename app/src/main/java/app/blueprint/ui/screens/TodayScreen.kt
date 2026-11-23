package app.blueprint.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Blueprint
import app.blueprint.data.Frequency
import app.blueprint.data.Habit
import app.blueprint.data.Repository
import app.blueprint.data.dayNumber
import app.blueprint.data.dayStat
import app.blueprint.data.isDueOn
import app.blueprint.data.weekCount
import app.blueprint.data.weekIndex
import app.blueprint.data.weekRange
import app.blueprint.data.summary
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.CheckCircle
import app.blueprint.ui.components.Dot
import app.blueprint.ui.components.HabitIcons
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.IconTile
import app.blueprint.ui.components.PrimaryButton
import app.blueprint.ui.components.ProgressBar
import app.blueprint.ui.components.ProgressRing
import app.blueprint.ui.components.T
import app.blueprint.ui.components.pressable
import app.blueprint.ui.glass.GlassIconButton
import app.blueprint.ui.glass.GlassTextButton
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.nav.Route
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(data: AppData, bp: Blueprint, today: Long, onCelebrate: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val h = Theme.haptics
    val nav = LocalNav.current
    var selected by rememberSaveable(today) { mutableLongStateOf(today.coerceIn(bp.startDay, maxOf(bp.startDay, minOf(today, bp.endDay)))) }
    val isToday = selected == today
    val dayNum = bp.dayNumber(selected)
    val scheduled = bp.habits.filter { it.frequency == Frequency.Scheduled && it.isDueOn(selected) }
    val weekly = bp.habits.filter { it.frequency == Frequency.PerWeek }
    val stat = data.dayStat(bp, selected)
    val doneCount = scheduled.count { data.count(it.id, selected) >= it.timesPerDay }
    val canEdit = selected <= today && selected >= bp.startDay

    // celebration when today's list becomes complete
    var wasPerfect by remember(selected) { mutableStateOf(stat.perfect) }
    LaunchedEffect(stat.perfect, selected) {
        if (stat.perfect && !wasPerfect && isToday) { h.confirm(); onCelebrate() }
        wasPerfect = stat.perfect
    }

    LargeTitleScreen(
        title = s.appName,
        subtitle = s.dateLong(today),
        actions = { b ->
            if (!isToday && today in bp.startDay..bp.endDay) GlassIconButton(b, Icons.Rounded.Today, { selected = today })
            GlassIconButton(b, Icons.Rounded.Add, {
                nav.push(Route.EditHabit(null, true, onSave = { Repository.upsertHabit(it) }))
            })
        },
    ) { _ ->
        item(key = "week") { WeekStrip(data, bp, today, selected) { h.tick(); selected = it } }

        if (today < bp.startDay) {
            item(key = "notstarted") {
                Card(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        T(s.notStarted, t.title3)
                        Spacer(Modifier.height(4.dp))
                        T(s.startsIn((bp.startDay - today).toInt()), t.subhead, color = c.label2)
                    }
                }
            }
        }

        item(key = "header") {
            Card(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(Modifier.padding(20.dp)) {
                    val elapsed = (bp.dayNumber(minOf(today, bp.endDay))).coerceIn(0, bp.lengthDays)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            T(bp.name, t.footnote.copy(fontWeight = FontWeight.SemiBold), color = c.blue, maxLines = 1)
                            Spacer(Modifier.height(2.dp))
                            T(s.dayOf(dayNum.coerceIn(1, bp.lengthDays), bp.lengthDays), t.title2)
                            T(s.daysRemaining((bp.lengthDays - elapsed).coerceAtLeast(0)), t.subhead, color = c.label2)
                        }
                        T(s.percent(elapsed.toFloat() / bp.lengthDays), t.title3, color = c.label2)
                    }
                    Spacer(Modifier.height(14.dp))
                    ProgressBar(elapsed.toFloat() / bp.lengthDays, c.blue, height = 8.dp)
                }
            }
        }

        if (today > bp.endDay) item(key = "complete") { CompleteCard(data, bp, today) }

        if (data.settings.quotes) checkpointFor(bp, dayNum)?.let { msg ->
            item(key = "checkpoint") { CheckpointCard(msg, bp.lengthDays) }
        }

        item(key = "habits_h") {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                T(if (isToday) s.todaysHabits else s.dateLong(selected), t.title3, Modifier.weight(1f))
                if (scheduled.isNotEmpty()) T("${s.num(doneCount)}/${s.num(scheduled.size)}", t.headline, color = if (doneCount == scheduled.size) c.green else c.label2)
            }
        }
        if (scheduled.isEmpty()) item(key = "empty") {
            Card(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                T(s.nothingToday, t.subhead, Modifier.padding(20.dp), color = c.label2)
            }
        }
        items(scheduled, key = { "h_" + it.id }) { habit ->
            HabitRow(
                habit = habit,
                count = data.count(habit.id, selected),
                enabled = canEdit,
                subtitle = habit.description.ifBlank { if (habit.timesPerDay > 1) s.timesADay(habit.timesPerDay) else s.freqLabel(habit.isDaily, false, 0, habit.days.size) },
                modifier = Modifier.animateItem(fadeInSpec = spring(stiffness = 400f), placementSpec = spring(0.85f, 400f)),
                onTap = {
                    val next = Repository.tap(habit, selected)
                    if (next >= habit.timesPerDay) h.confirm() else if (next == 0) h.light() else h.tick()
                },
                onLong = { h.heavy(); editHabit(nav, habit) },
            )
        }
        item(key = "allDone") {
            AnimatedVisibility(stat.perfect && isToday, enter = fadeIn(spring(stiffness = 300f)) + expandVertically(spring(0.85f, 300f)), exit = fadeOut() + shrinkVertically()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(18.dp), tint = c.yellow)
                    Spacer(Modifier.width(8.dp))
                    Column { T(s.allDone, t.headline); T(s.allDoneSub, t.footnote, color = c.label2) }
                }
            }
        }

        if (weekly.isNotEmpty()) {
            val week = bp.weekIndex(selected)
            item(key = "weekly_h") {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    T(s.weekN(week + 1), t.title3, Modifier.weight(1f))
                    val r = bp.weekRange(week)
                    T("${s.dateShort(r.first)} – ${s.dateShort(r.last)}", t.subhead, color = c.label2)
                }
            }
            items(weekly, key = { "w_" + it.id }) { habit ->
                val wc = data.weekCount(bp, habit, week)
                WeeklyRow(habit, wc, data.count(habit.id, selected) > 0, canEdit,
                    onTap = {
                        val cur = data.count(habit.id, selected)
                        Repository.setCount(habit.id, selected, if (cur > 0) 0 else 1)
                        if (cur == 0) h.confirm() else h.light()
                    },
                    onLong = { h.heavy(); editHabit(nav, habit) })
            }
        }

        item(key = "note") { NoteCard(data, selected) }

        if (data.settings.quotes) item(key = "quote") {
            val q = s.quotesList[(selected % s.quotesList.size).toInt().let { if (it < 0) -it else it }]
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)) {
                Icon(Icons.Rounded.FormatQuote, null, Modifier.size(20.dp), tint = c.label3)
                Spacer(Modifier.width(8.dp))
                T(q, t.subhead, color = c.label2)
            }
        }
    }
}

fun editHabit(nav: app.blueprint.ui.nav.Navigator, habit: Habit) {
    nav.push(Route.EditHabit(habit, false, onSave = { Repository.upsertHabit(it) }, onDelete = { Repository.removeHabit(habit.id) }))
}

private fun checkpointFor(bp: Blueprint, day: Int): Int? {
    val n = bp.lengthDays
    val points = setOf(1, 7, 14, 21, (n + 1) / 2, n - 6, n)
    return if (day in 1..n && day in points) day else null
}

@Composable
private fun CheckpointCard(day: Int, total: Int) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    Box(
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().clip(RoundedRectangle(26.dp))
            .background(Brush.linearGradient(listOf(c.blue.copy(alpha = 0.28f), Color(0xFFBF5AF2).copy(alpha = 0.22f))))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedRectangle(26.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Rounded.Flag, c.blue, 40.dp, 12.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                T(s.checkpoint, t.caption.copy(fontWeight = FontWeight.SemiBold), color = c.blue)
                Spacer(Modifier.height(2.dp))
                T(s.checkpointMsg(day, total), t.subhead, color = c.label)
            }
        }
    }
}

@Composable
fun WeekStrip(data: AppData, bp: Blueprint, today: Long, selected: Long, onSelect: (Long) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    // weeks aligned to locale week start
    fun weekStartOf(day: Long): Long {
        val dow = LocalDate.ofEpochDay(day).dayOfWeek.value
        val diff = (dow - s.weekStart + 7) % 7
        return day - diff
    }
    val first = weekStartOf(bp.startDay)
    val last = weekStartOf(bp.endDay)
    val count = ((last - first) / 7 + 1).toInt()
    val initial = ((weekStartOf(selected.coerceIn(bp.startDay, bp.endDay)) - first) / 7).toInt().coerceIn(0, count - 1)
    val pager = rememberPagerState(initialPage = initial) { count }
    val scope = rememberCoroutineScope()
    LaunchedEffect(selected) {
        val p = ((weekStartOf(selected) - first) / 7).toInt().coerceIn(0, count - 1)
        if (p != pager.currentPage) pager.animateScrollToPage(p, animationSpec = spring(0.9f, 300f))
    }
    HorizontalPager(pager, Modifier.fillMaxWidth().padding(vertical = 6.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp), pageSpacing = 12.dp) { page ->
        val ws = first + page * 7L
        Row(Modifier.fillMaxWidth()) {
            for (i in 0 until 7) {
                val d = ws + i
                val inRange = d in bp.startDay..bp.endDay
                val isSel = d == selected
                val isTd = d == today
                val st = if (inRange && d <= today) data.dayStat(bp, d) else null
                val bg by animateColorAsState(if (isSel) c.card else Color.Transparent, spring(1f, 500f), label = "sel")
                val scale by animateFloatAsState(if (isSel) 1f else 0.94f, spring(0.6f, 400f), label = "scale")
                Column(
                    Modifier.weight(1f).padding(horizontal = 2.dp).clip(RoundedRectangle(18.dp)).background(bg)
                        .pressable(0.9f, enabled = inRange) { onSelect(d) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    T(s.weekdayShort(LocalDate.ofEpochDay(d).dayOfWeek.value), t.caption.copy(fontWeight = FontWeight.Medium), color = if (isTd) c.blue else c.label2)
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.size(38.dp).graphicsLayer { scaleX = scale; scaleY = scale }, contentAlignment = Alignment.Center) {
                        if (inRange) ProgressRing(st?.rate, if (st?.perfect == true) c.green else c.blue, Modifier.size(38.dp), 3.2.dp, track = c.fill.copy(alpha = if (d > today) 0.25f else 0.6f))
                        T(s.dayOfMonth(d), t.subhead.copy(fontWeight = if (isTd || isSel) FontWeight.Bold else FontWeight.Medium), color = if (!inRange) c.label3 else if (isTd) c.blue else c.label)
                    }
                }
            }
        }
    }
}

@Composable
fun HabitRow(habit: Habit, count: Int, enabled: Boolean, subtitle: String, modifier: Modifier = Modifier, onTap: () -> Unit, onLong: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val color = c.habit(habit.color)
    val done = count >= habit.timesPerDay
    val tint by animateFloatAsState(if (done) 1f else 0f, spring(1f, 260f), label = "tint")
    Row(
        modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth()
            .pressable(0.975f, 0.9f, onLongPress = onLong) { if (enabled) onTap() }
            .clip(RoundedRectangle(22.dp))
            .background(lerp(c.card, color.copy(alpha = 1f), tint * 0.16f).let { if (c.isDark) it else lerp(c.card, color, tint * 0.12f) })
            .heightIn(min = 72.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(HabitIcons.get(habit.icon), color, 44.dp, 13.dp, 24.dp, filled = false)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            T(habit.name, t.headline.copy(textDecoration = if (done) TextDecoration.LineThrough else null), color = lerp(c.label, c.label2, tint), maxLines = 1)
            if (subtitle.isNotBlank()) T(subtitle, t.footnote, color = c.label2, maxLines = 1)
        }
        if (habit.timesPerDay > 1) {
            T("${s.num(count.coerceAtMost(habit.timesPerDay))}/${s.num(habit.timesPerDay)}", t.footnote.copy(fontWeight = FontWeight.SemiBold), color = if (done) color else c.label2)
            Spacer(Modifier.width(10.dp))
        }
        CheckCircle(count, habit.timesPerDay, color, Modifier.graphicsLayer { alpha = if (enabled) 1f else 0.4f }, size = 32.dp)
    }
}

@Composable
private fun WeeklyRow(habit: Habit, weekCount: Int, doneToday: Boolean, enabled: Boolean, onTap: () -> Unit, onLong: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val color = c.habit(habit.color)
    val met = weekCount >= habit.perWeek
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth()
            .pressable(0.975f, 0.9f, onLongPress = onLong) { if (enabled) onTap() }
            .clip(RoundedRectangle(22.dp)).background(if (met) lerp(c.card, color, 0.14f) else c.card)
            .heightIn(min = 72.dp).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(HabitIcons.get(habit.icon), color, 44.dp, 13.dp, 24.dp, filled = false)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            T(habit.name, t.headline, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 0 until habit.perWeek) {
                    val on = i < weekCount
                    val sc by animateFloatAsState(if (on) 1f else 0.75f, spring(0.5f, 500f), label = "dot")
                    Box(Modifier.padding(end = 4.dp).size(9.dp).graphicsLayer { scaleX = sc; scaleY = sc }.clip(CircleShape).background(if (on) color else c.fill))
                }
                Spacer(Modifier.width(6.dp))
                T(s.thisWeek(weekCount, habit.perWeek), t.footnote, color = if (met) color else c.label2)
            }
        }
        CheckCircle(if (doneToday) 1 else 0, 1, color, size = 32.dp)
    }
}

@Composable
private fun NoteCard(data: AppData, day: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val overlay = LocalOverlay.current
    val note = data.notes[day].orEmpty()
    Card(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            Modifier.fillMaxWidth().pressable(0.98f) {
                overlay.showSheet { dismiss -> NoteSheet(day, note, dismiss) }
            }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(Icons.Rounded.EditNote, c.orange, 36.dp, 10.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T(s.note, t.headline)
                T(note.ifBlank { s.noteHint }, t.footnote, color = c.label2, maxLines = 2)
            }
        }
    }
}

@Composable
private fun NoteSheet(day: Long, initial: String, dismiss: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    var text by remember { mutableStateOf(initial) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(350); runCatching { focus.requestFocus() } }
    Column(Modifier.padding(horizontal = 20.dp).padding(top = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                T(s.note, t.title3)
                T(s.dateLong(day), t.footnote, color = c.label2)
            }
            Box(Modifier.clip(Capsule()).background(c.blue).pressable(0.94f) { Repository.setNote(day, text.trim()); dismiss() }.padding(horizontal = 18.dp, vertical = 9.dp)) {
                T(s.done, t.headline, color = Color.White)
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().heightIn(min = 160.dp).clip(RoundedRectangle(18.dp)).background(c.fill.copy(alpha = 0.25f)).padding(14.dp)) {
            if (text.isEmpty()) T(s.notePlaceholder, t.body, color = c.label3)
            BasicTextField(
                text, { text = it }, Modifier.fillMaxWidth().focusRequester(focus),
                textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(c.blue),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}


@Composable
private fun CompleteCard(data: AppData, bp: Blueprint, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val nav = LocalNav.current
    val sum = data.summary(bp, today)
    Card(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconTile(Icons.Rounded.EmojiEvents, c.yellow, 56.dp, 16.dp, 32.dp)
            Spacer(Modifier.height(12.dp))
            T(s.complete, t.title2)
            Spacer(Modifier.height(4.dp))
            T(s.completeMsg(sum.perfectDays, bp.lengthDays), t.subhead, color = c.label2)
            Spacer(Modifier.height(16.dp))
            PrimaryButton(s.startNew) { nav.push(Route.NewBlueprint) }
        }
    }
}
