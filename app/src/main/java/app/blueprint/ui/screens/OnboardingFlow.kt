package app.blueprint.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.blueprint.data.Frequency
import app.blueprint.data.Habit
import app.blueprint.data.Repository
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.CheckCircle
import app.blueprint.ui.components.Chip
import app.blueprint.ui.components.Divider
import app.blueprint.ui.components.HabitIcons
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.IconTile
import app.blueprint.ui.components.PrimaryButton
import app.blueprint.ui.components.Stepper
import app.blueprint.ui.components.T
import app.blueprint.ui.components.fieldBg
import app.blueprint.ui.components.pressable
import app.blueprint.ui.glass.GlassIconButton
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.Route
import app.blueprint.ui.theme.Motion
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private data class Template(val en: String, val fa: String, val icon: String, val color: Int, val freq: Frequency = Frequency.Scheduled, val perWeek: Int = 3, val tpd: Int = 1)

private val templates = listOf(
    Template("Workout", "ورزش", "workout", 0, Frequency.PerWeek, 4),
    Template("Meditate", "مدیتیشن", "meditate", 3),
    Template("Morning Sunlight", "نور صبحگاهی", "sun", 6),
    Template("Read 20 pages", "خواندن ۲۰ صفحه", "read", 2),
    Template("Drink Water", "نوشیدن آب", "water", 5, tpd = 8),
    Template("Mobility", "تحرک و کشش", "mobility", 9),
    Template("Journal", "نوشتن روزانه", "journal", 10),
    Template("Walk 10k steps", "۱۰ هزار قدم", "walk", 1),
    Template("No Sugar", "بدون قند", "nofood", 7),
    Template("Sleep by 11", "خواب تا ساعت ۱۱", "sleep", 8),
    Template("Learn a Language", "یادگیری زبان", "language", 4),
    Template("Deep Work", "کار عمیق", "code", 1),
    Template("No Phone in Bed", "بدون گوشی در تخت", "phone", 11),
    Template("Run", "دویدن", "run", 4, Frequency.PerWeek, 3),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingFlow(showWelcome: Boolean) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val hap = Theme.haptics
    val nav = LocalNav.current
    val firstStep = if (showWelcome) 0 else 1
    var step by rememberSaveable { mutableIntStateOf(firstStep) }
    var name by rememberSaveable { mutableStateOf("") }
    var length by rememberSaveable { mutableIntStateOf(30) }
    var startOpt by rememberSaveable { mutableIntStateOf(0) }
    val habits = remember { mutableStateListOf<Habit>() }
    val backdrop = rememberLayerBackdrop()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val today = LocalDate.now()
    val startDate = when (startOpt) {
        0 -> today
        1 -> today.plusDays(1)
        else -> today.with(TemporalAdjusters.next(if (s.isFa) java.time.DayOfWeek.SATURDAY else java.time.DayOfWeek.MONDAY))
    }

    fun templateHabit(tp: Template) = Habit(Repository.newId(), if (s.isFa) tp.fa else tp.en, tp.icon, tp.color, frequency = tp.freq, perWeek = tp.perWeek, timesPerDay = tp.tpd)
    fun next() {
        hap.light()
        if (step < 4) step++ else {
            hap.confirm()
            Repository.createBlueprint(name.ifBlank { s.nameSuggestions.first() }, startDate.toEpochDay(), length, habits.toList())
            if (nav.stack.size > 1) nav.replaceRoot(Route.Tabs) else nav.replaceRoot(Route.Tabs)
        }
    }
    val canContinue = when (step) { 1 -> name.isNotBlank(); 3 -> habits.isNotEmpty(); else -> true }
    val canGoBack = step > firstStep || nav.stack.size > 1

    androidx.activity.compose.BackHandler(enabled = step > firstStep) { step-- }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            ScreenBackground()
            AnimatedContent(
                step,
                transitionSpec = {
                    val fwd = targetState > initialState
                    val d = if (rtl) -1 else 1
                    (slideInHorizontally(spring(0.9f, 300f)) { w -> (if (fwd) w else -w / 3) * d } + fadeIn(tween(250)))
                        .togetherWith(slideOutHorizontally(spring(0.9f, 300f)) { w -> (if (fwd) -w / 3 else w) * d } + fadeOut(tween(200)))
                },
                label = "step",
                modifier = Modifier.fillMaxSize()
            ) { st ->
                Column(Modifier.fillMaxSize().statusBarsPadding().padding(top = 64.dp)) {
                    when (st) {
                        0 -> Welcome()
                        1 -> NameStep(name) { name = it }
                        2 -> LengthStep(length, { length = it }, startOpt, { startOpt = it })
                        3 -> HabitsStep(habits, ::templateHabit)
                        else -> ReadyStep(name, length, startDate.toEpochDay(), habits)
                    }
                }
            }
        }
        // top bar
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp).height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            if (canGoBack) GlassIconButton(backdrop, Icons.Rounded.ChevronLeft, { if (step > firstStep) step-- else nav.pop() }, mirror = true)
            else Spacer(Modifier.size(44.dp))
            Spacer(Modifier.weight(1f))
            if (step > 0) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..4) {
                    val w by animateDpAsState(if (i == step) 22.dp else 7.dp, Motion.snappy(), label = "w")
                    Box(Modifier.height(7.dp).width(w).clip(Capsule()).background(if (i <= step) c.blue else c.label3))
                }
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(44.dp))
        }
        // bottom CTA
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, c.bg.copy(alpha = 0.9f), c.bg)))
                .navigationBarsPadding().padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 12.dp)
        ) {
            PrimaryButton(
                text = when (step) { 0 -> s.getStarted; 4 -> s.dayOneStarts; else -> s.continueTxt },
                enabled = canContinue,
                gradient = if (step == 4) listOf(c.blue, Color(0xFFBF5AF2)) else null,
                trailing = if (step == 4) Icons.AutoMirrored.Rounded.ArrowForward else null,
            ) { if (canContinue) next() else hap.reject() }
        }
    }
}

