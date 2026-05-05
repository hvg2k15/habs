package com.habs.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import com.habs.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class CalendarSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!calendarRepository.isSignedIn()) return Result.success()
            val habits = habitRepository.getAllHabits().first()
            habits.filter { it.calendarSynced && it.calendarEventId != null }.forEach { habit ->
                calendarRepository.updateCalendarEvent(habit)
            }
            val tasks = taskRepository.observeAllTasks().first()
            tasks.filter { it.calendarSynced && it.calendarEventId != null }.forEach { task ->
                calendarRepository.updateTaskCalendarEvent(task)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "calendar_sync"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<CalendarSyncWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CalendarSyncWorker.schedule(WorkManager.getInstance(context))
        }
    }
}
