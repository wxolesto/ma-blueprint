package app.blueprint.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import app.blueprint.data.Jalali
import app.blueprint.data.toPersianDigits
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

abstract class Strings {
    abstract val isFa: Boolean
    abstract val appName: String
    abstract val today: String
    abstract val progress: String
    abstract val settings: String
    abstract fun dayOf(day: Int, total: Int): String
    abstract fun daysRemaining(n: Int): String
    abstract val todaysHabits: String
    abstract fun weekN(n: Int): String
    abstract fun thisWeek(done: Int, target: Int): String
    abstract val dayStreak: String
    abstract val bestStreak: String
    abstract val perfectDays: String
    abstract val avgCompletion: String
    abstract val checkIns: String
    abstract val daysElapsed: String
    abstract val dailyProgress: String
    abstract val completionRate: String
    abstract val habitInsights: String
    abstract val heatmap: String
    abstract val editHabit: String
    abstract val newHabit: String
    abstract val nameAndIcon: String
    abstract val habitName: String
    abstract val description: String
    abstract val descriptionHint: String
    abstract val color: String
    abstract val frequency: String
    abstract val scheduled: String
    abstract val perWeek: String
    abstract val activeDays: String
    abstract fun timesAWeek(n: Int): String
    abstract val timesPerDay: String
    abstract fun timesADay(n: Int): String
    abstract val reminder: String
    abstract val reminderTime: String
    abstract val removeHabit: String
    abstract val removeHabitQ: String
    abstract val removeHabitMsg: String
    abstract val cancel: String
    abstract val remove: String
    abstract val save: String
    abstract val done: String
    abstract val add: String
    abstract val daily: String
    abstract fun freqLabel(daily: Boolean, perWeek: Boolean, n: Int, days: Int): String
    abstract val yourBlueprint: String
    abstract val name: String
    abstract val length: String
    abstract val startDate: String
    abstract val habits: String
    abstract val addHabit: String
    abstract val appearance: String
    abstract val theme: String
    abstract val system: String
    abstract val light: String
    abstract val dark: String
    abstract val language: String
    abstract val haptics: String
    abstract val quotes: String
    abstract val celebrate: String
    abstract val data: String
    abstract val exportBackup: String
    abstract val importBackup: String
    abstract val importDone: String
    abstract val importFailed: String
    abstract val newBlueprint: String
    abstract val newBlueprintMsg: String
    abstract val pastBlueprints: String
    abstract val about: String
    abstract val version: String
    abstract val getStarted: String
    abstract val welcomeTitle: String
    abstract val welcomeSub: String
    abstract val welcomePoints: List<Pair<String, String>>
    abstract val nameYourBlueprint: String
    abstract val nameSub: String
    abstract val nameSuggestions: List<String>
    abstract val howLong: String
    abstract val howLongSub: String
    abstract fun nDays(n: Int): String
    abstract val custom: String
    abstract val whenStart: String
    abstract val tomorrow: String
    abstract val nextMonday: String
    abstract val pickHabits: String
    abstract val pickHabitsSub: String
    abstract val customHabit: String
    abstract val continueTxt: String
    abstract val back: String
    abstract val ready: String
    abstract val days: String
    abstract val start: String
    abstract val dayOneStarts: String
    abstract val quotesList: List<String>
    abstract val note: String
    abstract val notePlaceholder: String
    abstract val noteHint: String
    abstract val checkpoint: String
    abstract fun checkpointMsg(day: Int, total: Int): String
    abstract val allDone: String
    abstract val allDoneSub: String
    abstract val nothingToday: String
    abstract val complete: String
    abstract fun completeMsg(perfect: Int, total: Int): String
    abstract val startNew: String
    abstract val notStarted: String
    abstract fun startsIn(n: Int): String
    abstract val active: String
    abstract val finished: String
    abstract val delete: String
    abstract val open: String
    abstract val icon: String
    abstract val reorder: String
    abstract val markDone: String
    abstract val reminderChannel: String
    abstract fun reminderText(name: String): String
    abstract val notifPermission: String
    abstract val streak: String
    abstract val best: String
    abstract val rate: String
    abstract val total: String
    abstract val swipeHint: String
    abstract val less: String
    abstract val more: String
    abstract val off: String

