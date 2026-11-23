package app.blueprint.focus

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import app.blueprint.MainActivity
import app.blueprint.R
import app.blueprint.data.AppData
import app.blueprint.data.FocusSession
import app.blueprint.data.Phase
import app.blueprint.data.Repository
import app.blueprint.data.TimerState
import app.blueprint.data.minutesFor
import app.blueprint.data.pomosFor
import app.blueprint.data.remaining
import app.blueprint.data.subjectTitle
import app.blueprint.i18n.FocusStrings
import app.blueprint.i18n.focusStrings

/**
 * Pomodoro engine. All state lives in [AppData.timer] so it survives process death.
 * The end of a phase is delivered by an exact alarm, so it works with the app closed.
 */
object Pomodoro {
    const val ACTION_FINISH = "app.blueprint.pomo.FINISH"
    const val ACTION_PAUSE = "app.blueprint.pomo.PAUSE"
    const val ACTION_RESUME = "app.blueprint.pomo.RESUME"
    const val ACTION_SKIP = "app.blueprint.pomo.SKIP"
    const val ACTION_STOP = "app.blueprint.pomo.STOP"
    private const val CH_ONGOING = "pomo_ongoing"
    private const val NID_ONGOING = 7001
    private const val NID_DONE = 7002

    private val d get() = Repository.data.value
    private fun fs(data: AppData = d): FocusStrings = focusStrings(data.settings.language == "fa")

    // ---------------- public API ----------------
    fun start(ctx: Context) {
        val data = d
        if (data.timer.running) return
        val now = System.currentTimeMillis()
        val rem = data.timer.remaining(now, data.pomo)
        Repository.update { it.copy(timer = it.timer.copy(running = true, endAt = now + rem, remainingMs = 0, segStart = now)) }
        schedule(ctx)
        cancel(ctx, NID_DONE)
        showOngoing(ctx)
    }

    fun pause(ctx: Context) {
        val data = d
        if (!data.timer.running) return
        val now = System.currentTimeMillis()
        Repository.update { a ->
            a.copy(timer = a.timer.copy(running = false, remainingMs = (a.timer.endAt - now).coerceAtLeast(1000)), sessions = logSegment(a, now, false))
        }
        unschedule(ctx)
        showOngoing(ctx)
    }

    fun toggle(ctx: Context) = if (d.timer.running) pause(ctx) else start(ctx)

    /** stop and reset the current phase to its full length */
    fun reset(ctx: Context) {
        val now = System.currentTimeMillis()
        Repository.update { a ->
            val sess = if (a.timer.running) logSegment(a, now, false) else a.sessions
            a.copy(timer = a.timer.copy(running = false, remainingMs = 0, endAt = 0), sessions = sess)
        }
        unschedule(ctx)
        cancel(ctx, NID_ONGOING)
    }

    /** reset the whole set (round counter) too */
    fun stopAll(ctx: Context) {
        reset(ctx)
        Repository.update { it.copy(timer = it.timer.copy(phase = Phase.Focus, cycle = 0)) }
    }

    fun skip(ctx: Context) {
        val now = System.currentTimeMillis()
        val wasRunning = d.timer.running
        Repository.update { a ->
            val sess = if (a.timer.running) logSegment(a, now, false) else a.sessions
            val next = nextPhase(a.timer, a.pomo.longEvery, countFocus = false)
            a.copy(timer = a.timer.copy(phase = next.first, cycle = next.second, running = false, remainingMs = 0, endAt = 0), sessions = sess)
        }
        unschedule(ctx)
        if (wasRunning && autoStartFor(d)) start(ctx) else cancel(ctx, NID_ONGOING)
    }

    fun setPhase(ctx: Context, p: Phase) {
        if (d.timer.phase == p && !d.timer.running) return
        reset(ctx)
        Repository.update { it.copy(timer = it.timer.copy(phase = p)) }
    }

    fun setSubject(ctx: Context, todoId: String?, label: String) {
        val now = System.currentTimeMillis()
        Repository.update { a ->
            // split the running segment so the timeline shows the switch of subject
            if (a.timer.running && now - a.timer.segStart > 5_000) {
                a.copy(sessions = logSegment(a, now, false), timer = a.timer.copy(todoId = todoId, label = label, segStart = now))
            } else a.copy(timer = a.timer.copy(todoId = todoId, label = label))
        }
        if (d.timer.running || d.timer.remainingMs > 0) showOngoing(ctx)
    }

