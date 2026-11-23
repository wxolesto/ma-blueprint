package app.blueprint.ui.focus

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.blueprint.data.AppData
import app.blueprint.data.FocusSession
import app.blueprint.data.Phase
import app.blueprint.data.Todo
import app.blueprint.data.durationMs
import app.blueprint.data.minutesFor
import app.blueprint.data.epochDayOf
import app.blueprint.data.focusMsOn
import app.blueprint.data.pomosFor
import app.blueprint.data.pomosOn
import app.blueprint.data.remaining
import app.blueprint.data.sessionsOn
import app.blueprint.data.subjectTitle
import app.blueprint.data.Repository
import app.blueprint.data.deleteSession
import app.blueprint.focus.Pomodoro
import app.blueprint.i18n.f
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.Divider
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.IconTile
import app.blueprint.ui.components.ProgressBar
import app.blueprint.ui.components.SectionHeader
import app.blueprint.ui.components.T
import app.blueprint.ui.components.fieldBg
import app.blueprint.ui.components.pressable
import app.blueprint.ui.components.pressableDone
import app.blueprint.ui.glass.GlassIconButton
import app.blueprint.ui.glass.SegmentedControl
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.nav.Route
import app.blueprint.ui.screens.LargeTitleScreen
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import com.kyant.shapes.Capsule
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FocusScreen(data: AppData, today: Long) {
    val c = Theme.colors
    val s = Theme.strings
    val f = s.f
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    val ctx = LocalContext.current
    val view = LocalView.current
    val tm = data.timer
    val ps = data.pomo
    val color by animateColorAsState(c.phase(tm.phase), spring(1f, 200f), label = "pc")
    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { Pomodoro.refreshNotification(ctx) }

    DisposableEffect(tm.running && ps.keepScreenOn) {
        view.keepScreenOn = tm.running && ps.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    fun startOrPause() {
        if (!tm.running && Build.VERSION.SDK_INT >= 33 && ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        Pomodoro.toggle(ctx)
    }

    LargeTitleScreen(
        title = f.focus,
        subtitle = f.phaseName(tm.phase),
        actions = { b ->
            GlassIconButton(b, Icons.Rounded.Timeline, { nav.push(Route.FocusHistory) })
            GlassIconButton(b, Icons.Rounded.Tune, { nav.push(Route.FocusSettings) })
        },
    ) { _ ->
        item(key = "phase") {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SegmentedControl(Phase.entries.map { f.phaseName(it) }, tm.phase.ordinal, { i ->
                    val p = Phase.entries[i]
                    if (p == tm.phase) return@SegmentedControl
                    if (tm.running || tm.remainingMs > 0) {
                        overlay.alert(f.changePhaseQ, f.changePhaseMsg, listOf(AlertAction(s.cancel), AlertAction(f.change, destructive = true) { Pomodoro.setPhase(ctx, p) }))
                    } else Pomodoro.setPhase(ctx, p)
                })
            }
        }
        item(key = "dial") {
            TimerDial(data, color)
        }
        item(key = "controls") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                val canReset = tm.running || tm.remainingMs > 0
                RoundButton(Icons.Rounded.Refresh, f.reset, enabled = canReset, size = 64) { Pomodoro.reset(ctx) }
                MainButton(tm.running, color) { startOrPause() }
                RoundButton(Icons.Rounded.SkipNext, f.skip, enabled = true, size = 64, mirror = true) { Pomodoro.skip(ctx) }
            }
        }
        item(key = "rounds") {
            RoundsDots(tm.cycle, ps.longEvery, tm.phase, color)
        }
        item(key = "subject_h") { Header(if (f.fa) f.workingOn else "Working On") }
        item(key = "subject") { SubjectCard(data) }
        item(key = "stats_h") { Header(f.todaysFocus) }
        item(key = "stats") { TodayStats(data, today) }
        item(key = "tl_h") {
            Header(f.timeline, trailing = {
                T(f.seeAll, Theme.type.subhead.copy(fontWeight = FontWeight.Medium), Modifier.pressable(0.94f) { nav.push(Route.FocusHistory) }, color = c.blue)
            })
        }
        item(key = "tl") { TodayTimeline(data, today) }
    }
}

