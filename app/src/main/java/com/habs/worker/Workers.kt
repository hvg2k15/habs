package com.habs.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CalendarSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val calendarRepository: CalendarRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!calendarRepository.isSignedIn()) return Result.success()
            val habits = habitRepository.getAllHabits().first()
            habits.filter { it.calendarSynced && it.calendarEventId != null }.forEach { habit ->
                calendarRepository.updateCalendarEvent(habit)
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

class CalendarSyncWorkerFactory @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calendarRepository: CalendarRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context, workerClassName: String, workerParameters: WorkerParameters
    ) = when (workerClassName) {
        CalendarSyncWorker::class.java.name ->
            CalendarSyncWorker(appContext, workerParameters, habitRepository, calendarRepository)
        else -> null
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CalendarSyncWorker.schedule(WorkManager.getInstance(context))
        }
    }
}