    /** Called by the alarm or by the UI when the countdown reaches zero. Idempotent. */
    fun complete(ctx: Context) {
        val data = d
        val tm = data.timer
        val now = System.currentTimeMillis()
        if (!tm.running || now < tm.endAt - 1500) return
        val finished = tm.phase
        Repository.update { a ->
            val sess = logSegment(a, a.timer.endAt.coerceAtMost(now), true)
            val next = nextPhase(a.timer, a.pomo.longEvery, countFocus = true)
            var todos = a.todos
            if (finished == Phase.Focus && a.pomo.autoCompleteTask && a.timer.todoId != null) {
                val t = todos.firstOrNull { it.id == a.timer.todoId }
                if (t != null && t.estimate > 0 && a.copy(sessions = sess).pomosFor(t.id) >= t.estimate)
                    todos = todos.map { if (it.id == t.id) it.copy(done = true, doneAt = now) else it }
            }
            a.copy(sessions = sess, todos = todos, timer = a.timer.copy(phase = next.first, cycle = next.second, running = false, remainingMs = 0, endAt = 0))
        }
        val auto = autoStartFor(d)
        if (auto) start(ctx) else cancel(ctx, NID_ONGOING)
        notifyDone(ctx, finished, auto)
    }

    /** after reboot / app update */
    fun restore(ctx: Context) {
        val tm = d.timer
        if (!tm.running) return
        if (System.currentTimeMillis() >= tm.endAt) complete(ctx) else { schedule(ctx); showOngoing(ctx) }
    }

    fun refreshNotification(ctx: Context) {
        val tm = d.timer
        if (tm.running || tm.remainingMs > 0) showOngoing(ctx) else cancel(ctx, NID_ONGOING)
    }

    // ---------------- internals ----------------
    private fun autoStartFor(a: AppData) = if (a.timer.phase == Phase.Focus) a.pomo.autoFocus else a.pomo.autoBreaks

    private fun nextPhase(tm: TimerState, longEvery: Int, countFocus: Boolean): Pair<Phase, Int> = when (tm.phase) {
        Phase.Focus -> {
            val c = if (countFocus) tm.cycle + 1 else tm.cycle
            if (countFocus && c >= longEvery) Phase.Long to c else Phase.Short to c
        }
        Phase.Short -> Phase.Focus to tm.cycle
        Phase.Long -> Phase.Focus to 0
    }

    private fun logSegment(a: AppData, end: Long, completed: Boolean): List<FocusSession> {
        val tm = a.timer
        if (tm.segStart <= 0 || end <= tm.segStart) return a.sessions
        if (tm.phase != Phase.Focus && !a.pomo.logBreaks) return a.sessions
        val len = end - tm.segStart
        if (!completed && len < a.pomo.minLogMin * 60_000L) return a.sessions
        val s = FocusSession(Repository.newId(), tm.segStart, end, tm.phase, tm.todoId, a.subjectTitle(tm.todoId, tm.label), completed)
        return a.sessions + s
    }

