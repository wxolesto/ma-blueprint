package app.blueprint.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.blueprint.MainActivity
import app.blueprint.R
import app.blueprint.data.Frequency
import app.blueprint.data.Repository
import app.blueprint.data.dayNumber
import app.blueprint.data.isDueOn
import app.blueprint.i18n.En
import app.blueprint.i18n.Fa
import java.time.LocalDate

private val habitKey = ActionParameters.Key<String>("habit")

class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Repository.init(context)
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val data = Repository.data.value
        val s = if (data.settings.language == "fa") Fa else En
        val bp = data.active
        val label = ColorProvider(Color.Black, Color.White)
        val label2 = ColorProvider(Color(0x993C3C43), Color(0x99EBEBF5))
        val blue = ColorProvider(Color(0xFF007AFF), Color(0xFF0A84FF))
        Column(
            GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg)).cornerRadius(22.dp).padding(14.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            if (bp == null) {
                Text(s.appName, style = TextStyle(color = label, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                return@Column
            }
            val today = LocalDate.now().toEpochDay()
            val habits = bp.habits.filter { it.frequency == Frequency.PerWeek || it.isDueOn(today) }
            val done = habits.count { data.count(it.id, today) >= (if (it.frequency == Frequency.PerWeek) 1 else it.timesPerDay) }
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(GlanceModifier.defaultWeight()) {
                    Text(bp.name, style = TextStyle(color = blue, fontSize = 11.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                    Text(s.dayOf(bp.dayNumber(today).coerceIn(1, bp.lengthDays), bp.lengthDays), style = TextStyle(color = label, fontSize = 15.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                }
                Text("${s.num(done)}/${s.num(habits.size)}", style = TextStyle(color = label2, fontSize = 13.sp, fontWeight = FontWeight.Bold))
            }
            Spacer(GlanceModifier.height(8.dp))
            habits.take(5).forEach { h ->
                val target = if (h.frequency == Frequency.PerWeek) 1 else h.timesPerDay
                val ok = data.count(h.id, today) >= target
                Row(
                    GlanceModifier.fillMaxWidth().padding(vertical = 4.dp).clickable(actionRunCallback<ToggleHabit>(actionParametersOf(habitKey to h.id))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(h.name, GlanceModifier.defaultWeight(), style = TextStyle(color = if (ok) label2 else label, fontSize = 13.sp), maxLines = 1)
                    Spacer(GlanceModifier.width(6.dp))
                    Image(ImageProvider(if (ok) R.drawable.w_check_on else R.drawable.w_check_off), null, GlanceModifier.size(22.dp))
                }
            }
        }
    }
}

class ToggleHabit : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Repository.init(context)
        val id = parameters[habitKey] ?: return
        val h = Repository.data.value.active?.habits?.firstOrNull { it.id == id } ?: return
        val today = LocalDate.now().toEpochDay()
        if (h.frequency == Frequency.PerWeek) {
            val cur = Repository.data.value.count(id, today)
            Repository.setCount(id, today, if (cur > 0) 0 else 1)
        } else Repository.tap(h, today)
        TodayWidget().update(context, glanceId)
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}
