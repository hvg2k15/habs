package com.habs.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class Habit(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val frequency: Frequency,
    /**
     * When set, this habit is scheduled on those ISO weekdays (Mon=bit0 … Sun=bit6).
     * Used when [repeatIntervalDays] is null; takes precedence over [frequency] for scheduling.
     */
    val weeklyDayMask: Int? = null,
    /** When >= 2, habit repeats every N calendar days from [repeatAnchorDateKey] (or creation date). */
    val repeatIntervalDays: Int? = null,
    /** ISO local date `yyyy-MM-dd` anchor for every-N-days repeat; null uses [createdAt] date in local zone. */
    val repeatAnchorDateKey: String? = null,
    /** Preferred time of day to perform the habit (also used for Google Calendar event time). */
    val executionTime: LocalTime? = null,
    val calendarSynced: Boolean = false,
    val calendarEventId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class HabitCompletion(
    val id: Long = 0,
    val habitId: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val dateKey: String  // "yyyy-MM-dd"
)

data class HabitWithCompletion(
    val habit: Habit,
    val completedToday: Boolean,
    val currentStreak: Int
)

data class HabitStats(
    val habit: Habit,
    val completionRate: Float,          // 0.0 - 1.0 (scheduled days in period only)
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val monthlyRates: Map<String, Float>,  // "yyyy-MM" -> rate
    val dailyCompletions: Map<String, Boolean>,  // "yyyy-MM-dd" -> done
    /** Scheduled habit days from the stats range start through today */
    val scheduledDaysInPeriod: Int,
    /** Scheduled days in that range with no check-in */
    val missedScheduledDays: Int
)

data class OverallStats(
    val averageCompletionRate: Float,
    val perfectDays: Int,
    val totalCheckIns: Int,
    val bestStreak: Int,
    val bestStreakHabitName: String,
    val monthlyOverview: List<MonthlyOverview>
)

data class MonthlyOverview(
    val monthKey: String,  // "yyyy-MM"
    val completionRate: Float,
    val perfectDays: Int,
    val totalCheckIns: Int
)

enum class Frequency {
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    /** Monday, Wednesday, Friday */
    THREE_PER_WEEK,
    /** Monday and Wednesday */
    MON_WED,
    /** Tuesday and Thursday */
    TUE_THU,
    /** Monday through Thursday */
    MON_THU,
    /** Monday, Tuesday, Thursday, Friday */
    MON_TUE_THU_FRI,
    /** Use [Habit.weeklyDayMask] for which weekdays (single or multiple). */
    CUSTOM;

    fun isScheduledFor(day: DayOfWeek): Boolean = when (this) {
        DAILY -> true
        WEEKDAYS -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
        WEEKENDS -> day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
        THREE_PER_WEEK ->
            day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.FRIDAY
        MON_WED -> day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY
        TUE_THU -> day == DayOfWeek.TUESDAY || day == DayOfWeek.THURSDAY
        MON_THU ->
            day == DayOfWeek.MONDAY ||
                day == DayOfWeek.TUESDAY ||
                day == DayOfWeek.WEDNESDAY ||
                day == DayOfWeek.THURSDAY
        MON_TUE_THU_FRI ->
            day == DayOfWeek.MONDAY ||
                day == DayOfWeek.TUESDAY ||
                day == DayOfWeek.THURSDAY ||
                day == DayOfWeek.FRIDAY
        CUSTOM -> false
    }
}

private const val WeekdayMaskAll: Int = 0x7F // bits 0–6 = Mon–Sun

private val IsoLocalDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun habitCreatedLocalDate(habit: Habit): LocalDate =
    Instant.ofEpochMilli(habit.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()

/** Draft repeat settings while adding or editing a habit (Daily / custom weekdays / every N days). */
data class HabitRepeatDraft(
    val frequency: Frequency = Frequency.DAILY,
    val weeklyDayMask: Int? = null,
    val repeatIntervalDays: Int? = null,
    val repeatAnchorDateKey: String? = null,
) {
    fun summary(locale: Locale = Locale.getDefault()): String =
        habitRepeatSummary(frequency, weeklyDayMask, repeatIntervalDays, repeatAnchorDateKey, locale)
}

fun Habit.isScheduledOn(date: LocalDate): Boolean {
    repeatIntervalDays?.takeIf { it >= 2 }?.let { n ->
        val anchor = repeatAnchorDateKey?.let { LocalDate.parse(it, IsoLocalDate) }
            ?: habitCreatedLocalDate(this)
        val diff = ChronoUnit.DAYS.between(anchor, date)
        if (diff < 0) return false
        return diff % n == 0L
    }
    weeklyDayMask?.let { mask -> return (mask and dayBit(date.dayOfWeek)) != 0 }
    return frequency.isScheduledFor(date.dayOfWeek)
}

/** Human-readable repeat line for cards and editors. */
fun Habit.repeatSummary(locale: Locale = Locale.getDefault()): String =
    habitRepeatSummary(frequency, weeklyDayMask, repeatIntervalDays, repeatAnchorDateKey, locale)

private fun habitRepeatSummary(
    frequency: Frequency,
    weeklyDayMask: Int?,
    repeatIntervalDays: Int?,
    repeatAnchorDateKey: String?,
    locale: Locale,
): String {
    repeatIntervalDays?.takeIf { it >= 2 }?.let { n ->
        val anchorParsed = repeatAnchorDateKey?.let { LocalDate.parse(it, IsoLocalDate) }
        val anchorText = anchorParsed?.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        ) ?: "start"
        return "Every $n days (from $anchorText)"
    }
    weeklyDayMask?.let { mask ->
        val days = DayOfWeek.values().filter { (mask and dayBit(it)) != 0 }
        return when {
            days.isEmpty() -> "Pick days"
            days.size >= 7 -> "Every day"
            days.size == 1 ->
                "Weekly on ${days.first().getDisplayName(TextStyle.FULL, locale)}"
            else ->
                days.joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
        }
    }
    if (frequency == Frequency.CUSTOM) return "Pick days"
    return frequency.displayLabel()
}

/** Google Calendar–style RRULE for this habit’s repeat pattern. */
fun Habit.recurrenceRrule(): String {
    repeatIntervalDays?.takeIf { it >= 2 }?.let { n ->
        return "RRULE:FREQ=DAILY;INTERVAL=$n"
    }
    weeklyDayMask?.let { mask ->
        if (mask == WeekdayMaskAll) return "RRULE:FREQ=DAILY"
        return "RRULE:FREQ=WEEKLY;BYDAY=${maskToRfcByDay(mask)}"
    }
    return when (frequency) {
        Frequency.DAILY -> "RRULE:FREQ=DAILY"
        Frequency.WEEKDAYS -> "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
        Frequency.WEEKENDS -> "RRULE:FREQ=WEEKLY;BYDAY=SA,SU"
        Frequency.THREE_PER_WEEK -> "RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR"
        Frequency.MON_WED -> "RRULE:FREQ=WEEKLY;BYDAY=MO,WE"
        Frequency.TUE_THU -> "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
        Frequency.MON_THU -> "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH"
        Frequency.MON_TUE_THU_FRI -> "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,TH,FR"
        Frequency.CUSTOM -> "RRULE:FREQ=WEEKLY;BYDAY=MO"
    }
}

private fun dayBit(day: DayOfWeek): Int = 1 shl (day.value - 1)

private fun maskToRfcByDay(mask: Int): String =
    DayOfWeek.values()
        .filter { (mask and dayBit(it)) != 0 }
        .joinToString(",") { it.toRfcByDayToken() }

private fun DayOfWeek.toRfcByDayToken(): String = when (this) {
    DayOfWeek.MONDAY -> "MO"
    DayOfWeek.TUESDAY -> "TU"
    DayOfWeek.WEDNESDAY -> "WE"
    DayOfWeek.THURSDAY -> "TH"
    DayOfWeek.FRIDAY -> "FR"
    DayOfWeek.SATURDAY -> "SA"
    DayOfWeek.SUNDAY -> "SU"
}

/** Short label for UI (cards, subtitles). */
fun Frequency.displayLabel(): String = when (this) {
    Frequency.DAILY -> "Every day"
    Frequency.WEEKDAYS -> "Mon–Fri"
    Frequency.WEEKENDS -> "Sat & Sun"
    Frequency.THREE_PER_WEEK -> "Mon, Wed, Fri"
    Frequency.MON_WED -> "Mon & Wed"
    Frequency.TUE_THU -> "Tue & Thu"
    Frequency.MON_THU -> "Mon–Thu"
    Frequency.MON_TUE_THU_FRI -> "Mon, Tue, Thu, Fri"
    Frequency.CUSTOM -> "Custom"
}