    abstract fun num(n: Int): String
    abstract fun percent(f: Float): String
    abstract fun weekdayShort(isoDow: Int): String
    abstract fun weekdayLetter(isoDow: Int): String
    abstract fun dateShort(day: Long): String
    abstract fun dateLong(day: Long): String
    abstract fun dayOfMonth(day: Long): String
    /** ISO day of week the week starts with */
    abstract val weekStart: Int
    abstract fun time(minutes: Int): String
}

object En : Strings() {
    override val isFa = false
    override val appName = "Blueprint"
    override val today = "Today"
    override val progress = "Progress"
    override val settings = "Settings"
    override fun dayOf(day: Int, total: Int) = "Day $day of $total"
    override fun daysRemaining(n: Int) = if (n == 1) "1 day remaining" else "$n days remaining"
    override val todaysHabits = "Today's Habits"
    override fun weekN(n: Int) = "Week $n"
    override fun thisWeek(done: Int, target: Int) = "$done/$target this week"
    override val dayStreak = "Day Streak"
    override val bestStreak = "Best Streak"
    override val perfectDays = "Perfect Days"
    override val avgCompletion = "Avg Completion"
    override val checkIns = "Check-ins"
    override val daysElapsed = "Days Elapsed"
    override val dailyProgress = "Daily Progress"
    override val completionRate = "Daily Completion Rate"
    override val habitInsights = "Habit Insights"
    override val heatmap = "Consistency"
    override val editHabit = "Edit Habit"
    override val newHabit = "New Habit"
    override val nameAndIcon = "NAME & ICON"
    override val habitName = "Habit name"
    override val description = "DESCRIPTION"
    override val descriptionHint = "Why does this matter? (optional)"
    override val color = "COLOR"
    override val frequency = "FREQUENCY"
    override val scheduled = "Scheduled"
    override val perWeek = "Per Week"
    override val activeDays = "ACTIVE DAYS"
    override fun timesAWeek(n: Int) = "$n× per week"
    override val timesPerDay = "TIMES PER DAY"
    override fun timesADay(n: Int) = if (n == 1) "Once a day" else "$n× a day"
    override val reminder = "REMINDER"
    override val reminderTime = "Remind me"
    override val removeHabit = "Remove Habit"
    override val removeHabitQ = "Remove this habit?"
    override val removeHabitMsg = "Its history will be kept in your backup but it will disappear from your Blueprint."
    override val cancel = "Cancel"
    override val remove = "Remove"
    override val save = "Save"
    override val done = "Done"
    override val add = "Add"
    override val daily = "Daily"
    override fun freqLabel(daily: Boolean, perWeek: Boolean, n: Int, days: Int) = when {
        perWeek -> "$n× per week"; daily -> "Daily"; else -> "$days days a week"
    }
    override val yourBlueprint = "YOUR BLUEPRINT"
    override val name = "Name"
    override val length = "Length"
    override val startDate = "Start date"
    override val habits = "HABITS"
    override val addHabit = "Add Habit"
    override val appearance = "APPEARANCE"
    override val theme = "Theme"
    override val system = "System"
    override val light = "Light"
    override val dark = "Dark"
    override val language = "Language"
    override val haptics = "Haptic Feedback"
    override val quotes = "Motivation Quotes"
    override val celebrate = "Celebrations"
    override val data = "DATA"
    override val exportBackup = "Export Backup"
    override val importBackup = "Import Backup"
    override val importDone = "Backup restored"
    override val importFailed = "That file is not a valid backup"
    override val newBlueprint = "Start a New Blueprint"
    override val newBlueprintMsg = "Your current Blueprint will be archived. You can reopen it any time from Past Blueprints."
    override val pastBlueprints = "Past Blueprints"
    override val about = "ABOUT"
    override val version = "Version"
    override val getStarted = "Get Started"
    override val welcomeTitle = "Blueprint"
    override val welcomeSub = "Design the next chapter of your life, one day at a time."
    override val welcomePoints = listOf(
        "Build a plan" to "Pick a length, choose your habits, commit.",
        "Check in daily" to "One tap a day keeps the streak alive.",
        "Watch it compound" to "Streaks, charts and perfect days.",
    )
    override val nameYourBlueprint = "Name your Blueprint"
    override val nameSub = "Give this chapter a name you'll want to live up to."
    override val nameSuggestions = listOf("Lock In", "New Me", "Summer Arc", "Deep Work Month", "75 Soft", "Glow Up")
    override val howLong = "How long?"
    override val howLongSub = "Research says it takes about 66 days to form a habit. Start where you can commit."
    override fun nDays(n: Int) = "$n days"
    override val custom = "Custom"
    override val whenStart = "STARTS"
    override val tomorrow = "Tomorrow"
    override val nextMonday = "Next Monday"
    override val pickHabits = "Choose your habits"
    override val pickHabitsSub = "Start with 3–5. You can always change them later."
    override val customHabit = "Create Custom Habit"
    override val continueTxt = "Continue"
    override val back = "Back"
    override val ready = "Your Blueprint is ready"
    override val days = "DAYS"
    override val start = "START"
    override val dayOneStarts = "Day 1 starts now"
    override val quotesList = listOf(
        "We are what we repeatedly do. Excellence, then, is not an act, but a habit. — Aristotle",
        "You do not rise to the level of your goals. You fall to the level of your systems. — James Clear",
        "Small daily improvements over time lead to stunning results. — Robin Sharma",
        "Discipline is choosing between what you want now and what you want most.",
        "The secret of getting ahead is getting started. — Mark Twain",
        "Motivation gets you going, but habit gets you there. — Zig Ziglar",
        "Success is the sum of small efforts, repeated day in and day out. — Robert Collier",
        "Don't break the chain.",
    )
    override val note = "Daily Note"
    override val notePlaceholder = "How did today go?"
    override val noteHint = "Tap to reflect on your day"
    override val checkpoint = "CHECKPOINT"
    override fun checkpointMsg(day: Int, total: Int) = when {
        day == 1 -> "Day one. The hardest part is showing up — and you're here."
        day <= 7 -> "First week. Momentum is being built right now."
        day * 2 < total -> "You're building something real. Keep stacking days."
        day * 2 == total || day * 2 == total + 1 -> "Halfway there. The person who started this would be proud."
        day >= total -> "Final day. Finish what you started."
        total - day <= 7 -> "Final stretch. ${total - day} days left — don't let up."
        else -> "Past the halfway mark. This is who you are now."
    }
    override val allDone = "All done for today"
    override val allDoneSub = "Every habit checked. See you tomorrow."
    override val nothingToday = "Nothing scheduled today. Rest is part of the plan."
    override val complete = "Blueprint complete"
    override fun completeMsg(perfect: Int, total: Int) = "You finished all $total days with $perfect perfect days."
    override val startNew = "Start New Blueprint"
    override val notStarted = "Not started yet"
    override fun startsIn(n: Int) = if (n == 1) "Starts tomorrow" else "Starts in $n days"
    override val active = "Active"
    override val finished = "Finished"
    override val delete = "Delete"
    override val open = "Open"
    override val icon = "Icon"
    override val reorder = "Reorder"
    override val markDone = "Mark done"
    override val reminderChannel = "Habit reminders"
    override fun reminderText(name: String) = "Time for $name"
    override val notifPermission = "Allow notifications to get reminders"
    override val streak = "Streak"
    override val best = "Best"
    override val rate = "Rate"
    override val total = "Total"
    override val swipeHint = "Swipe for more"
    override val less = "Less"
    override val more = "More"
    override val off = "Off"
    override fun num(n: Int) = n.toString()
    override fun percent(f: Float) = "${Math.round(f * 100)}%"
    override fun weekdayShort(isoDow: Int) = java.time.DayOfWeek.of(isoDow).getDisplayName(TextStyle.SHORT, Locale.US)
    override fun weekdayLetter(isoDow: Int) = "MTWTFSS"[isoDow - 1].toString()
    override fun dateShort(day: Long): String { val d = LocalDate.ofEpochDay(day); return "${d.month.getDisplayName(TextStyle.SHORT, Locale.US)} ${d.dayOfMonth}" }
    override fun dateLong(day: Long): String { val d = LocalDate.ofEpochDay(day); return "${d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)}, ${d.month.getDisplayName(TextStyle.FULL, Locale.US)} ${d.dayOfMonth}" }
    override fun dayOfMonth(day: Long) = LocalDate.ofEpochDay(day).dayOfMonth.toString()
    override val weekStart = 7
    override fun time(minutes: Int): String {
        val h = minutes / 60; val m = minutes % 60
        val h12 = if (h % 12 == 0) 12 else h % 12
        return "%d:%02d %s".format(h12, m, if (h < 12) "AM" else "PM")
    }
}

