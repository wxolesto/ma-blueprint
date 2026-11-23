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
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.blueprint.data.AppData
import app.blueprint.data.Blueprint
import app.blueprint.data.Frequency
import app.blueprint.data.Habit
import app.blueprint.data.Repository
import app.blueprint.data.Settings
import app.blueprint.data.ThemeMode
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
        return AppData(blueprints = listOf(bp), activeId = "b1", completions = comp, settings = Settings(theme = theme, language = lang))
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
}
