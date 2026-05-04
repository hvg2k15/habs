package com.habs.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Habit(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val frequency: Frequency,
    val reminderTime: LocalTime? = null,
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
    DAILY, WEEKDAYS, THREE_PER_WEEK;

    fun isScheduledFor(day: DayOfWeek): Boolean = when (this) {
        DAILY -> true
        WEEKDAYS -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
        THREE_PER_WEEK -> day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.FRIDAY
    }
}
