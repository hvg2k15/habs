package com.habs.data.repository

import com.habs.data.local.CompletionDao
import com.habs.data.local.CompletionEntity
import com.habs.data.local.HabitDao
import com.habs.data.local.HabitEntity
import com.habs.data.local.toEntity
import com.habs.domain.model.*
import com.habs.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM")

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> =
        habitDao.getAllHabits().map { list -> list.map { it.toDomain() } }

    override suspend fun getHabitById(id: Long): Habit? =
        habitDao.getHabitById(id)?.toDomain()

    override suspend fun insertHabit(habit: Habit): Long =
        habitDao.insertHabit(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) =
        habitDao.updateHabit(habit.toEntity())

    override suspend fun deleteHabit(habit: Habit) =
        habitDao.deleteHabit(habit.toEntity())

    override fun getHabitsWithCompletionForDate(date: LocalDate): Flow<List<HabitWithCompletion>> {
        val dateKey = date.format(DATE_FMT)
        return combine(
            habitDao.getAllHabits(),
            completionDao.getCompletionsForDate(dateKey)
        ) { habits, completions ->
            val completedIds = completions.map { it.habitId }.toSet()
            habits
                .filter { it.toDomain().frequency.isScheduledFor(date.dayOfWeek) }
                .map { entity ->
                    val habit = entity.toDomain()
                    val streak = calculateStreak(habit.id, date)
                    HabitWithCompletion(habit, habit.id in completedIds, streak)
                }
        }
    }

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val dateKey = date.format(DATE_FMT)
        val existing = completionDao.getCompletion(habitId, dateKey)
        if (existing != null) {
            completionDao.deleteCompletion(habitId, dateKey)
        } else {
            completionDao.insertCompletion(CompletionEntity(habitId = habitId, dateKey = dateKey))
        }
    }

    override fun getCompletionsForDateRange(from: LocalDate, to: LocalDate): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsInRange(from.format(DATE_FMT), to.format(DATE_FMT))
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getStatsForHabit(habitId: Long, fromDate: LocalDate): HabitStats {
        val habit = habitDao.getHabitById(habitId)?.toDomain() ?: error("Habit not found: $habitId")
        val completions = completionDao.getCompletionsForHabit(habitId)
        val completedDates = completions.map { it.dateKey }.toSet()

        val today = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(fromDate, today).toInt() + 1
        val scheduledDays = (0 until totalDays).count { offset ->
            val d = fromDate.plusDays(offset.toLong())
            habit.frequency.isScheduledFor(d.dayOfWeek)
        }
        val completedScheduledInPeriod = (0 until totalDays).count { offset ->
            val d = fromDate.plusDays(offset.toLong())
            habit.frequency.isScheduledFor(d.dayOfWeek) && d.format(DATE_FMT) in completedDates
        }
        val missedScheduled = (scheduledDays - completedScheduledInPeriod).coerceAtLeast(0)
        val completionRate =
            if (scheduledDays > 0) completedScheduledInPeriod.toFloat() / scheduledDays else 0f
        val dailyMap = completedDates.associateWith { true }
        val monthlyRates = buildMonthlyRates(habit, completedDates, fromDate, today)

        return HabitStats(
            habit = habit,
            completionRate = completionRate,
            currentStreak = calculateStreak(habitId, today),
            longestStreak = calculateLongestStreak(habit, completedDates),
            totalCompletions = completions.size,
            monthlyRates = monthlyRates,
            dailyCompletions = dailyMap,
            scheduledDaysInPeriod = scheduledDays,
            missedScheduledDays = missedScheduled
        )
    }

    override suspend fun getOverallStats(fromDate: LocalDate): OverallStats {
        val today = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(fromDate, today).toInt() + 1
        var totalCompletions = 0
        var perfectDays = 0
        var bestStreak = 0
        var bestStreakName = ""

        // Use stdlib first() — collects one emission and cancels the flow
        val habitsList: List<HabitEntity> = habitDao.getAllHabits().first()

        habitsList.forEach { entity ->
            val h = entity.toDomain()
            val completions = completionDao.getCompletionsForHabit(h.id)
            totalCompletions += completions.size
            val streak = calculateStreak(h.id, today)
            if (streak > bestStreak) {
                bestStreak = streak
                bestStreakName = h.name
            }
        }

        (0 until totalDays).forEach { offset ->
            val d = fromDate.plusDays(offset.toLong())
            val dateKey = d.format(DATE_FMT)
            val scheduledCount = habitsList.count { it.toDomain().frequency.isScheduledFor(d.dayOfWeek) }
            val doneCount = completionDao.countCompletionsForDate(dateKey)
            if (scheduledCount > 0 && doneCount >= scheduledCount) perfectDays++
        }

        val monthlyOverview = buildMonthlyOverview(habitsList, fromDate, today)
        val avgRate = if (habitsList.isNotEmpty()) totalCompletions.toFloat() / habitsList.size else 0f

        return OverallStats(
            averageCompletionRate = avgRate,
            perfectDays = perfectDays,
            totalCheckIns = totalCompletions,
            bestStreak = bestStreak,
            bestStreakHabitName = bestStreakName,
            monthlyOverview = monthlyOverview
        )
    }

    private suspend fun calculateStreak(habitId: Long, upTo: LocalDate): Int {
        val habit = habitDao.getHabitById(habitId)?.toDomain() ?: return 0
        val completions = completionDao.getCompletionsForHabit(habitId)
        val completedDates = completions.map { it.dateKey }.toSet()
        var streak = 0
        var current = upTo
        while (true) {
            if (!habit.frequency.isScheduledFor(current.dayOfWeek)) {
                current = current.minusDays(1)
                continue
            }
            if (current.format(DATE_FMT) in completedDates) {
                streak++
                current = current.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(habit: Habit, completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sorted = completedDates.sorted()
        var longest = 1
        var current = 1
        for (i in 1 until sorted.size) {
            val prev = LocalDate.parse(sorted[i - 1], DATE_FMT)
            val curr = LocalDate.parse(sorted[i], DATE_FMT)
            if (ChronoUnit.DAYS.between(prev, curr) == 1L) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun buildMonthlyRates(
        habit: Habit,
        completedDates: Set<String>,
        from: LocalDate,
        to: LocalDate
    ): Map<String, Float> {
        val result = mutableMapOf<String, Float>()
        var month = from.withDayOfMonth(1)
        while (!month.isAfter(to)) {
            val monthKey = month.format(MONTH_FMT)
            val daysInMonth = month.lengthOfMonth()
            var scheduled = 0
            var done = 0
            (1..daysInMonth).forEach { day ->
                val d = month.withDayOfMonth(day)
                if (!d.isAfter(to) && habit.frequency.isScheduledFor(d.dayOfWeek)) {
                    scheduled++
                    if (d.format(DATE_FMT) in completedDates) done++
                }
            }
            result[monthKey] = if (scheduled > 0) done.toFloat() / scheduled else 0f
            month = month.plusMonths(1)
        }
        return result
    }

    private suspend fun buildMonthlyOverview(
        habits: List<HabitEntity>,
        from: LocalDate,
        to: LocalDate
    ): List<MonthlyOverview> {
        val result = mutableListOf<MonthlyOverview>()
        var month = from.withDayOfMonth(1)
        while (!month.isAfter(to)) {
            val monthKey = month.format(MONTH_FMT)
            val daysInMonth = month.lengthOfMonth()
            var totalCheckIns = 0
            var perfectDays = 0
            var totalScheduled = 0
            (1..daysInMonth).forEach { day ->
                val d = month.withDayOfMonth(day)
                if (!d.isAfter(to)) {
                    val dateKey = d.format(DATE_FMT)
                    val scheduled = habits.count { it.toDomain().frequency.isScheduledFor(d.dayOfWeek) }
                    val done = completionDao.countCompletionsForDate(dateKey)
                    totalCheckIns += done
                    totalScheduled += scheduled
                    if (scheduled > 0 && done >= scheduled) perfectDays++
                }
            }
            result.add(
                MonthlyOverview(
                    monthKey = monthKey,
                    completionRate = if (totalScheduled > 0) totalCheckIns.toFloat() / totalScheduled else 0f,
                    perfectDays = perfectDays,
                    totalCheckIns = totalCheckIns
                )
            )
            month = month.plusMonths(1)
        }
        return result
    }
}