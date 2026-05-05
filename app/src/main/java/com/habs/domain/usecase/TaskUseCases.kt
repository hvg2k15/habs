package com.habs.domain.usecase

import com.habs.domain.model.Task
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val TaskDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

data class AddTaskResult(val taskId: Long, val calendarSyncWarning: String? = null)

class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(viewDate: LocalDate = LocalDate.now()): Flow<List<Task>> =
        taskRepository.observeTasksForDate(viewDate)
}

class SyncTaskToCalendarUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return if (task.calendarSynced && task.calendarEventId != null) {
            calendarRepository.removeTaskFromCalendar(task.calendarEventId)
            taskRepository.updateTask(task.copy(calendarSynced = false, calendarEventId = null))
            Result.success(Unit)
        } else {
            calendarRepository.syncTaskToCalendar(task).map { eventId ->
                taskRepository.updateTask(task.copy(calendarSynced = true, calendarEventId = eventId))
            }
        }
    }
}

class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val syncTaskToCalendar: SyncTaskToCalendarUseCase
) {
    suspend operator fun invoke(
        title: String,
        dueDate: LocalDate = LocalDate.now(),
        dueTime: LocalTime? = null,
        calendarSynced: Boolean = false
    ): Result<AddTaskResult> {
        val key = dueDate.format(TaskDateFmt)
        val task = Task(
            title = title.trim(),
            dueDateKey = key,
            dueTime = dueTime,
            calendarSynced = calendarSynced
        )
        val id = taskRepository.insertTask(task)
        val stored = task.copy(id = id)
        if (!calendarSynced) {
            return Result.success(AddTaskResult(id))
        }
        return syncTaskToCalendar(stored).fold(
            onSuccess = { Result.success(AddTaskResult(id)) },
            onFailure = { e ->
                taskRepository.updateTask(stored.copy(calendarSynced = false, calendarEventId = null))
                Result.success(
                    AddTaskResult(id, e.message ?: "Calendar sync failed")
                )
            }
        )
    }
}

class ToggleTaskCompletionUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(task: Task, onDate: LocalDate = LocalDate.now()) {
        val todayKey = onDate.format(TaskDateFmt)
        val updated = if (task.isCompleted) {
            task.copy(isCompleted = false, completedOnKey = null)
        } else {
            task.copy(isCompleted = true, completedOnKey = todayKey)
        }
        taskRepository.updateTask(updated)
        if (updated.calendarSynced && updated.calendarEventId != null) {
            calendarRepository.updateTaskCalendarEvent(updated)
        }
    }
}

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(task: Task) {
        task.calendarEventId?.let { calendarRepository.removeTaskFromCalendar(it) }
        taskRepository.deleteTask(task)
    }
}
