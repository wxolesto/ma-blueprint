package app.blueprint.i18n

import app.blueprint.data.Phase
import app.blueprint.data.minuteOfDay
import app.blueprint.data.toPersianDigits

abstract class FocusStrings {
    abstract val fa: Boolean
    fun n(x: Int) = if (fa) x.toString().toPersianDigits() else x.toString()
    fun mmss(ms: Long): String {
        val sec = ((ms + 999) / 1000).toInt()
        val h = sec / 3600; val m = (sec % 3600) / 60; val s = sec % 60
        val str = if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        return if (fa) str.toPersianDigits() else str
    }
    fun clock(ms: Long): String { val m = minuteOfDay(ms); return (if (fa) Fa else En).time(m) }
    fun clock24(ms: Long): String { val m = minuteOfDay(ms); val s = "%02d:%02d".format(m / 60, m % 60); return if (fa) s.toPersianDigits() else s }
    abstract fun dur(ms: Long): String
    abstract fun minutes(n: Int): String
    abstract fun phaseName(p: Phase): String
    abstract fun phaseShort(p: Phase): String

    abstract val tasks: String
    abstract val focus: String
    abstract val newTask: String
    abstract val active: String
    abstract val completed: String
    abstract val overdue: String
    abstract val todaySec: String
    abstract val tomorrow: String
    abstract val upcoming: String
    abstract val someday: String
    abstract val noTasks: String
    abstract val noTasksSub: String
    abstract val noCompleted: String
    abstract val clearCompleted: String
    abstract val clearCompletedQ: String
    abstract val editTask: String
    abstract val taskTitle: String
    abstract val notes: String
    abstract val notesHint: String
    abstract val dueDate: String
    abstract val none: String
    abstract val nextWeek: String
    abstract val pickDate: String
    abstract val priority: String
    abstract val priorities: List<String>
    abstract val estimate: String
    abstract val estimateSub: String
    abstract fun pomos(n: Int): String
    abstract val subtasks: String
    abstract val addSubtask: String
    abstract val deleteTask: String
    abstract val deleteTaskQ: String
    abstract val startFocus: String
    abstract val focusOnTask: String
    abstract val totalFocus: String
    abstract val sessionsTitle: String
    abstract val color: String
    abstract val workingOn: String
    abstract val chooseTask: String
    abstract val noTask: String
    abstract val customLabel: String
    abstract val customLabelHint: String
    abstract fun round(n: Int, of: Int): String
    abstract val todaysFocus: String
    abstract val pomodoros: String
    abstract val focusTime: String
    abstract val tasksDone: String
    abstract val timeline: String
    abstract val seeAll: String
    abstract val now: String
    abstract val interrupted: String
    abstract val finished: String
    abstract val noSessions: String
    abstract val noSessionsSub: String
    abstract val history: String
    abstract val last7: String
    abstract val dayTotal: String
    abstract val deleteSessionQ: String
    abstract val delete: String
    abstract val rename: String
    abstract val changePhaseQ: String
    abstract val changePhaseMsg: String
    abstract val change: String
    abstract val start: String
    abstract val resume: String
    abstract val pause: String
    abstract val skip: String
    abstract val stop: String
    abstract val reset: String
    abstract val ready: String
    abstract val paused: String
    abstract val running: String

    abstract val focusSettings: String
    abstract val durations: String
    abstract val focusLen: String
    abstract val shortLen: String
    abstract val longLen: String
    abstract val longEvery: String
    abstract fun everyN(n: Int): String
    abstract val presets: String
    abstract val presetNames: List<String>
    abstract val automation: String
    abstract val autoBreaks: String
    abstract val autoFocus: String
    abstract val autoComplete: String
    abstract val autoCompleteSub: String
    abstract val dailyGoal: String
    abstract val notifications: String
    abstract val notifyDone: String
    abstract val sound: String
    abstract val vibration: String
    abstract val ongoingNotif: String
    abstract val ongoingSub: String
    abstract val channelSettings: String
    abstract val exactAlarm: String
    abstract val exactAlarmSub: String
    abstract val behavior: String
    abstract val keepScreenOn: String
    abstract val logBreaks: String
    abstract val minLog: String
    abstract val lastSecondsHaptic: String
    abstract val dataSec: String
    abstract val clearHistory: String
    abstract val clearHistoryQ: String
    abstract val resetDefaults: String
    abstract val resetDefaultsQ: String
    abstract val goalReached: String

    abstract val chOngoing: String
    abstract fun chDone(sound: Boolean, vib: Boolean): String
    abstract fun endsAt(t: String): String
    abstract fun pausedLeft(t: String): String
    abstract val focusDoneTitle: String
    abstract val breakDoneTitle: String
    abstract fun nextUp(p: Phase, min: Int): String
    abstract fun nextStarted(p: Phase, min: Int): String
    abstract fun startPhase(p: Phase): String
}

