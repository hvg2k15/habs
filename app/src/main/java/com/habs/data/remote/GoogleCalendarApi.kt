package com.habs.data.remote

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.habs.domain.model.Frequency
import com.habs.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarApi @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CALENDAR_ID = "primary"
        val SCOPES = listOf(CalendarScopes.CALENDAR)
    }

    private val credential: GoogleAccountCredential by lazy {
        GoogleAccountCredential.usingOAuth2(context, SCOPES)
            .setBackOff(ExponentialBackOff())
    }

    private fun buildService(): Calendar {
        return Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Habs").build()
    }

    fun setAccount(accountName: String) {
        credential.selectedAccountName = accountName
    }

    fun getSelectedAccount(): String? = credential.selectedAccountName

    suspend fun createRecurringEvent(habit: Habit): String {
        val service = buildService()
        val event = habit.toCalendarEvent()
        val created = service.events().insert(CALENDAR_ID, event).execute()
        return created.id
    }

    suspend fun deleteEvent(eventId: String) {
        buildService().events().delete(CALENDAR_ID, eventId).execute()
    }

    suspend fun updateEvent(habit: Habit) {
        val service = buildService()
        val event = habit.toCalendarEvent()
        service.events().update(CALENDAR_ID, habit.calendarEventId, event).execute()
    }

    private fun Habit.toCalendarEvent(): Event {
        val startDate = LocalDate.now()
        val reminderHour = reminderTime?.hour ?: 8
        val reminderMinute = reminderTime?.minute ?: 0

        val startDateTime = startDate.atTime(reminderHour, reminderMinute)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        val endDateTime = startDate.atTime(reminderHour, reminderMinute + 30)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        val recurrenceRule = when (frequency) {
            Frequency.DAILY -> "RRULE:FREQ=DAILY"
            Frequency.WEEKDAYS -> "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
            Frequency.THREE_PER_WEEK -> "RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR"
        }

        return Event().apply {
            summary = "$icon $name"
            description = "Habs habit reminder"
            start = EventDateTime().setDateTime(
                com.google.api.client.util.DateTime(startDateTime)
            )
            end = EventDateTime().setDateTime(
                com.google.api.client.util.DateTime(endDateTime)
            )
            recurrence = listOf(recurrenceRule)
            colorId = "9"
        }
    }
}