    private fun pi(ctx: Context, action: String, code: Int): PendingIntent =
        PendingIntent.getBroadcast(ctx, code, Intent(ctx, PomodoroReceiver::class.java).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun schedule(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val at = d.timer.endAt
        val p = pi(ctx, ACTION_FINISH, 1)
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) {
            val show = PendingIntent.getActivity(ctx, 2, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            runCatching { am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), p) }
                .onFailure { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p) }
        } else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p)
    }

    private fun unschedule(ctx: Context) = ctx.getSystemService(AlarmManager::class.java).cancel(pi(ctx, ACTION_FINISH, 1))

    private fun canNotify(ctx: Context) = Build.VERSION.SDK_INT < 33 || ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun cancel(ctx: Context, id: Int) = ctx.getSystemService(NotificationManager::class.java).cancel(id)

    private fun openIntent(ctx: Context) = PendingIntent.getActivity(
        ctx, 0, Intent(ctx, MainActivity::class.java).putExtra("tab", 2).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun phaseColor(p: Phase) = when (p) { Phase.Focus -> 0xFFFF453A.toInt(); Phase.Short -> 0xFF30D158.toInt(); Phase.Long -> 0xFF0A84FF.toInt() }

    private fun showOngoing(ctx: Context) {
        val data = d
        if (!data.pomo.ongoing || !canNotify(ctx)) { cancel(ctx, NID_ONGOING); return }
        val s = fs(data)
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH_ONGOING, s.chOngoing, NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) })
        val tm = data.timer
        val subject = data.subjectTitle(tm.todoId, tm.label)
        val title = s.phaseName(tm.phase) + if (subject.isNotBlank()) " · $subject" else ""
        val b = Notification.Builder(ctx, CH_ONGOING)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setColor(phaseColor(tm.phase))
            .setContentTitle(title)
            .setContentIntent(openIntent(ctx))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_STOPWATCH)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
        if (tm.running) {
            b.setUsesChronometer(true).setWhen(tm.endAt).setShowWhen(true)
            if (Build.VERSION.SDK_INT >= 24) b.setChronometerCountDown(true)
            b.setContentText(s.endsAt(s.clock(tm.endAt)))
            b.addAction(Notification.Action.Builder(null, s.pause, pi(ctx, ACTION_PAUSE, 3)).build())
            b.addAction(Notification.Action.Builder(null, s.skip, pi(ctx, ACTION_SKIP, 4)).build())
        } else {
            b.setShowWhen(false).setContentText(s.pausedLeft(s.mmss(tm.remaining(System.currentTimeMillis(), data.pomo))))
            b.addAction(Notification.Action.Builder(null, s.resume, pi(ctx, ACTION_RESUME, 5)).build())
        }
        b.addAction(Notification.Action.Builder(null, s.stop, pi(ctx, ACTION_STOP, 6)).build())
        nm.notify(NID_ONGOING, b.build())
    }

    private fun doneChannel(ctx: Context, sound: Boolean, vibrate: Boolean, s: FocusStrings): String {
        val id = "pomo_done_${if (sound) 1 else 0}${if (vibrate) 1 else 0}"
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(id) == null) {
            val ch = NotificationChannel(id, s.chDone(sound, vibrate), NotificationManager.IMPORTANCE_HIGH)
            if (sound) ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            else ch.setSound(null, null)
            ch.enableVibration(vibrate)
            if (vibrate) ch.vibrationPattern = longArrayOf(0, 350, 180, 350, 180, 600)
            ch.enableLights(true); ch.lightColor = Color.RED
            nm.createNotificationChannel(ch)
        }
        return id
    }

    fun doneChannelId(ctx: Context): String { val a = d; return doneChannel(ctx, a.pomo.sound, a.pomo.vibrate, fs(a)) }

    private fun notifyDone(ctx: Context, finished: Phase, autoStarted: Boolean) {
        val data = d
        if (!data.pomo.notify || !canNotify(ctx)) return
        val s = fs(data)
        val ch = doneChannel(ctx, data.pomo.sound, data.pomo.vibrate, s)
        val next = data.timer.phase
        val subject = data.subjectTitle(data.timer.todoId, data.timer.label)
        val title = if (finished == Phase.Focus) s.focusDoneTitle else s.breakDoneTitle
        val text = buildString {
            if (finished == Phase.Focus && subject.isNotBlank()) append(subject).append(" · ")
            append(if (autoStarted) s.nextStarted(next, data.pomo.minutesFor(next)) else s.nextUp(next, data.pomo.minutesFor(next)))
        }
        val b = Notification.Builder(ctx, ch)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setColor(phaseColor(finished))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(openIntent(ctx))
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
        if (!autoStarted) b.addAction(Notification.Action.Builder(null, s.startPhase(next), pi(ctx, ACTION_RESUME, 7)).build())
        nm(ctx).notify(NID_DONE, b.build())
    }

    private fun nm(ctx: Context) = ctx.getSystemService(NotificationManager::class.java)
}


class PomodoroReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Repository.init(context)
        val pending = goAsync()
        when (intent.action) {
            Pomodoro.ACTION_FINISH -> Pomodoro.complete(context)
            Pomodoro.ACTION_PAUSE -> Pomodoro.pause(context)
            Pomodoro.ACTION_RESUME -> Pomodoro.start(context)
            Pomodoro.ACTION_SKIP -> Pomodoro.skip(context)
            Pomodoro.ACTION_STOP -> Pomodoro.reset(context)
        }
        // give the repository time to persist the new state before the process may be frozen
        Handler(Looper.getMainLooper()).postDelayed({ pending.finish() }, 600)
    }
}