object EnF : FocusStrings() {
    override val fa = false
    override fun dur(ms: Long): String { val m = (ms / 60000).toInt(); val h = m / 60; return when { m == 0 -> "<1m"; h > 0 && m % 60 > 0 -> "${h}h ${m % 60}m"; h > 0 -> "${h}h"; else -> "${m}m" } }
    override fun minutes(n: Int) = if (n == 1) "1 min" else "$n min"
    override fun phaseName(p: Phase) = when (p) { Phase.Focus -> "Focus"; Phase.Short -> "Short Break"; Phase.Long -> "Long Break" }
    override fun phaseShort(p: Phase) = when (p) { Phase.Focus -> "Focus"; Phase.Short -> "Short"; Phase.Long -> "Long" }
    override val tasks = "Tasks"
    override val focus = "Focus"
    override val newTask = "New Task"
    override val active = "Active"
    override val completed = "Completed"
    override val overdue = "Overdue"
    override val todaySec = "Today"
    override val tomorrow = "Tomorrow"
    override val upcoming = "Upcoming"
    override val someday = "Someday"
    override val noTasks = "No tasks yet"
    override val noTasksSub = "Add what you need to get done, then focus on it with the timer."
    override val noCompleted = "Nothing completed yet"
    override val clearCompleted = "Clear Completed"
    override val clearCompletedQ = "Delete all completed tasks?"
    override val editTask = "Task"
    override val taskTitle = "Title"
    override val notes = "Notes"
    override val notesHint = "Add notes"
    override val dueDate = "Due Date"
    override val none = "None"
    override val nextWeek = "Next Week"
    override val pickDate = "Pick a Date"
    override val priority = "Priority"
    override val priorities = listOf("None", "Low", "Medium", "High")
    override val estimate = "Estimated Pomodoros"
    override val estimateSub = "How many focus sessions this should take"
    override fun pomos(n: Int) = if (n == 1) "1 pomodoro" else "$n pomodoros"
    override val subtasks = "Subtasks"
    override val addSubtask = "Add Subtask"
    override val deleteTask = "Delete Task"
    override val deleteTaskQ = "Delete this task?"
    override val startFocus = "Start Focus"
    override val focusOnTask = "Focus on this task"
    override val totalFocus = "Total focus"
    override val sessionsTitle = "Sessions"
    override val color = "Color"
    override val workingOn = "WORKING ON"
    override val chooseTask = "Choose a task"
    override val noTask = "No task"
    override val customLabel = "Or type a label"
    override val customLabelHint = "e.g. Reading, Email…"
    override fun round(n: Int, of: Int) = "Round $n of $of"
    override val todaysFocus = "Today's Focus"
    override val pomodoros = "Pomodoros"
    override val focusTime = "Focus time"
    override val tasksDone = "Tasks done"
    override val timeline = "Timeline"
    override val seeAll = "See All"
    override val now = "Now"
    override val interrupted = "Interrupted"
    override val finished = "Completed"
    override val noSessions = "No sessions yet"
    override val noSessionsSub = "Start the timer and your day will be logged here, from start to finish."
    override val history = "Focus History"
    override val last7 = "Last 7 Days"
    override val dayTotal = "Total"
    override val deleteSessionQ = "Delete this session?"
    override val delete = "Delete"
    override val rename = "Rename"
    override val changePhaseQ = "Stop the current timer?"
    override val changePhaseMsg = "The time you've spent so far will still be saved to your timeline."
    override val change = "Stop"
    override val start = "Start"
    override val resume = "Resume"
    override val pause = "Pause"
    override val skip = "Skip"
    override val stop = "Stop"
    override val reset = "Reset"
    override val ready = "Ready"
    override val paused = "Paused"
    override val running = "In progress"
    override val focusSettings = "Timer Settings"
    override val durations = "DURATIONS"
    override val focusLen = "Focus"
    override val shortLen = "Short Break"
    override val longLen = "Long Break"
    override val longEvery = "Long Break After"
    override fun everyN(n: Int) = "$n focus sessions"
    override val presets = "PRESETS"
    override val presetNames = listOf("Classic", "Deep Work", "Sprint", "Flow")
    override val automation = "AUTOMATION"
    override val autoBreaks = "Auto-start Breaks"
    override val autoFocus = "Auto-start Focus"
    override val autoComplete = "Auto-complete Tasks"
    override val autoCompleteSub = "Mark a task done when its estimate is reached"
    override val dailyGoal = "Daily Goal"
    override val notifications = "NOTIFICATIONS"
    override val notifyDone = "Notify When Finished"
    override val sound = "Sound"
    override val vibration = "Vibration"
    override val ongoingNotif = "Live Timer Notification"
    override val ongoingSub = "Countdown with Pause / Skip in the notification shade"
    override val channelSettings = "Alert Sound & Style"
    override val exactAlarm = "Precise Alarms"
    override val exactAlarmSub = "Needed so the timer ends on the exact second"
    override val behavior = "BEHAVIOR"
    override val keepScreenOn = "Keep Screen On"
    override val logBreaks = "Log Breaks in Timeline"
    override val minLog = "Ignore Sessions Shorter Than"
    override val lastSecondsHaptic = "Haptic Countdown"
    override val dataSec = "DATA"
    override val clearHistory = "Clear Focus History"
    override val clearHistoryQ = "Delete all logged sessions?"
    override val resetDefaults = "Reset to Defaults"
    override val resetDefaultsQ = "Reset all timer settings?"
    override val goalReached = "Daily goal reached"
    override val chOngoing = "Running timer"
    override fun chDone(sound: Boolean, vib: Boolean) = "Timer finished" + when { sound && vib -> ""; sound -> " (sound)"; vib -> " (vibrate)"; else -> " (silent)" }
    override fun endsAt(t: String) = "Ends at $t"
    override fun pausedLeft(t: String) = "Paused · $t left"
    override val focusDoneTitle = "Focus session complete"
    override val breakDoneTitle = "Break's over"
    override fun nextUp(p: Phase, min: Int) = "Next: ${phaseName(p)} (${minutes(min)})"
    override fun nextStarted(p: Phase, min: Int) = "${phaseName(p)} started (${minutes(min)})"
    override fun startPhase(p: Phase) = "Start ${phaseName(p)}"
}

