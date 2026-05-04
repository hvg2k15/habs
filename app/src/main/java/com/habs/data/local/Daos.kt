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
