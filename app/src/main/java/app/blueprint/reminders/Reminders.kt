package app.blueprint.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import app.blueprint.MainActivity
import app.blueprint.R
import app.blueprint.data.Frequency
import app.blueprint.data.Repository
import app.blueprint.data.isDueOn
import app.blueprint.i18n.En
import app.blueprint.i18n.Fa
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object Reminders {
    private const val CHANNEL = "reminders"
    const val ACTION_FIRE = "app.blueprint.REMIND"
    const val ACTION_DONE = "app.blueprint.DONE"

    private fun pi(context: Context, habitId: String, action: String): PendingIntent {
        val i = Intent(context, ReminderReceiver::class.java).setAction(action).putExtra("habit", habitId)
        return PendingIntent.getBroadcast(context, (action + habitId).hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun scheduleAll(context: Context) {
        Repository.init(context)
        val am = context.getSystemService(AlarmManager::class.java)
        val data = Repository.data.value
        val bp = data.blueprints.firstOrNull { it.id == data.activeId }
        // cancel all known habits first
        data.blueprints.flatMap { it.habits }.forEach { am.cancel(pi(context, it.id, ACTION_FIRE)) }
        bp ?: return
        val now = LocalDateTime.now()
        for (h in bp.habits) {
            val minutes = h.reminder ?: continue
            var day = LocalDate.now()
            var next: LocalDateTime? = null
            for (k in 0..8) {
                val cand = day.atStartOfDay().plusMinutes(minutes.toLong())
                val e = day.toEpochDay()
                val inRange = e in bp.startDay..bp.endDay
                val due = h.frequency == Frequency.PerWeek || h.isDueOn(e)
                if (cand.isAfter(now) && inRange && due) { next = cand; break }
                day = day.plusDays(1)
            }
            next ?: continue
            val ms = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pi(context, h.id, ACTION_FIRE))
        }
    }

    fun notify(context: Context, habitId: String) {
        Repository.init(context)
        val data = Repository.data.value
        val bp = data.active ?: return
        val h = bp.habits.firstOrNull { it.id == habitId } ?: return
        val today = LocalDate.now().toEpochDay()
        if (data.count(h.id, today) >= h.timesPerDay) return
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val s = if (data.settings.language == "fa") Fa else En
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, s.reminderChannel, NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE)
        val n = android.app.Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(h.name)
            .setContentText(h.description.ifBlank { s.reminderText(h.name) })
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(android.app.Notification.Action.Builder(null, s.markDone, pi(context, h.id, ACTION_DONE)).build())
            .build()
        nm.notify(h.id.hashCode(), n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("habit") ?: return
        Repository.init(context)
        when (intent.action) {
            Reminders.ACTION_FIRE -> { Reminders.notify(context, id); Reminders.scheduleAll(context) }
            Reminders.ACTION_DONE -> {
                val bp = Repository.data.value.active
                val h = bp?.habits?.firstOrNull { it.id == id }
                if (h != null) {
                    val today = LocalDate.now().toEpochDay()
                    val cur = Repository.data.value.count(id, today)
                    Repository.setCount(id, today, h.timesPerDay.coerceAtLeast(cur))
                }
                context.getSystemService(NotificationManager::class.java).cancel(id.hashCode())
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.scheduleAll(context)
        runCatching { app.blueprint.focus.Pomodoro.restore(context) }
    }
}
