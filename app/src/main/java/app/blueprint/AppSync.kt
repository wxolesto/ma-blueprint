package app.blueprint

import android.content.Context
import androidx.glance.appwidget.updateAll
import app.blueprint.reminders.Reminders
import app.blueprint.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppSync {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    fun onDataChanged(context: Context) {
        job?.cancel()
        job = scope.launch {
            delay(250)
            runCatching { Reminders.scheduleAll(context) }
            runCatching { TodayWidget().updateAll(context) }
        }
    }
}