object Fa : Strings() {
    override val isFa = true
    override val appName = "بلوپرینت"
    override val today = "امروز"
    override val progress = "پیشرفت"
    override val settings = "تنظیمات"
    override fun dayOf(day: Int, total: Int) = "روز ${num(day)} از ${num(total)}"
    override fun daysRemaining(n: Int) = "${num(n)} روز باقی مانده"
    override val todaysHabits = "عادت‌های امروز"
    override fun weekN(n: Int) = "هفته ${num(n)}"
    override fun thisWeek(done: Int, target: Int) = "${num(done)}/${num(target)} این هفته"
    override val dayStreak = "روز پیاپی"
    override val bestStreak = "بهترین رکورد"
    override val perfectDays = "روزهای کامل"
    override val avgCompletion = "میانگین انجام"
    override val checkIns = "ثبت‌ها"
    override val daysElapsed = "روز گذشته"
    override val dailyProgress = "پیشرفت روزانه"
    override val completionRate = "نرخ انجام روزانه"
    override val habitInsights = "تحلیل عادت‌ها"
    override val heatmap = "پیوستگی"
    override val editHabit = "ویرایش عادت"
    override val newHabit = "عادت جدید"
    override val nameAndIcon = "نام و آیکون"
    override val habitName = "نام عادت"
    override val description = "توضیحات"
    override val descriptionHint = "چرا برایت مهم است؟ (اختیاری)"
    override val color = "رنگ"
    override val frequency = "تکرار"
    override val scheduled = "زمان‌بندی‌شده"
    override val perWeek = "هفتگی"
    override val activeDays = "روزهای فعال"
    override fun timesAWeek(n: Int) = "${num(n)} بار در هفته"
    override val timesPerDay = "دفعات در روز"
    override fun timesADay(n: Int) = if (n == 1) "یک بار در روز" else "${num(n)} بار در روز"
    override val reminder = "یادآور"
    override val reminderTime = "یادآوری کن"
    override val removeHabit = "حذف عادت"
    override val removeHabitQ = "این عادت حذف شود؟"
    override val removeHabitMsg = "سابقه‌اش در نسخه پشتیبان می‌ماند اما از بلوپرینت تو حذف می‌شود."
    override val cancel = "انصراف"
    override val remove = "حذف"
    override val save = "ذخیره"
    override val done = "تمام"
    override val add = "افزودن"
    override val daily = "روزانه"
    override fun freqLabel(daily: Boolean, perWeek: Boolean, n: Int, days: Int) = when {
        perWeek -> "${num(n)} بار در هفته"; daily -> "روزانه"; else -> "${num(days)} روز در هفته"
    }
    override val yourBlueprint = "بلوپرینت تو"
    override val name = "نام"
    override val length = "مدت"
    override val startDate = "تاریخ شروع"
    override val habits = "عادت‌ها"
    override val addHabit = "افزودن عادت"
    override val appearance = "ظاهر"
    override val theme = "پوسته"
    override val system = "سیستم"
    override val light = "روشن"
    override val dark = "تیره"
    override val language = "زبان"
    override val haptics = "لرزش لمسی"
    override val quotes = "جملات انگیزشی"
    override val celebrate = "جشن موفقیت"
    override val data = "داده‌ها"
    override val exportBackup = "خروجی پشتیبان"
    override val importBackup = "بازیابی پشتیبان"
    override val importDone = "پشتیبان بازیابی شد"
    override val importFailed = "این فایل پشتیبان معتبر نیست"
    override val newBlueprint = "شروع بلوپرینت جدید"
    override val newBlueprintMsg = "بلوپرینت فعلی بایگانی می‌شود و هر وقت بخواهی از بخش بلوپرینت‌های قبلی قابل باز کردن است."
    override val pastBlueprints = "بلوپرینت‌های قبلی"
    override val about = "درباره"
    override val version = "نسخه"
    override val getStarted = "شروع کنیم"
    override val welcomeTitle = "بلوپرینت"
    override val welcomeSub = "فصل بعدی زندگی‌ات را طراحی کن، روز به روز."
    override val welcomePoints = listOf(
        "یک برنامه بساز" to "مدت را انتخاب کن، عادت‌ها را بچین، متعهد شو.",
        "هر روز ثبت کن" to "روزی یک لمس، زنجیره را زنده نگه می‌دارد.",
        "رشدش را ببین" to "رکوردها، نمودارها و روزهای کامل.",
    )
    override val nameYourBlueprint = "اسم بلوپرینتت را بگذار"
    override val nameSub = "اسمی بگذار که بخواهی لایقش باشی."
    override val nameSuggestions = listOf("تمرکز کامل", "منِ جدید", "فصل تابستان", "ماه کار عمیق", "شروع دوباره", "نسخه بهتر")
    override val howLong = "چه مدت؟"
    override val howLongSub = "تحقیقات می‌گوید شکل‌گیری یک عادت حدود ۶۶ روز طول می‌کشد. از جایی شروع کن که می‌توانی پایبند بمانی."
    override fun nDays(n: Int) = "${num(n)} روز"
    override val custom = "دلخواه"
    override val whenStart = "شروع"
    override val tomorrow = "فردا"
    override val nextMonday = "شنبه آینده"
    override val pickHabits = "عادت‌هایت را انتخاب کن"
    override val pickHabitsSub = "با ۳ تا ۵ عادت شروع کن. بعداً همیشه قابل تغییرند."
    override val customHabit = "ساخت عادت دلخواه"
    override val continueTxt = "ادامه"
    override val back = "بازگشت"
    override val ready = "بلوپرینت تو آماده است"
    override val days = "روز"
    override val start = "شروع"
    override val dayOneStarts = "روز اول از همین حالا"
    override val quotesList = listOf(
        "ما همان چیزی هستیم که بارها و بارها انجام می‌دهیم. — ارسطو",
        "تو به سطح اهدافت نمی‌رسی؛ به سطح سیستم‌هایت سقوط می‌کنی. — جیمز کلییر",
        "پیشرفت‌های کوچک روزانه، در طول زمان نتایج شگفت‌انگیزی می‌سازند.",
        "انضباط یعنی انتخاب بین آنچه الان می‌خواهی و آنچه بیشتر از همه می‌خواهی.",
        "راز جلو افتادن، شروع کردن است. — مارک تواین",
        "انگیزه تو را راه می‌اندازد، اما عادت تو را به مقصد می‌رساند.",
        "موفقیت حاصل جمع تلاش‌های کوچکی است که هر روز تکرار می‌شوند.",
        "زنجیره را نشکن.",
    )
    override val note = "یادداشت روز"
    override val notePlaceholder = "امروز چطور گذشت؟"
    override val noteHint = "برای مرور روزت لمس کن"
    override val checkpoint = "ایستگاه"
    override fun checkpointMsg(day: Int, total: Int) = when {
        day == 1 -> "روز اول. سخت‌ترین قسمت حاضر شدن است — و تو اینجایی."
        day <= 7 -> "هفته اول. همین حالا داری شتاب می‌سازی."
        day * 2 < total -> "داری چیز واقعی می‌سازی. روزها را روی هم بچین."
        day * 2 == total || day * 2 == total + 1 -> "نیمی از راه. کسی که این را شروع کرد به تو افتخار می‌کند."
        day >= total -> "روز آخر. کاری را که شروع کردی تمام کن."
        total - day <= 7 -> "مسیر پایانی. ${num(total - day)} روز مانده — کوتاه نیا."
        else -> "از نیمه گذشتی. حالا این خودِ تویی."
    }
    override val allDone = "کارهای امروز تمام شد"
    override val allDoneSub = "همه عادت‌ها انجام شد. فردا می‌بینمت."
    override val nothingToday = "امروز برنامه‌ای نیست. استراحت هم بخشی از برنامه است."
    override val complete = "بلوپرینت کامل شد"
    override fun completeMsg(perfect: Int, total: Int) = "هر ${num(total)} روز را تمام کردی، با ${num(perfect)} روز کامل."
    override val startNew = "شروع بلوپرینت جدید"
    override val notStarted = "هنوز شروع نشده"
    override fun startsIn(n: Int) = if (n == 1) "از فردا شروع می‌شود" else "${num(n)} روز دیگر شروع می‌شود"
    override val active = "فعال"
    override val finished = "تمام‌شده"
    override val delete = "حذف"
    override val open = "باز کردن"
    override val icon = "آیکون"
    override val reorder = "مرتب‌سازی"
    override val markDone = "انجام شد"
    override val reminderChannel = "یادآور عادت‌ها"
    override fun reminderText(name: String) = "وقت $name است"
    override val notifPermission = "برای یادآورها اجازه اعلان بده"
    override val streak = "پیاپی"
    override val best = "بهترین"
    override val rate = "نرخ"
    override val total = "کل"
    override val swipeHint = "برای بیشتر بکش"
    override val less = "کمتر"
    override val more = "بیشتر"
    override val off = "خاموش"
    override fun num(n: Int) = n.toString().toPersianDigits()
    override fun percent(f: Float) = "٪" + Math.round(f * 100).toString().toPersianDigits()
    private val wd = listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
    private val wl = listOf("د", "س", "چ", "پ", "ج", "ش", "ی")
    override fun weekdayShort(isoDow: Int) = wl[isoDow - 1]
    override fun weekdayLetter(isoDow: Int) = wl[isoDow - 1]
    override fun dateShort(day: Long): String { val j = Jalali.fromGregorian(LocalDate.ofEpochDay(day)); return "${num(j.day)} ${Jalali.monthsFa[j.month - 1]}" }
    override fun dateLong(day: Long): String {
        val d = LocalDate.ofEpochDay(day); val j = Jalali.fromGregorian(d)
        return "${wd[d.dayOfWeek.value - 1]}، ${num(j.day)} ${Jalali.monthsFa[j.month - 1]}"
    }
    override fun dayOfMonth(day: Long) = num(Jalali.fromGregorian(LocalDate.ofEpochDay(day)).day)
    override val weekStart = 6
    override fun time(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60).toPersianDigits()
}

val LocalStrings = staticCompositionLocalOf<Strings> { En }
