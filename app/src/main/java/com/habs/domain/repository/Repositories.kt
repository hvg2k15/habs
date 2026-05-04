package com.habs.domain.repository

import com.habs.domain.model.Habit
import com.habs.domain.model.HabitCompletion
import com.habs.domain.model.HabitStats
import com.habs.domain.model.HabitWithCompletion
import com.habs.domain.model.OverallStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun getHabitsWithCompletionForDate(date: LocalDate): Flow<List<HabitWithCompletion>>
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun toggleCompletion(habitId: Long, date: LocalDate)
    suspend fun getStatsForHabit(habitId: Long, fromDate: LocalDate): HabitStats
    suspend fun getOverallStats(fromDate: LocalDate): OverallStats
    fun getCompletionsForDateRange(from: LocalDate, to: LocalDate): Flow<List<HabitCompletion>>
}

interface CalendarRepository {
    suspend fun syncHabitToCalendar(habit: Habit): Result<String>
    suspend fun removeHabitFromCalendar(calendarEventId: String): Result<Unit>
    suspend fun updateCalendarEvent(habit: Habit): Result<Unit>
    suspend fun isSignedIn(): Boolean
    suspend fun signIn(activity: android.app.Activity): Result<Unit>
    suspend fun signOut()
}