@Composable
private fun TimerDial(data: AppData, color: Color) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val hap = Theme.haptics
    val ctx = LocalContext.current
    val tm = data.timer
    val ps = data.pomo
    val now by rememberTicker(tm.running)
    val total = ps.durationMs(tm.phase).coerceAtLeast(1)
    val rem = tm.remaining(now, ps)
    val frac = (rem.toFloat() / total).coerceIn(0f, 1f)
    val idleFrac by animateFloatAsState(frac, spring(0.9f, 120f), label = "idle")
    val shown = if (tm.running) frac else idleFrac
    val secLeft = ((rem + 999) / 1000).toInt()

    LaunchedEffect(tm.running, secLeft) {
        if (tm.running && rem <= 0L) Pomodoro.complete(ctx)
        if (tm.running && ps.lastSecondsHaptic && secLeft in 1..5) hap.tick()
    }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(tm.running) { pop.snapTo(if (tm.running) 0.97f else 1.02f); pop.animateTo(1f, spring(0.45f, 320f)) }
    val glow = rememberInfiniteTransition(label = "g").animateFloat(0.55f, 1f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "ga").value

    Box(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 10.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(296.dp).scale(pop.value), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(296.dp)) {
                val sw = 16.dp.toPx()
                val inset = 26.dp.toPx()
                val r = size.minDimension / 2
                // minute ticks
                for (i in 0 until 60) {
                    val a = Math.toRadians(i * 6.0 - 90)
                    val long = i % 5 == 0
                    val r1 = r - (if (long) 9.dp.toPx() else 6.dp.toPx())
                    val r2 = r - 1.dp.toPx()
                    val lit = i / 60f >= 1f - shown
                    drawLine(
                        if (lit) color.copy(alpha = 0.55f) else c.label3.copy(alpha = 0.35f),
                        Offset(center.x + r1 * cos(a).toFloat(), center.y + r1 * sin(a).toFloat()),
                        Offset(center.x + r2 * cos(a).toFloat(), center.y + r2 * sin(a).toFloat()),
                        strokeWidth = (if (long) 2.2f else 1.3f).dp.toPx(), cap = StrokeCap.Round
                    )
                }
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val tl = Offset(inset, inset)
                drawArc(c.fill.copy(alpha = if (c.isDark) 0.35f else 0.22f), 0f, 360f, false, tl, arcSize, style = Stroke(sw))
                if (shown > 0.0005f) {
                    val sweep = 360f * shown
                    if (tm.running) {
                        drawArc(color.copy(alpha = 0.22f * glow), -90f, sweep, false, tl, arcSize, style = Stroke(sw * 2.1f, cap = StrokeCap.Round))
                    }
                    rotate(-90f) {
                        drawArc(
                            Brush.sweepGradient(listOf(color.copy(alpha = 0.65f), color, color), center),
                            0f, sweep, false, tl, arcSize, style = Stroke(sw, cap = StrokeCap.Round)
                        )
                    }
                    // knob at the head of the arc
                    val a = Math.toRadians((-90 + sweep).toDouble())
                    val rr = arcSize.width / 2
                    val p = Offset(center.x + rr * cos(a).toFloat(), center.y + rr * sin(a).toFloat())
                    drawCircle(Color.White, sw * 0.32f, p)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                T(f.phaseName(tm.phase).uppercase(), t.footnote.copy(fontWeight = FontWeight.SemiBold, letterSpacing = if (f.fa) 0.sp else 1.2.sp), color = color)
                Spacer(Modifier.height(2.dp))
                T(
                    f.mmss(rem),
                    t.largeTitle.copy(fontSize = 66.sp, lineHeight = 72.sp, fontWeight = FontWeight.Normal, fontFeatureSettings = "tnum", letterSpacing = 0.sp, fontFamily = t.display),
                    color = c.label, maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                AnimatedContent(Triple(tm.running, tm.remainingMs > 0, tm.endAt), transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) }, label = "st") { (run, paused, end) ->
                    val txt = when { run -> f.endsAt(f.clock(end)); paused -> f.paused; else -> f.minutes(ps.minutesFor(tm.phase)) }
                    T(txt, t.subhead, color = if (paused && !run) c.orange else c.label2)
                }
            }
        }
    }
}


@Composable
private fun MainButton(running: Boolean, color: Color, onClick: () -> Unit) {
    val hap = Theme.haptics
    Box(
        Modifier.size(88.dp).pressable(0.9f) { hap.confirm(); onClick() }
            .clip(CircleShape).background(color.copy(alpha = 0.22f)).padding(5.dp)
            .clip(CircleShape).background(Brush.verticalGradient(listOf(color.copy(alpha = 0.92f), color))),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(running, transitionSpec = {
            (scaleIn(spring(0.55f, 500f), initialScale = 0.4f) + fadeIn(tween(120))) togetherWith (scaleOut(tween(120), targetScale = 0.4f) + fadeOut(tween(100)))
        }, label = "pp") { r ->
            Icon(if (r) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, Modifier.size(40.dp), tint = Color.White)
        }
    }
}

