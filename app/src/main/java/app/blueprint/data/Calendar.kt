package app.blueprint.data

import java.time.LocalDate

/** Gregorian <-> Jalali (Solar Hijri) conversion */
object Jalali {
    data class JDate(val year: Int, val month: Int, val day: Int)

    fun fromGregorian(date: LocalDate): JDate {
        val gy = date.year; val gm = date.monthValue; val gd = date.dayOfMonth
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) { jm = 1 + days / 31; jd = 1 + days % 31 } else { jm = 7 + (days - 186) / 30; jd = 1 + (days - 186) % 30 }
        return JDate(jy, jm, jd)
    }

    val monthsFa = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
}

fun String.toPersianDigits(): String = buildString {
    for (c in this@toPersianDigits) append(if (c in '0'..'9') '۰' + (c - '0') else if (c == '.') '٫' else c)
}
