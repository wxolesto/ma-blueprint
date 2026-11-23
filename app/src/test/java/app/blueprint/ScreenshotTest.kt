package app.blueprint

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.blueprint.data.AppData
import app.blueprint.data.Blueprint
import app.blueprint.data.Frequency
import app.blueprint.data.Habit
import app.blueprint.data.Repository
import app.blueprint.data.Settings
import app.blueprint.data.ThemeMode
import app.blueprint.data.Todo
import app.blueprint.data.SubTask
import app.blueprint.data.FocusSession
import app.blueprint.data.Phase
import app.blueprint.data.TimerState
import app.blueprint.ui.BlueprintRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun demo(theme: ThemeMode = ThemeMode.Dark, lang: String = "en", empty: Boolean = false): AppData {
        if (empty) return AppData(settings = Settings(theme = theme, language = lang))
        val today = LocalDate.now().toEpochDay()
        val fa = lang == "fa"
        val habits = listOf(
            Habit("h1", if (fa) "ورزش" else "Workout", "workout", 0, frequency = Frequency.PerWeek, perWeek = 4),
            Habit("h2", if (fa) "مدیتیشن" else "Meditate", "meditate", 3, description = if (fa) "۱۰ دقیقه تنفس" else "10 minutes of breathing"),
            Habit("h3", if (fa) "نور صبحگاهی" else "Morning Sunlight", "sun", 6),
            Habit("h4", if (fa) "خواندن ۲۰ صفحه" else "Read 20 pages", "read", 2),
            Habit("h5", if (fa) "نوشیدن آب" else "Drink Water", "water", 5, timesPerDay = 8),
        )
        val start = today - 14
        val bp = Blueprint("b1", if (fa) "تمرکز کامل" else "Summer Lock In", start, 30, habits)
        val r = Random(7)
        val comp = habits.associate { h ->
            h.id to (start until today).mapNotNull { d -> if (r.nextFloat() < 0.78f) d to (if (h.timesPerDay > 1) h.timesPerDay else 1) else null }.toMap() +
                (if (h.id == "h2" || h.id == "h5") mapOf(today to if (h.id == "h5") 3 else 1) else emptyMap())
        }
        return withFocus(AppData(blueprints = listOf(bp), activeId = "b1", completions = comp, settings = Settings(theme = theme, language = lang)), fa)
    }

    private fun withFocus(d: AppData, fa: Boolean): AppData {
        val today = LocalDate.now()
        val td = today.toEpochDay()
        fun at(h: Int, m: Int) = today.atTime(h, m).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todos = listOf(
            Todo("t1", if (fa) "نوشتن گزارش پروژه" else "Write project report", priority = 3, dueDay = td, estimate = 4, color = 7,
                subtasks = listOf(SubTask("s1", "Outline", true), SubTask("s2", "Draft", false), SubTask("s3", "Review", false)), notes = "x"),
            Todo("t2", if (fa) "مطالعه فصل ۵ کتاب" else "Study chapter 5", priority = 2, dueDay = td, estimate = 3, color = 3),
            Todo("t3", if (fa) "پاسخ به ایمیل‌ها" else "Reply to emails", dueDay = td - 1, color = 1),
            Todo("t4", if (fa) "طراحی صفحه‌ی اصلی اپ" else "Design app home screen", priority = 1, dueDay = td + 1, estimate = 6, color = 5),
            Todo("t5", if (fa) "خرید هفتگی" else "Weekly groceries", dueDay = td + 4, color = 0),
            Todo("t6", if (fa) "یادگیری Kotlin Flow" else "Learn Kotlin Flow", estimate = 2, color = 8),
            Todo("t7", if (fa) "تماس با بانک" else "Call the bank", done = true, doneAt = at(10, 5), color = 2),
        )
        val ss = listOf(
            FocusSession("a", at(8, 30), at(8, 55), Phase.Focus, "t1", todos[0].title, true),
            FocusSession("b", at(8, 55), at(9, 0), Phase.Short, null, "", true),
            FocusSession("c", at(9, 0), at(9, 25), Phase.Focus, "t1", todos[0].title, true),
            FocusSession("d", at(9, 25), at(9, 30), Phase.Short, null, "", true),
            FocusSession("e", at(9, 30), at(9, 42), Phase.Focus, "t2", todos[1].title, false),
            FocusSession("f", at(10, 10), at(10, 35), Phase.Focus, null, if (fa) "مطالعه" else "Reading", true),
        ) + (1..6).flatMap { k -> (0 until (k * 3) % 7 + 1).map { j ->
            val st = today.minusDays(k.toLong()).atTime(9 + j, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            FocusSession("o$k$j", st, st + 25 * 60000, Phase.Focus, "t2", todos[1].title, true) } }
        return d.copy(todos = todos, sessions = ss, timer = TimerState(phase = Phase.Focus, cycle = 2, todoId = "t1", remainingMs = 0))
    }

    private fun show(d: AppData) {
        Repository.init(rule.activity)
        Repository.update { d }
        rule.setContent { BlueprintRoot {} }
        rule.mainClock.advanceTimeBy(2500)
        rule.waitForIdle()
    }

    private fun snap(name: String) {
        rule.mainClock.advanceTimeBy(2500)
        rule.onRoot().captureRoboImage("screenshots/$name.png")
    }

    @Test fun todayDark() { show(demo()); snap("01_today_dark") }
    @Test fun todayLight() { show(demo(ThemeMode.Light)); snap("02_today_light") }
    @Test fun todayFa() { show(demo(lang = "fa")); snap("03_today_fa") }
    @Test fun progress() {
        show(demo()); rule.onAllNodesWithText("Progress")[0].performClick(); snap("04_progress")
    }
    @Test fun progressLight() {
        show(demo(ThemeMode.Light)); rule.onAllNodesWithText("Progress")[0].performClick(); snap("05_progress_light")
    }
    @Test fun settings() {
        show(demo()); rule.onAllNodesWithText("Settings")[0].performClick(); snap("06_settings")
    }
    @Test fun editHabit() {
        show(demo()); rule.onNodeWithText("Meditate").performTouchInput { longClick() }; snap("07_edit_habit")
    }
    @Test fun onboarding() { show(demo(empty = true)); snap("08_welcome") }
    private fun tap(t: String) { rule.onAllNodesWithText(t, useUnmergedTree = true)[0].performClick(); rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }
    @Test fun onboardingHabits() {
        show(demo(empty = true))
        tap("Get Started"); snap("08b_name")
        tap("New Me"); tap("Continue")
        snap("09_length")
        tap("Continue")
        tap("Meditate"); tap("Journal"); tap("Workout")
        snap("10_habits")
        tap("Continue"); snap("11_ready")
    }
    @Test fun settingsFaLight() {
        show(demo(ThemeMode.Light, "fa")); rule.onAllNodesWithText("تنظیمات")[0].performClick(); snap("12_settings_fa_light")
    }

    private fun tab(i: Int) { app.blueprint.ui.TabBus.pending.value = i; rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }
    @Test fun tasks() { show(demo()); tab(1); snap("20_tasks") }
    @Test fun tasksLightFa() { show(demo(ThemeMode.Light, "fa")); tab(1); snap("21_tasks_fa_light") }
    @Test fun focusIdle() { show(demo()); tab(2); snap("22_focus") }
    @Test fun focusRunning() {
        val d = demo()
        val now = System.currentTimeMillis()
        rule.mainClock.autoAdvance = false
        show(d.copy(timer = d.timer.copy(running = true, endAt = now + 17 * 60000 + 23000, segStart = now - 7 * 60000)))
        tab(2); snap("23_focus_running")
        rule.onRoot().performTouchInput { swipeUp(startY = bottom * 0.8f, endY = bottom * 0.1f) }
        snap("24_focus_timeline")
    }
    @Test fun focusFa() { show(demo(lang = "fa")); tab(2); snap("25_focus_fa") }
    @Test fun focusLight() { show(demo(ThemeMode.Light)); tab(2); snap("26_focus_light") }
    @Test fun editTodo() { show(demo()); tab(1); rule.onNodeWithText("Write project report").performClick(); snap("27_edit_todo") }
    @Test fun focusSettings() { show(demo()); tab(2); rule.onAllNodesWithContentDescription("settings_btn").fetchSemanticsNodes(); tab(4)
        rule.onNodeWithText("Timer Settings").performClick(); snap("28_focus_settings") }
    @Test fun focusHistory() { show(demo()); tab(2); rule.onRoot().performTouchInput { swipeUp(startY = bottom * 0.8f, endY = bottom * 0.1f) }; rule.mainClock.advanceTimeBy(1500); tap("See All"); snap("29_history") }
    @Test fun subjectSheet() { show(demo()); tab(2); rule.onNodeWithText("Write project report").performClick(); snap("30_subject_sheet") }
}
