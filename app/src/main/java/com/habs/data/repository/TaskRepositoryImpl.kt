package com.habs.data.repository

import com.habs.data.local.TaskDao
import com.habs.data.local.toEntity
import com.habs.domain.model.Task
import com.habs.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun observeTasksForDate(viewDate: LocalDate): Flow<List<Task>> {
        val key = viewDate.format(fmt)
        val flow =
            if (viewDate == LocalDate.now()) {
                taskDao.observeTasksForTodayWithBacklog(key)
            } else {
                taskDao.observeTasksDueOnDay(key)
            }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllTasks(): Flow<List<Task>> =
        taskDao.observeAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) =
        taskDao.updateTask(task.toEntity())

    override suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task.toEntity())
}
