package com.habs.domain.repository

import com.habs.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun observeTasksForDate(viewDate: LocalDate): Flow<List<Task>>
    fun observeAllTasks(): Flow<List<Task>>
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
}
