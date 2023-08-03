package kr.hstar.commonutil

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 전달 날짜에 days 값을 더한 날짜를 획득
 */
public fun addDays(date: Date, days: Int): Date? {
    val cal = GregorianCalendar()
    cal.time = date
    cal.add(Calendar.DATE, days)
    return cal.time
}

/**
 * 전달 날짜에 시간과 분을 더한 날짜를 획득
 */
public fun addHoursAndMinutes(date: Date, timeStr: String): Date? {
    val cal = GregorianCalendar()
    cal.time = date
    if (timeStr.isNotEmpty() && timeStr.length == 5 && timeStr.substring(2, 3) == ":") {
        val hours: Int = Integer.parseInt(timeStr.substring(0, 2), 0)
        val minutes: Int = Integer.parseInt(timeStr.substring(3), 0)
        cal.add(Calendar.HOUR, hours)
        cal.add(Calendar.MINUTE, minutes)
    }
    return cal.time
}

/**
 * 전달 날짜에 hours 값을 더한 날짜를 획득
 */
private fun addHours(date: Date, hours: Int): Date? {
    val cal = GregorianCalendar()
    cal.time = date
    cal.add(Calendar.HOUR, hours)
    return cal.time
}

/**
 * 전달 날짜에 minutes 값을 더한 날짜를 획득
 */
public fun addMinutes(date: Date, minutes: Int): Date {
    return runCatching {
        val cal = GregorianCalendar()
        cal.time = date
        cal.add(Calendar.MINUTE, minutes)
        cal.time
    }.getOrDefault(Date())
}

/**
 * 전달 날짜에 seconds 값을 더한 날짜를 획득
 */
public fun addSeconds(date: Date, seconds: Int): Date? {
    val cal = GregorianCalendar()
    cal.time = date
    cal.add(Calendar.SECOND, seconds)
    return cal.time
}

/**
 * 전달 날짜에 milliseconds 값을 더한 날짜를 획득
 */
public fun addMilliseconds(date: Date, milliseconds: Int): Date? {
    val cal = GregorianCalendar()
    cal.time = date
    cal.add(Calendar.MILLISECOND, milliseconds)
    return cal.time
}

public fun getToday(): String {
    val onlyDate: LocalDate = LocalDate.now()
    return onlyDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}