object FaF : FocusStrings() {
    override val fa = true
    override fun dur(ms: Long): String { val m = (ms / 60000).toInt(); val h = m / 60; return when { m == 0 -> "کمتر از ۱ دقیقه"; h > 0 && m % 60 > 0 -> "${n(h)} ساعت و ${n(m % 60)} دقیقه"; h > 0 -> "${n(h)} ساعت"; else -> "${n(m)} دقیقه" } }
    override fun minutes(n: Int) = "${n(n)} دقیقه"
    override fun phaseName(p: Phase) = when (p) { Phase.Focus -> "تمرکز"; Phase.Short -> "استراحت کوتاه"; Phase.Long -> "استراحت طولانی" }
    override fun phaseShort(p: Phase) = when (p) { Phase.Focus -> "تمرکز"; Phase.Short -> "کوتاه"; Phase.Long -> "طولانی" }
    override val tasks = "کارها"
    override val focus = "تمرکز"
    override val newTask = "کار جدید"
    override val active = "در جریان"
    override val completed = "انجام‌شده"
    override val overdue = "عقب‌افتاده"
    override val todaySec = "امروز"
    override val tomorrow = "فردا"
    override val upcoming = "پیش رو"
    override val someday = "بدون تاریخ"
    override val noTasks = "هنوز کاری نداری"
    override val noTasksSub = "کارهایی که باید انجام بدی رو اضافه کن و با تایمر روشون تمرکز کن."
    override val noCompleted = "هنوز کاری انجام نشده"
    override val clearCompleted = "پاک کردن انجام‌شده‌ها"
    override val clearCompletedQ = "همه‌ی کارهای انجام‌شده پاک بشن؟"
    override val editTask = "کار"
    override val taskTitle = "عنوان"
    override val notes = "یادداشت"
    override val notesHint = "یادداشت اضافه کن"
    override val dueDate = "موعد"
    override val none = "هیچ"
    override val nextWeek = "هفته‌ی بعد"
    override val pickDate = "انتخاب تاریخ"
    override val priority = "اولویت"
    override val priorities = listOf("هیچ", "کم", "متوسط", "زیاد")
    override val estimate = "تعداد پومودوروی تخمینی"
    override val estimateSub = "چند جلسه‌ی تمرکز برای این کار لازمه"
    override fun pomos(n: Int) = "${n(n)} پومودورو"
    override val subtasks = "زیرکارها"
    override val addSubtask = "افزودن زیرکار"
    override val deleteTask = "حذف کار"
    override val deleteTaskQ = "این کار حذف بشه؟"
    override val startFocus = "شروع تمرکز"
    override val focusOnTask = "روی این کار تمرکز کن"
    override val totalFocus = "مجموع تمرکز"
    override val sessionsTitle = "جلسه‌ها"
    override val color = "رنگ"
    override val workingOn = "در حال کار روی"
    override val chooseTask = "یک کار انتخاب کن"
    override val noTask = "بدون کار"
    override val customLabel = "یا یک عنوان بنویس"
    override val customLabelHint = "مثلاً مطالعه، ایمیل…"
    override fun round(n: Int, of: Int) = "دور ${n(n)} از ${n(of)}"
    override val todaysFocus = "تمرکز امروز"
    override val pomodoros = "پومودورو"
    override val focusTime = "زمان تمرکز"
    override val tasksDone = "کار انجام‌شده"
    override val timeline = "خط زمانی"
    override val seeAll = "همه"
    override val now = "الان"
    override val interrupted = "نیمه‌کاره"
    override val finished = "کامل"
    override val noSessions = "هنوز جلسه‌ای ثبت نشده"
    override val noSessionsSub = "تایمر رو شروع کن تا روزت از شروع تا پایان اینجا ثبت بشه."
    override val history = "تاریخچه‌ی تمرکز"
    override val last7 = "۷ روز اخیر"
    override val dayTotal = "مجموع"
    override val deleteSessionQ = "این جلسه حذف بشه؟"
    override val delete = "حذف"
    override val rename = "تغییر نام"
    override val changePhaseQ = "تایمر فعلی متوقف بشه؟"
    override val changePhaseMsg = "زمانی که تا الان گذشته در خط زمانی ذخیره می‌شه."
    override val change = "توقف"
    override val start = "شروع"
    override val resume = "ادامه"
    override val pause = "مکث"
    override val skip = "رد کردن"
    override val stop = "توقف"
    override val reset = "از نو"
    override val ready = "آماده"
    override val paused = "متوقف"
    override val running = "در جریان"
    override val focusSettings = "تنظیمات تایمر"
    override val durations = "مدت‌ها"
    override val focusLen = "تمرکز"
    override val shortLen = "استراحت کوتاه"
    override val longLen = "استراحت طولانی"
    override val longEvery = "استراحت طولانی بعد از"
    override fun everyN(n: Int) = "${n(n)} جلسه تمرکز"
    override val presets = "الگوهای آماده"
    override val presetNames = listOf("کلاسیک", "کار عمیق", "سریع", "جریان")
    override val automation = "خودکار"
    override val autoBreaks = "شروع خودکار استراحت"
    override val autoFocus = "شروع خودکار تمرکز"
    override val autoComplete = "تکمیل خودکار کارها"
    override val autoCompleteSub = "وقتی به تعداد تخمینی رسید، کار انجام‌شده بشه"
    override val dailyGoal = "هدف روزانه"
    override val notifications = "اعلان‌ها"
    override val notifyDone = "اعلان در پایان تایمر"
    override val sound = "صدا"
    override val vibration = "لرزش"
    override val ongoingNotif = "تایمر زنده در اعلان‌ها"
    override val ongoingSub = "شمارش معکوس با دکمه‌ی مکث و رد کردن در نوار اعلان"
    override val channelSettings = "صدا و سبک هشدار"
    override val exactAlarm = "هشدار دقیق"
    override val exactAlarmSub = "برای اینکه تایمر دقیقاً سر ثانیه تموم بشه"
    override val behavior = "رفتار"
    override val keepScreenOn = "روشن ماندن صفحه"
    override val logBreaks = "ثبت استراحت‌ها در خط زمانی"
    override val minLog = "نادیده گرفتن جلسه‌های کوتاه‌تر از"
    override val lastSecondsHaptic = "لرزش ثانیه‌های آخر"
    override val dataSec = "داده‌ها"
    override val clearHistory = "پاک کردن تاریخچه‌ی تمرکز"
    override val clearHistoryQ = "همه‌ی جلسه‌های ثبت‌شده پاک بشن؟"
    override val resetDefaults = "بازگشت به پیش‌فرض"
    override val resetDefaultsQ = "همه‌ی تنظیمات تایمر به حالت اول برگرده؟"
    override val goalReached = "به هدف امروز رسیدی"
    override val chOngoing = "تایمر در حال اجرا"
    override fun chDone(sound: Boolean, vib: Boolean) = "پایان تایمر" + when { sound && vib -> ""; sound -> " (صدا)"; vib -> " (لرزش)"; else -> " (بی‌صدا)" }
    override fun endsAt(t: String) = "پایان ساعت $t"
    override fun pausedLeft(t: String) = "متوقف · $t مانده"
    override val focusDoneTitle = "جلسه‌ی تمرکز تموم شد"
    override val breakDoneTitle = "استراحت تموم شد"
    override fun nextUp(p: Phase, min: Int) = "بعدی: ${phaseName(p)} (${minutes(min)})"
    override fun nextStarted(p: Phase, min: Int) = "${phaseName(p)} شروع شد (${minutes(min)})"
    override fun startPhase(p: Phase) = "شروع ${phaseName(p)}"
}

fun focusStrings(fa: Boolean): FocusStrings = if (fa) FaF else EnF
val Strings.f: FocusStrings get() = if (isFa) FaF else EnF
