package app.blueprint.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.blueprint.data.AppData
import app.blueprint.data.Frequency
import app.blueprint.data.Repository
import app.blueprint.data.habitSummary
import app.blueprint.data.summary
import app.blueprint.ui.components.Card
import app.blueprint.ui.components.HabitIcons
import app.blueprint.ui.components.Icon
import app.blueprint.ui.components.ProgressRing
import app.blueprint.ui.components.T
import app.blueprint.ui.components.pressable
import app.blueprint.ui.glass.GlassIconButton
import app.blueprint.ui.nav.AlertAction
import app.blueprint.ui.nav.LocalNav
import app.blueprint.ui.nav.LocalOverlay
import app.blueprint.ui.theme.Theme
import app.blueprint.ui.theme.habit
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

@Composable
fun HistoryScreen(data: AppData, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val nav = LocalNav.current
    val overlay = LocalOverlay.current
    LargeTitleScreen(title = s.pastBlueprints, onBack = { nav.pop() }, bottomPadding = 40.dp) { _ ->
        items(data.blueprints.reversed(), key = { it.id }) { bp ->
            val sum = remember(data, bp) { data.summary(bp, today) }
            val active = bp.id == data.activeId
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()
                    .pressable(0.98f, onLongPress = {
                        if (!active) overlay.alert(bp.name, null, listOf(
                            AlertAction(s.open, bold = true) { Repository.activate(bp.id) },
                            AlertAction(s.delete, destructive = true) { Repository.deleteBlueprint(bp.id) },
                            AlertAction(s.cancel),
                        ))
                    }) { if (!active) { Repository.activate(bp.id); nav.pop() } }
                    .clip(RoundedRectangle(24.dp)).background(c.card).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        T(bp.name, t.headline, maxLines = 1)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.clip(Capsule()).background((if (active) c.green else c.label3).copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            T(if (active) s.active else s.finished, t.caption2.copy(fontWeight = FontWeight.SemiBold), color = if (active) c.green else c.label2)
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    T("${s.dateShort(bp.startDay)} – ${s.dateShort(bp.endDay)} · ${s.nDays(bp.lengthDays)}", t.footnote, color = c.label2)
                    T("${s.perfectDays} ${s.num(sum.perfectDays)} · ${s.bestStreak} ${s.num(sum.bestStreak)}", t.footnote, color = c.label2)
                }
                Box(contentAlignment = Alignment.Center) {
                    ProgressRing(sum.avgCompletion, c.blue, Modifier.size(50.dp), 4.dp)
                    T(s.percent(sum.avgCompletion), t.caption2.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun HabitDetailScreen(data: AppData, habitId: String, today: Long) {
    val c = Theme.colors
    val t = Theme.type
    val s = Theme.strings
    val nav = LocalNav.current
    val bp = data.active ?: return
    val h = bp.habits.firstOrNull { it.id == habitId } ?: run { androidx.compose.runtime.LaunchedEffect(Unit) { nav.pop() }; return }
    val col = c.habit(h.color)
    val hs = remember(data, h, today) { data.habitSummary(bp, h, today) }
    LargeTitleScreen(
        title = h.name, onBack = { nav.pop() }, bottomPadding = 40.dp, largeTitle = false,
        actions = { b -> GlassIconButton(b, Icons.Rounded.Edit, { editHabit(nav, h) }) },
    ) { _ ->
        item {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    ProgressRing(hs.rate, col, Modifier.size(132.dp), 10.dp)
                    Box(Modifier.size(84.dp).clip(RoundedRectangle(26.dp)).background(col.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                        Icon(HabitIcons.get(h.icon), null, Modifier.size(44.dp), tint = col)
                    }
                }
                Spacer(Modifier.height(14.dp))
                T(h.name, t.title1)
                T(s.freqLabel(h.isDaily, h.frequency == Frequency.PerWeek, h.perWeek, h.days.size), t.subhead, color = c.label2)
                if (h.description.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.foundation.text.BasicText(h.description, Modifier.padding(horizontal = 32.dp), style = t.body.copy(color = c.label2, textAlign = TextAlign.Center))
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(s.rate to s.percent(hs.rate), s.streak to s.num(hs.streak), s.best to s.num(hs.best), s.total to s.num(hs.total)).forEach { (l, v) ->
                    Column(Modifier.weight(1f).clip(RoundedRectangle(20.dp)).background(c.card).padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        T(v, t.title3, color = col)
                        T(l, t.caption, color = c.label2, maxLines = 1)
                    }
                }
            }
        }
        item { Heatmap(data, bp, today, h.id, col) }
    }
}
