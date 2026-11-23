package app.blueprint.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Blueprint
import app.blueprint.data.Frequency
import app.blueprint.data.dayNumber
import app.blueprint.data.dayStat
import app.blueprint.data.habitSummary
import app.blueprint.data.isDueOn
import app.blueprint.data.summary
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.HabitIcons
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.IconTile
import app.blueprint.ui.components.ProgressRing
import app.blueprint.ui.components.T
import app.blueprint.ui.components.pressable
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.Route
import app.blueprint.ui.theme.Motion
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(data: AppData, bp: Blueprint, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val nav = LocalNav.current
    val sum = remember(data, bp, today) { data.summary(bp, today) }
    LargeTitleScreen(title = s.progress, subtitle = bp.name) { _ ->
        item(key = "stats") {
            val cards = listOf(
                Triple(Icons.Rounded.LocalFireDepartment, c.orange, s.dayStreak to sum.currentStreak.toFloat()),
                Triple(Icons.Rounded.EmojiEvents, c.yellow, s.bestStreak to sum.bestStreak.toFloat()),
                Triple(Icons.Rounded.Star, c.green, s.perfectDays to sum.perfectDays.toFloat()),
                Triple(Icons.Rounded.Insights, c.blue, s.avgCompletion to -sum.avgCompletion - 1f),
                Triple(Icons.Rounded.DoneAll, Color(0xFFBF5AF2), s.checkIns to sum.totalCheckIns.toFloat()),
                Triple(Icons.Rounded.CalendarMonth, Color(0xFF64D2FF), s.daysElapsed to sum.elapsedDays.toFloat()),
            )
            val pager = rememberPagerState { 3 }
            Column {
                HorizontalPager(pager, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp) { page ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (k in 0..1) {
                            val (icon, col, pair) = cards[page * 2 + k]
                            StatCard(icon, col, pair.first, pair.second, Modifier.weight(1f))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.Center) {
                    for (i in 0 until 3) {
                        val sel = pager.currentPage == i
                        val w by androidx.compose.animation.core.animateDpAsState(if (sel) 18.dp else 7.dp, Motion.snappy(), label = "dot")
                        Box(Modifier.padding(horizontal = 3.dp).height(7.dp).width(w).clip(CircleShape).background(if (sel) c.label else c.label3))
                    }
                }
            }
        }
        item(key = "grid") { DailyGrid(data, bp, today) }
        item(key = "chart") { RateChart(data, bp, today) }
        item(key = "heat") { Heatmap(data, bp, today) }
        item(key = "insights_h") {
            T(s.habitInsights, t.title3, Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp))
        }
        items(bp.habits, key = { "ins_" + it.id }) { h ->
            val hs = remember(data, h, today) { data.habitSummary(bp, h, today) }
            val col = c.habit(h.color)
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth().pressable(0.98f) { nav.push(Route.HabitDetail(h.id)) }
                    .clip(RoundedRectangle(22.dp)).background(c.card).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(HabitIcons.get(h.icon), col, 40.dp, 12.dp, 22.dp, filled = false)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    T(h.name, t.headline, maxLines = 1)
                    T("${s.streak} ${s.num(hs.streak)} · ${s.best} ${s.num(hs.best)} · ${s.total} ${s.num(hs.total)}", t.footnote, color = c.label2, maxLines = 1)
                }
                Box(contentAlignment = Alignment.Center) {
                    ProgressRing(hs.rate, col, Modifier.size(46.dp), 4.dp)
                    T(s.percent(hs.rate), t.caption2.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, label: String, value: Float, modifier: Modifier) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val isPercent = value < 0f
    val target = if (isPercent) ((-value - 1f) * 100).roundToInt() else value.roundToInt()
    val anim = remember { Animatable(0f) }
    LaunchedEffect(target) { anim.animateTo(target.toFloat(), tween(900, easing = Motion.iosEase)) }
    Column(modifier.clip(RoundedRectangle(26.dp)).background(c.card).padding(16.dp)) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(20.dp), tint = color)
        }
        Spacer(Modifier.height(14.dp))
        val v = anim.value.roundToInt()
        T(if (isPercent) s.percent(v / 100f) else s.num(v), t.largeTitle.copy(fontFamily = t.display))
        T(label, t.footnote.copy(fontWeight = FontWeight.Medium), color = c.label2, maxLines = 1)
    }
}

