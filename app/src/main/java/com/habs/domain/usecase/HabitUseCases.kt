package com.habs.domain.usecase

import com.habs.domain.model.Habit
import com.habs.domain.model.HabitStats
import com.habs.domain.model.HabitWithCompletion
import com.habs.domain.model.OverallStats
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

data class AddHabitResult(val habitId: Long, val calendarSyncWarning: String? = null)

class GetTodayHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<List<HabitWithCompletion>> =
        habitRepository.getHabitsWithCompletionForDate(date)
}

class ToggleHabitCompletionUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate = LocalDate.now()) =
        habitRepository.toggleCompletion(habitId, date)
}

class SyncHabitToCalendarUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(habit: Habit): Result<Unit> {
        return if (habit.calendarSynced && habit.calendarEventId != null) {
            calendarRepository.removeHabitFromCalendar(habit.calendarEventId)
            habitRepository.updateHabit(habit.copy(calendarSynced = false, calendarEventId = null))
            Result.success(Unit)
        } else {
            calendarRepository.syncHabitToCalendar(habit).map { eventId ->
                habitRepository.updateHabit(habit.copy(calendarSynced = true, calendarEventId = eventId))
            }
        }
    }
}

class AddHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val syncHabitToCalendar: SyncHabitToCalendarUseCase
) {
    suspend operator fun invoke(habit: Habit): Result<AddHabitResult> {
        val id = habitRepository.insertHabit(habit)
        val stored = habit.copy(id = id)
        if (!habit.calendarSynced) {
            return Result.success(AddHabitResult(habitId = id))
        }
        return syncHabitToCalendar(stored).fold(
            onSuccess = { Result.success(AddHabitResult(habitId = id)) },
            onFailure = { e ->
                habitRepository.updateHabit(
                    stored.copy(calendarSynced = false, calendarEventId = null)
                )
                Result.success(
                    AddHabitResult(
                        habitId = id,
                        calendarSyncWarning = e.message ?: "Calendar sync failed"
                    )
                )
            }
        )
    }
}

class DeleteHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(habit: Habit) {
        habit.calendarEventId?.let { calendarRepository.removeHabitFromCalendar(it) }
        habitRepository.deleteHabit(habit)
    }
}

class GetHabitStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(
        habitId: Long,
        fromDate: LocalDate = LocalDate.now().minusYears(1),
        toDate: LocalDate = LocalDate.now()
    ): HabitStats =
        habitRepository.getStatsForHabit(habitId, fromDate, toDate)
}

class GetOverallStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(fromDate: LocalDate = LocalDate.now().withDayOfMonth(1)): OverallStats =
        habitRepository.getOverallStats(fromDate)
}
