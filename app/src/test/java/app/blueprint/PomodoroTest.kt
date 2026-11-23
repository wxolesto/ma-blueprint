package app.blueprint

import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.blueprint.data.AppData
import app.blueprint.data.Phase
import app.blueprint.data.PomoSettings
import app.blueprint.data.Repository
import app.blueprint.focus.Pomodoro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class PomodoroTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun expire() = Repository.update {
        val now = System.currentTimeMillis()
        it.copy(timer = it.timer.copy(endAt = now - 10, segStart = now - 25 * 60000))
    }

    @Test fun fullCycle() {
        Shadows.shadowOf(ctx).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        Repository.init(ctx)
        Repository.update { AppData(pomo = PomoSettings(longEvery = 2, autoBreaks = true)) }
        Pomodoro.setSubject(ctx, null, "Reading")
        Pomodoro.start(ctx)
        var d = Repository.data.value
        assertTrue(d.timer.running)
        assertTrue(kotlin.math.abs(d.timer.endAt - d.timer.segStart - 25 * 60000L) < 2000)

        // focus 1 finishes -> short break auto-starts
        expire(); Pomodoro.complete(ctx)
        d = Repository.data.value
        assertEquals(1, d.sessions.size)
        assertEquals("Reading", d.sessions[0].label)
        assertTrue(d.sessions[0].completed)
        assertEquals(Phase.Short, d.timer.phase)
        assertEquals(1, d.timer.cycle)
        assertTrue(d.timer.running)
        val nm = Shadows.shadowOf(ctx.getSystemService(NotificationManager::class.java))
        assertTrue(nm.allNotifications.isNotEmpty())

        // calling complete twice is a no-op
        Pomodoro.complete(ctx)
        assertEquals(1, Repository.data.value.sessions.size)

        // break finishes -> focus, not auto-started
        expire(); Pomodoro.complete(ctx)
        d = Repository.data.value
        assertEquals(Phase.Focus, d.timer.phase); assertFalse(d.timer.running)

        // second focus -> long break (every 2)
        Pomodoro.start(ctx); expire(); Pomodoro.complete(ctx)
        d = Repository.data.value
        assertEquals(Phase.Long, d.timer.phase); assertEquals(2, d.timer.cycle)

        // pause logs a partial segment, resume continues
        Repository.update { val n = System.currentTimeMillis(); it.copy(timer = it.timer.copy(segStart = n - 3 * 60000)) }
        Pomodoro.pause(ctx)
        d = Repository.data.value
        assertFalse(d.timer.running); assertTrue(d.timer.remainingMs > 0)
        assertFalse(d.sessions.last().completed)
        Pomodoro.start(ctx)
        assertTrue(Repository.data.value.timer.running)
        Pomodoro.reset(ctx)
        assertFalse(Repository.data.value.timer.running)
    }
}