@Composable
private fun DailyGrid(data: AppData, bp: Blueprint, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val cell = 30.dp
    val gap = 6.dp
    LaunchedEffect(Unit) {
        val idx = (minOf(today, bp.endDay) - bp.startDay).toInt().coerceAtLeast(0)
        val px = with(density) { ((cell + gap) * idx).toPx() }
        scroll.scrollTo((px - with(density) { 140.dp.toPx() }).roundToInt().coerceAtLeast(0))
    }
    Card(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                T(s.dailyProgress, t.headline, Modifier.weight(1f))
                T("${s.dateShort(bp.startDay)} – ${s.dateShort(bp.endDay)}", t.footnote, color = c.label2)
            }
            Spacer(Modifier.height(12.dp))
            Row {
                Column(Modifier.padding(start = 16.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(gap)) {
                    Spacer(Modifier.height(18.dp))
                    bp.habits.forEach { h -> IconTile(HabitIcons.get(h.icon), c.habit(h.color), cell, 9.dp, 17.dp, filled = false) }
                }
                Row(Modifier.horizontalScroll(scroll).padding(end = 16.dp), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    for (i in 0 until bp.lengthDays) {
                        val d = bp.startDay + i
                        val isToday = d == today
                        Column(
                            Modifier.clip(RoundedRectangle(10.dp)).background(if (isToday) c.blue.copy(alpha = 0.12f) else Color.Transparent),
                            verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            T(s.num(i + 1), t.caption2.copy(fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium), Modifier.height(18.dp).padding(top = 2.dp), color = if (isToday) c.blue else c.label3)
                            bp.habits.forEach { h ->
                                val col = c.habit(h.color)
                                val cnt = data.count(h.id, d)
                                val due = h.frequency == Frequency.PerWeek || h.isDueOn(d)
                                val done = if (h.frequency == Frequency.PerWeek) cnt > 0 else cnt >= h.timesPerDay
                                val partial = !done && cnt > 0
                                Box(Modifier.size(cell).clip(RoundedRectangle(9.dp)).background(
                                    when {
                                        done -> col
                                        partial -> col.copy(alpha = 0.35f)
                                        d > today || !due -> c.fill.copy(alpha = 0.18f)
                                        d == today -> c.fill.copy(alpha = 0.35f)
                                        h.frequency == Frequency.PerWeek -> c.fill.copy(alpha = 0.3f)
                                        else -> c.red.copy(alpha = 0.16f)
                                    }
                                ), contentAlignment = Alignment.Center) {
                                    when {
                                        done -> Icon(Icons.Rounded.Check, null, Modifier.size(18.dp), tint = Color.White)
                                        d < today && due && h.frequency == Frequency.Scheduled && !partial -> Icon(Icons.Rounded.Close, null, Modifier.size(15.dp), tint = c.red.copy(alpha = 0.8f))
                                        else -> {}
                                    }
                                }
                            }
                            Spacer(Modifier.height(0.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RateChart(data: AppData, bp: Blueprint, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val h = Theme.haptics
    val last = minOf(today, bp.endDay)
    val points = remember(data, bp, today) {
        if (last < bp.startDay) emptyList() else (bp.startDay..last).map { (data.dayStat(bp, it).rate ?: 0f) }
    }
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(1100, easing = Motion.iosEase)) }
    var scrub by remember { mutableIntStateOf(-1) }
    val measurer = rememberTextMeasurer()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val labelStyle = t.caption2.copy(color = c.label3)
    val avg = if (points.isEmpty()) 0f else points.average().toFloat()
    Card(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    T(s.completionRate, t.headline)
                    val sel = scrub.takeIf { it in points.indices }
                    T(
                        if (sel != null) "${s.dayOf(sel + 1, bp.lengthDays)} · ${s.percent(points[sel])}" else "${s.avgCompletion} ${s.percent(avg)}",
                        t.footnote, color = c.label2
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Canvas(
                Modifier.fillMaxWidth().height(190.dp)
                    .pointerInput(points.size) {
                        fun idx(x: Float): Int {
                            val left = 34.dp.toPx()
                            val w = size.width - left
                            val xx = if (rtl) size.width - x else x
                            val n = (points.size - 1).coerceAtLeast(1)
                            return (((xx - left) / w) * n).roundToInt().coerceIn(0, points.size - 1)
                        }
                        detectHorizontalDragGestures(
                            onDragStart = { o -> scrub = idx(o.x); h.tick() },
                            onDragEnd = { scrub = -1 }, onDragCancel = { scrub = -1 },
                        ) { ch, _ -> val i = idx(ch.position.x); if (i != scrub) { scrub = i; h.tick() } }
                    }
            ) {
                val left = 34.dp.toPx()
                val bottomPad = 18.dp.toPx()
                val w = size.width - left
                val hh = size.height - bottomPad
                fun xOf(i: Int): Float {
                    val n = (points.size - 1).coerceAtLeast(1)
                    val x = left + w * i / n
                    return if (rtl) size.width - x + 0f else x
                }
                fun yOf(v: Float) = hh - v * (hh - 6.dp.toPx()) - 2.dp.toPx()
                // grid
                for (g in listOf(0f, 0.5f, 1f)) {
                    val y = yOf(g)
                    drawLine(c.separator, Offset(if (rtl) 0f else left, y), Offset(if (rtl) size.width - left else size.width, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    val lbl = measurer.measure(s.percent(g), labelStyle)
                    drawText(lbl, topLeft = Offset(if (rtl) size.width - lbl.size.width else 0f, y - lbl.size.height / 2))
                }
                if (points.isEmpty()) return@Canvas
                val line = Path()
                points.forEachIndexed { i, v ->
                    val x = xOf(i); val y = yOf(v)
                    if (i == 0) line.moveTo(x, y) else {
                        val px = xOf(i - 1); val py = yOf(points[i - 1])
                        val cx = (px + x) / 2
                        line.cubicTo(cx, py, cx, y, x, y)
                    }
                }
                val fill = Path().apply { addPath(line); lineTo(xOf(points.size - 1), hh); lineTo(xOf(0), hh); close() }
                val revealX = if (rtl) size.width - (left + w * reveal.value) else left + w * reveal.value
                clipRect(left = if (rtl) revealX else 0f, right = if (rtl) size.width else revealX) {
                    drawPath(fill, Brush.verticalGradient(listOf(c.blue.copy(alpha = 0.38f), c.blue.copy(alpha = 0f)), startY = 0f, endY = hh))
                    drawPath(line, Brush.horizontalGradient(listOf(c.blue, Color(0xFFBF5AF2))), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                }
                // last point marker
                if (reveal.value > 0.98f) {
                    val lx = xOf(points.size - 1); val ly = yOf(points.last())
                    drawCircle(c.card, 7.dp.toPx(), Offset(lx, ly)); drawCircle(Color(0xFFBF5AF2), 4.5.dp.toPx(), Offset(lx, ly))
                }
                if (scrub in points.indices) {
                    val sx = xOf(scrub); val sy = yOf(points[scrub])
                    drawLine(c.label3, Offset(sx, 0f), Offset(sx, hh), 1.5f)
                    drawCircle(c.card, 8.dp.toPx(), Offset(sx, sy)); drawCircle(c.blue, 5.dp.toPx(), Offset(sx, sy))
                }
                // x labels
                val step = maxOf(1, points.size / 5)
                for (i in points.indices step step) {
                    val lbl = measurer.measure(s.num(i + 1), labelStyle)
                    drawText(lbl, topLeft = Offset(xOf(i) - lbl.size.width / 2, size.height - lbl.size.height))
                }
            }
        }
    }
}

@Composable
fun Heatmap(data: AppData, bp: Blueprint, today: Long, habitId: String? = null, color: Color? = null) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val col = color ?: c.green
    val habit = bp.habits.firstOrNull { it.id == habitId }
    val weeks = (bp.lengthDays + 6) / 7
    Card(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Column(Modifier.padding(16.dp)) {
            T(s.heatmap, t.headline)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                for (w in 0 until weeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (dd in 0 until 7) {
                            val d = bp.startDay + w * 7 + dd
                            val rate: Float? = when {
                                d > bp.endDay -> null
                                d > today -> -1f
                                habit != null -> if (habit.frequency == Frequency.PerWeek) (if (data.count(habit.id, d) > 0) 1f else 0f)
                                    else if (!habit.isDueOn(d)) -1f else (data.count(habit.id, d).toFloat() / habit.timesPerDay).coerceAtMost(1f)
                                else -> data.dayStat(bp, d).rate ?: -1f
                            }
                            Box(Modifier.size(16.dp).clip(RoundedRectangle(4.5.dp)).background(
                                when {
                                    rate == null -> Color.Transparent
                                    rate < 0f -> c.fill.copy(alpha = 0.18f)
                                    rate == 0f -> c.fill.copy(alpha = 0.4f)
                                    else -> lerp(col.copy(alpha = 0.25f), col, rate)
                                }
                            ))
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                T(s.less, t.caption2, color = c.label3)
                Spacer(Modifier.width(6.dp))
                for (k in 0..4) {
                    Box(Modifier.padding(horizontal = 2.dp).size(11.dp).clip(RoundedRectangle(3.dp)).background(if (k == 0) c.fill.copy(alpha = 0.4f) else lerp(col.copy(alpha = 0.25f), col, k / 4f)))
                }
                Spacer(Modifier.width(6.dp))
                T(s.more, t.caption2, color = c.label3)
            }
        }
    }
}
