package com.habs.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
}

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions WHERE dateKey = :dateKey")
    fun getCompletionsForDate(dateKey: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND dateKey = :dateKey LIMIT 1")
    suspend fun getCompletion(habitId: Long, dateKey: String): CompletionEntity?

    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY completedAt ASC")
    suspend fun getCompletionsForHabit(habitId: Long): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE dateKey BETWEEN :fromKey AND :toKey")
    fun getCompletionsInRange(fromKey: String, toKey: String): Flow<List<CompletionEntity>>

    @Query("SELECT COUNT(*) FROM completions WHERE dateKey = :dateKey")
    suspend fun countCompletionsForDate(dateKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity)

    @Query("DELETE FROM completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun deleteCompletion(habitId: Long, dateKey: String)
}

@Dao
interface TaskDao {
    @Query(
        """
        SELECT * FROM tasks WHERE
          (isCompleted = 0 AND dueDateKey <= :todayKey)
          OR (isCompleted = 1 AND completedOnKey = :todayKey)
        ORDER BY isCompleted ASC, dueDateKey ASC,
          (CASE WHEN dueTimeMinutes IS NULL THEN 1 ELSE 0 END), dueTimeMinutes ASC,
          createdAt ASC
        """
    )
    fun observeTasksForTodayWithBacklog(todayKey: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks WHERE dueDateKey = :dateKey
        ORDER BY isCompleted ASC,
          (CASE WHEN dueTimeMinutes IS NULL THEN 1 ELSE 0 END), dueTimeMinutes ASC,
          createdAt ASC
        """
    )
    fun observeTasksDueOnDay(dateKey: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY dueDateKey ASC, createdAt ASC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(entity: TaskEntity): Long

    @Update
    suspend fun updateTask(entity: TaskEntity)

    @Delete
    suspend fun deleteTask(entity: TaskEntity)
}