@Composable
private fun RoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, enabled: Boolean, size: Int, mirror: Boolean = false, onClick: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val hap = Theme.haptics
    val a by animateFloatAsState(if (enabled) 1f else 0.35f, label = "a")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { alpha = a }) {
        Box(
            Modifier.size(size.dp).pressable(0.88f, enabled = enabled) { hap.tick(); onClick() }.clip(CircleShape).background(c.fill.copy(alpha = if (c.isDark) 0.4f else 0.22f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, Modifier.size(28.dp), tint = c.label, mirror = mirror) }
        Spacer(Modifier.height(6.dp))
        T(label, t.caption.copy(fontWeight = FontWeight.Medium), color = c.label2)
    }
}

@Composable
private fun RoundsDots(cycle: Int, every: Int, phase: Phase, color: Color) {
    val c = Theme.colors
    val f = Theme.strings.f
    val done = if (phase == Phase.Long) every else cycle.coerceAtMost(every)
    Column(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(every) { i ->
                val on = i < done
                val current = i == done && phase == Phase.Focus
                val w by animateFloatAsState(if (current) 22f else 8f, spring(0.6f, 400f), label = "w")
                val bg by animateColorAsState(if (on) c.phase(Phase.Focus) else if (current) color.copy(alpha = 0.6f) else c.fill.copy(alpha = 0.5f), label = "bg")
                Box(Modifier.width(w.dp).height(8.dp).clip(Capsule()).background(bg))
            }
        }
        Spacer(Modifier.height(6.dp))
        T(f.round((if (phase == Phase.Long) every else cycle % every + 1).coerceAtMost(every), every), Theme.type.footnote, color = c.label2)
    }
}

@Composable
private fun SubjectCard(data: AppData) {
    val c = Theme.colors
    val t = Theme.type
    val f = Theme.strings.f
    val overlay = LocalOverlay.current
    val tm = data.timer
    val todo = data.todos.firstOrNull { it.id == tm.todoId }
    val title = data.subjectTitle(tm.todoId, tm.label)
    Card(Modifier.padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().pressable(0.985f, 0.7f) { overlay.showSheet { dismiss -> SubjectSheet(data, dismiss) } }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(Icons.Rounded.Checklist, if (todo != null) c.habit(todo.color) else c.label3, 40.dp, 12.dp, 22.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                AnimatedContent(title, transitionSpec = { (fadeIn(tween(200)) + scaleIn(spring(0.8f, 500f), 0.96f)) togetherWith fadeOut(tween(100)) }, label = "sub") { tt ->
                    T(tt.ifBlank { f.chooseTask }, t.headline, color = if (tt.isBlank()) c.label2 else c.label, maxLines = 2)
                }
                if (todo != null) {
                    val n = data.pomosFor(todo.id)
                    Spacer(Modifier.height(2.dp))
                    T(if (todo.estimate > 0) "${f.n(n)} / ${f.pomos(todo.estimate)}" else f.pomos(n), t.footnote, color = c.label2)
                }
            }
            Icon(Icons.Rounded.ChevronRight, null, Modifier.size(20.dp), tint = c.label3, mirror = true)
        }
    }
}

