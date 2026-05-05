package com.habs.domain.repository

import com.habs.domain.model.Habit
import com.habs.domain.model.HabitCompletion
import com.habs.domain.model.HabitStats
import com.habs.domain.model.HabitWithCompletion
import com.habs.domain.model.OverallStats
import com.habs.domain.model.Task
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
    /** Inclusive date range [fromDate, toDate] (clamped to habit creation day where applicable). */
    suspend fun getStatsForHabit(habitId: Long, fromDate: LocalDate, toDate: LocalDate): HabitStats
    suspend fun getOverallStats(fromDate: LocalDate): OverallStats
    fun getCompletionsForDateRange(from: LocalDate, to: LocalDate): Flow<List<HabitCompletion>>
}

interface CalendarRepository {
    suspend fun syncHabitToCalendar(habit: Habit): Result<String>
    suspend fun removeHabitFromCalendar(calendarEventId: String): Result<Unit>
    suspend fun updateCalendarEvent(habit: Habit): Result<Unit>
    suspend fun syncTaskToCalendar(task: Task): Result<String>
    suspend fun removeTaskFromCalendar(calendarEventId: String): Result<Unit>
    suspend fun updateTaskCalendarEvent(task: Task): Result<Unit>
    suspend fun isSignedIn(): Boolean
    /** Intent for [androidx.activity.result.ActivityResultContracts.StartActivityForResult]. */
    fun calendarSignInIntent(activity: android.app.Activity): android.content.Intent
    suspend fun completeCalendarSignIn(data: android.content.Intent?): Result<Unit>
    suspend fun signOut()
}