@Composable
private fun Welcome() {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, spring(0.7f, 120f)) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(40.dp))
        Box(
            Modifier.size(108.dp).graphicsLayer { scaleX = 0.6f + 0.4f * a.value; scaleY = scaleX; alpha = a.value.coerceIn(0f, 1f); rotationZ = (1f - a.value) * -20f }
                .clip(RoundedRectangle(30.dp)).background(Brush.linearGradient(listOf(Color(0xFF1A3A8F), Color(0xFF0A84FF)))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Architecture, null, Modifier.size(58.dp), tint = Color.White) }
        Spacer(Modifier.height(28.dp))
        T(s.welcomeTitle, t.largeTitle.copy(textAlign = TextAlign.Center, fontSize = androidx.compose.ui.unit.TextUnit(40f, androidx.compose.ui.unit.TextUnitType.Sp)))
        Spacer(Modifier.height(8.dp))
        T(s.welcomeSub, t.body.copy(textAlign = TextAlign.Center), color = c.label2)
        Spacer(Modifier.height(40.dp))
        val icons = listOf(Icons.Rounded.TaskAlt to c.blue, Icons.Rounded.CheckCircle to c.green, Icons.Rounded.Insights to Color(0xFFBF5AF2))
        s.welcomePoints.forEachIndexed { i, (title, sub) ->
            val p = remember { Animatable(0f) }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(200L + i * 110L); p.animateTo(1f, spring(0.8f, 200f)) }
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp).graphicsLayer { alpha = p.value.coerceIn(0f, 1f); translationY = (1f - p.value) * 40f }, verticalAlignment = Alignment.CenterVertically) {
                Icon(icons[i].first, null, Modifier.size(34.dp), tint = icons[i].second)
                Spacer(Modifier.width(16.dp))
                Column { T(title, t.headline); T(sub, t.subhead, color = c.label2) }
            }
        }
        Spacer(Modifier.height(140.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NameStep(name: String, onName: (String) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        T(s.nameYourBlueprint, t.title1)
        Spacer(Modifier.height(6.dp))
        T(s.nameSub, t.body, color = c.label2)
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth().fieldBg().padding(horizontal = 18.dp, vertical = 16.dp)) {
            if (name.isEmpty()) T(s.nameSuggestions.first(), t.title3.copy(fontWeight = FontWeight.Medium), color = c.label3)
            BasicTextField(name, { onName(it.take(40)) }, Modifier.fillMaxWidth(), singleLine = true, textStyle = t.title3.copy(color = c.label, fontWeight = FontWeight.Medium), cursorBrush = SolidColor(c.blue))
        }
        Spacer(Modifier.height(18.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            s.nameSuggestions.forEach { sug -> Chip(sug, sug == name) { onName(sug) } }
        }
        Spacer(Modifier.height(140.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LengthStep(length: Int, onLength: (Int) -> Unit, startOpt: Int, onStart: (Int) -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val hap = Theme.haptics
    val presets = listOf(7, 14, 21, 30, 60, 90)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        T(s.howLong, t.title1)
        Spacer(Modifier.height(6.dp))
        T(s.howLongSub, t.body, color = c.label2)
        Spacer(Modifier.height(24.dp))
        FlowRow(Modifier.fillMaxWidth(), maxItemsInEachRow = 3, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            presets.forEach { p ->
                val sel = p == length
                Column(
                    Modifier.weight(1f).pressable(0.95f) { hap.tick(); onLength(p) }.clip(RoundedRectangle(22.dp))
                        .background(if (sel) c.blue else c.card).padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    T(s.num(p), t.title1, color = if (sel) Color.White else c.label)
                    T(s.days, t.caption.copy(fontWeight = FontWeight.SemiBold), color = if (sel) Color.White.copy(0.8f) else c.label2)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                T(s.custom, t.body, Modifier.weight(1f))
                T(s.nDays(length), t.body, color = c.label2)
                Spacer(Modifier.width(10.dp))
                Stepper(length, 3..365, onLength)
            }
        }
        Spacer(Modifier.height(26.dp))
        T(s.whenStart, t.footnote.copy(fontWeight = FontWeight.Medium), Modifier.padding(start = 4.dp), color = c.label2)
        Spacer(Modifier.height(8.dp))
        app.blueprint.ui.glass.SegmentedControl(listOf(s.today, s.tomorrow, s.nextMonday), startOpt, onStart)
        Spacer(Modifier.height(140.dp))
    }
}

@Composable
private fun HabitsStep(habits: MutableList<Habit>, fromTemplate: (Template) -> Habit) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val hap = Theme.haptics
    val nav = LocalNav.current
    val templateNames = templates.map { if (s.isFa) it.fa else it.en }.toSet()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 140.dp)) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                T(s.pickHabits, t.title1)
                Spacer(Modifier.height(6.dp))
                T(s.pickHabitsSub, t.body, color = c.label2)
                Spacer(Modifier.height(18.dp))
            }
        }
        // custom habits first
        items(habits.filter { it.name !in templateNames }, key = { it.id }) { h ->
            SuggestionRow(h.name, h.icon, h.color, s.freqLabel(h.isDaily, h.frequency == Frequency.PerWeek, h.perWeek, h.days.size), true,
                onClick = { nav.push(Route.EditHabit(h, false, onSave = { nh -> val i = habits.indexOfFirst { it.id == nh.id }; if (i >= 0) habits[i] = nh }, onDelete = { habits.removeAll { it.id == h.id } })) },
                onToggle = { hap.light(); habits.removeAll { it.id == h.id } })
        }
        items(templates, key = { it.en }) { tp ->
            val nm = if (s.isFa) tp.fa else tp.en
            val existing = habits.firstOrNull { it.name == nm }
            val h = existing ?: fromTemplate(tp)
            SuggestionRow(nm, tp.icon, tp.color, s.freqLabel(h.isDaily, h.frequency == Frequency.PerWeek, h.perWeek, h.days.size) + if (h.timesPerDay > 1) " · " + s.timesADay(h.timesPerDay) else "", existing != null,
                onClick = {
                    if (existing != null) nav.push(Route.EditHabit(existing, false, onSave = { nh -> val i = habits.indexOfFirst { it.id == nh.id }; if (i >= 0) habits[i] = nh }, onDelete = { habits.removeAll { it.id == existing.id } }))
                    else { hap.confirm(); habits.add(h) }
                },
                onToggle = { if (existing != null) { hap.light(); habits.remove(existing) } else { hap.confirm(); habits.add(h) } })
        }
        item {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().pressable(0.97f) {
                    nav.push(Route.EditHabit(null, true, onSave = { habits.add(0, it) }))
                }.clip(RoundedRectangle(22.dp)).border(1.dp, c.blue.copy(alpha = 0.5f), RoundedRectangle(22.dp)).padding(18.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Add, null, Modifier.size(20.dp), tint = c.blue)
                Spacer(Modifier.width(8.dp))
                T(s.customHabit, t.headline, color = c.blue)
            }
        }
    }
}

