package app.blueprint.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val all: List<Pair<String, ImageVector>> = listOf(
        "workout" to Icons.Rounded.FitnessCenter,
        "run" to Icons.Rounded.DirectionsRun,
        "walk" to Icons.Rounded.DirectionsWalk,
        "bike" to Icons.Rounded.DirectionsBike,
        "swim" to Icons.Rounded.Pool,
        "hike" to Icons.Rounded.Hiking,
        "mobility" to Icons.Rounded.AccessibilityNew,
        "meditate" to Icons.Rounded.SelfImprovement,
        "spa" to Icons.Rounded.Spa,
        "sun" to Icons.Rounded.WbSunny,
        "sleep" to Icons.Rounded.Bedtime,
        "alarm" to Icons.Rounded.Alarm,
        "water" to Icons.Rounded.WaterDrop,
        "food" to Icons.Rounded.Restaurant,
        "nofood" to Icons.Rounded.NoFood,
        "coffee" to Icons.Rounded.LocalCafe,
        "nosmoke" to Icons.Rounded.SmokeFree,
        "pill" to Icons.Rounded.Medication,
        "read" to Icons.Rounded.MenuBook,
        "journal" to Icons.Rounded.EditNote,
        "study" to Icons.Rounded.School,
        "language" to Icons.Rounded.Translate,
        "code" to Icons.Rounded.Code,
        "work" to Icons.Rounded.Work,
        "idea" to Icons.Rounded.Lightbulb,
        "music" to Icons.Rounded.MusicNote,
        "art" to Icons.Rounded.Brush,
        "camera" to Icons.Rounded.PhotoCamera,
        "phone" to Icons.Rounded.PhonelinkOff,
        "clean" to Icons.Rounded.CleaningServices,
        "plant" to Icons.Rounded.LocalFlorist,
        "eco" to Icons.Rounded.Eco,
        "pet" to Icons.Rounded.Pets,
        "money" to Icons.Rounded.Savings,
        "heart" to Icons.Rounded.Favorite,
        "mind" to Icons.Rounded.Psychology,
        "mood" to Icons.Rounded.Mood,
        "people" to Icons.Rounded.People,
        "call" to Icons.Rounded.Call,
        "pray" to Icons.Rounded.VolunteerActivism,
        "timer" to Icons.Rounded.Timer,
        "star" to Icons.Rounded.Star,
        "bolt" to Icons.Rounded.Bolt,
        "fire" to Icons.Rounded.LocalFireDepartment,
        "tennis" to Icons.Rounded.SportsTennis,
        "soccer" to Icons.Rounded.SportsSoccer,
        "basket" to Icons.Rounded.SportsBasketball,
        "game" to Icons.Rounded.SportsEsports,
    )
    private val map = all.toMap()
    fun get(key: String): ImageVector = map[key] ?: Icons.Rounded.Star
}