@Composable
fun SubjectSheet(data: AppData, dismiss: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val f = s.f
    val ctx = LocalContext.current
    val hap = Theme.haptics
    var label by remember { mutableStateOf(if (data.timer.todoId == null) data.timer.label else "") }
    val todos = data.todos.filter { !it.done }.sortedWith(compareByDescending<Todo> { it.priority }.thenBy { it.dueDay ?: Long.MAX_VALUE })
    Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(if (f.fa) f.workingOn else "Working On", t.title3, Modifier.weight(1f))
            Box(Modifier.pressableDone {
                if (label.isNotBlank()) Pomodoro.setSubject(ctx, null, label.trim())
                dismiss()
            }) { T(s.done, t.headline, color = Color.White) }
        }
        Spacer(Modifier.height(14.dp))
        T(f.customLabel, t.footnote.copy(fontWeight = FontWeight.SemiBold), color = c.label2)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().fieldBg().padding(14.dp)) {
            if (label.isEmpty()) T(f.customLabelHint, t.body, color = c.label3)
            BasicTextField(label, { label = it.take(60) }, Modifier.fillMaxWidth(), singleLine = true, textStyle = t.body.copy(color = c.label), cursorBrush = SolidColor(c.blue))
        }
        Spacer(Modifier.height(16.dp))
        T(f.tasks, t.footnote.copy(fontWeight = FontWeight.SemiBold), color = c.label2)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().heightIn(max = 340.dp).clip(com.kyant.shapes.RoundedRectangle(18.dp)).background(c.fill.copy(alpha = 0.18f))) {
            LazyColumn {
                item {
                    SubjectRow(f.noTask, null, data.timer.todoId == null && data.timer.label.isBlank(), c.label3) {
                        hap.tick(); Pomodoro.setSubject(ctx, null, ""); dismiss()
                    }
                }
                items(todos, key = { it.id }) { td ->
                    Divider(48.dp)
                    val n = data.pomosFor(td.id)
                    SubjectRow(td.title, if (td.estimate > 0) "${f.n(n)}/${f.n(td.estimate)}" else if (n > 0) f.n(n) else null, data.timer.todoId == td.id, c.habit(td.color)) {
                        hap.tick(); Pomodoro.setSubject(ctx, td.id, ""); dismiss()
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SubjectRow(title: String, meta: String?, selected: Boolean, dot: Color, onClick: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    Row(Modifier.fillMaxWidth().pressable(0.98f, 0.7f, onClick = onClick).padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(20.dp))
        T(title, t.body, Modifier.weight(1f), maxLines = 1)
        if (meta != null) { T(meta, t.subhead, color = c.label2); Spacer(Modifier.width(8.dp)) }
        if (selected) Icon(Icons.Rounded.Check, null, Modifier.size(20.dp), tint = c.blue)
    }
}

@Composable
private fun TodayStats(data: AppData, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val f = Theme.strings.f
    val pom = data.pomosOn(today)
    val ms = data.focusMsOn(today)
    val tasksDone = data.todos.count { it.done && it.doneAt != null && epochDayOf(it.doneAt) == today }
    val goal = data.pomo.dailyGoal
    Card(Modifier.padding(horizontal = 16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                T(f.n(pom), t.title1.copy(fontFeatureSettings = "tnum"), color = c.phase(Phase.Focus))
                T(" / ${f.n(goal)}", t.title3.copy(fontWeight = FontWeight.Normal), color = c.label2)
                Spacer(Modifier.weight(1f))
                T(if (pom >= goal) f.goalReached else f.pomodoros, t.subhead, color = if (pom >= goal) c.green else c.label2)
            }
            Spacer(Modifier.height(10.dp))
            ProgressBar(pom.toFloat() / goal.coerceAtLeast(1), if (pom >= goal) c.green else c.phase(Phase.Focus))
            Spacer(Modifier.height(14.dp))
            Row {
                Stat(f.focusTime, f.dur(ms), Modifier.weight(1f))
                Stat(f.tasksDone, f.n(tasksDone), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        T(value, Theme.type.title3)
        T(label, Theme.type.footnote, color = Theme.colors.label2)
    }
}

@Composable
private fun TodayTimeline(data: AppData, today: Long) {
    val c = Theme.colors
    val f = Theme.strings.f
    val s = Theme.strings
    val overlay = LocalOverlay.current
    val tm = data.timer
    val list = data.sessionsOn(today)
    val now by rememberTicker(false)
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(tm.running) { while (tm.running) { tick = System.currentTimeMillis(); kotlinx.coroutines.delay(15_000) } }
    val live = if (tm.running && epochDayOf(tm.segStart) == today) FocusSession("live", tm.segStart, maxOf(tick, now, tm.segStart), tm.phase, tm.todoId, data.subjectTitle(tm.todoId, tm.label)) else null
    Card(Modifier.padding(horizontal = 16.dp)) {
        if (list.isEmpty() && live == null) {
            EmptyState(f.noSessions, f.noSessionsSub, Icons.Rounded.Timeline, c.phase(Phase.Focus))
        } else {
            Spacer(Modifier.height(4.dp))
            val all = list + listOfNotNull(live)
            all.forEachIndexed { i, ss ->
                val isLive = ss.id == "live"
                TimelineRow(
                    ss, ss.label.ifBlank { f.phaseName(ss.phase) }, c.phase(ss.phase), isLast = i == all.lastIndex, live = isLive,
                    onLongPress = if (isLive) null else ({
                        overlay.alert(f.deleteSessionQ, "${f.clock(ss.start)} – ${f.clock(ss.end)}", listOf(AlertAction(s.cancel), AlertAction(f.delete, destructive = true) { Repository.deleteSession(ss.id) }))
                    })
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