@Composable
private fun SuggestionRow(name: String, icon: String, color: Int, sub: String, selected: Boolean, onClick: () -> Unit, onToggle: () -> Unit) {
    val c = Theme.colors
    val t = Theme.type
    val col = c.habit(color)
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth().pressable(0.975f) { onClick() }
            .clip(RoundedRectangle(22.dp)).background(if (selected) androidx.compose.ui.graphics.lerp(c.card, col, 0.14f) else c.card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(HabitIcons.get(icon), col, 42.dp, 12.dp, 23.dp, filled = false)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { T(name, t.headline, maxLines = 1); T(sub, t.footnote, color = c.label2, maxLines = 1) }
        Box(Modifier.pressable(0.85f) { onToggle() }) { CheckCircle(if (selected) 1 else 0, 1, col, size = 30.dp) }
    }
}

@Composable
private fun ReadyStep(name: String, length: Int, start: Long, habits: List<Habit>) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val today = LocalDate.now().toEpochDay()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        T(s.ready, t.title1)
        Spacer(Modifier.height(4.dp))
        T(name.ifBlank { s.nameSuggestions.first() }, t.title3, color = c.blue)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val tiles = listOf(
                s.num(length) to s.days,
                s.num(habits.size) to s.habits,
                (if (start == today) s.today else if (start == today + 1) s.tomorrow else s.dateShort(start)) to s.start,
            )
            tiles.forEachIndexed { i, (v, l) ->
                val p = remember { Animatable(0f) }
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(80L * i); p.animateTo(1f, spring(0.6f, 260f)) }
                Column(
                    Modifier.weight(1f).graphicsLayer { scaleX = 0.8f + 0.2f * p.value; scaleY = scaleX; alpha = p.value.coerceIn(0f, 1f) }
                        .clip(RoundedRectangle(22.dp)).background(c.card).padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    T(v, t.title2, maxLines = 1)
                    T(l, t.caption.copy(fontWeight = FontWeight.SemiBold), color = c.label2)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card {
            habits.forEachIndexed { i, h ->
                if (i > 0) Divider(70.dp)
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(HabitIcons.get(h.icon), c.habit(h.color), 40.dp, 12.dp, 22.dp, filled = false)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        T(h.name, t.headline, maxLines = 1)
                        T(s.freqLabel(h.isDaily, h.frequency == Frequency.PerWeek, h.perWeek, h.days.size), t.footnote, color = c.label2)
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.padding(horizontal = 6.dp)) {
            Icon(Icons.Rounded.FormatQuote, null, Modifier.size(22.dp), tint = c.label3)
            Spacer(Modifier.width(8.dp))
            T(s.quotesList[1], t.subhead, color = c.label2)
        }
        Spacer(Modifier.height(150.dp))
    }
}
