package com.habs.data.remote

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar as GoogleCalendarService
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.google.api.client.util.DateTime as ApiDateTime
import com.habs.domain.model.Habit
import com.habs.domain.model.Task
import com.habs.domain.model.isScheduledOn
import com.habs.domain.model.recurrenceRrule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
        private val httpTransport by lazy { AndroidHttp.newCompatibleTransport() }
    }

    private val credential: GoogleAccountCredential by lazy {
        GoogleAccountCredential.usingOAuth2(context, SCOPES)
            .setBackOff(ExponentialBackOff())
    }

    private fun buildService(): GoogleCalendarService =
        GoogleCalendarService.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Habs")
            .build()

    fun setAccount(accountName: String) {
        credential.selectedAccountName = accountName
    }

    fun getSelectedAccount(): String? = credential.selectedAccountName

    suspend fun createRecurringEvent(habit: Habit): String {
        try {
            val service = buildService()
            val event = habit.toCalendarEvent()
            val created = service.events().insert(CALENDAR_ID, event).execute()
            return created.id ?: error("Calendar insert returned no event id")
        } catch (e: UserRecoverableAuthIOException) {
            throw Exception(
                "Google needs you to grant Calendar access again — open Settings, disconnect Google Calendar, then sign in again.",
                e
            )
        } catch (e: GoogleJsonResponseException) {
            throw Exception("Calendar API ${e.statusCode}: ${e.message}", e)
        }
    }

    suspend fun deleteEvent(eventId: String) {
        try {
            buildService().events().delete(CALENDAR_ID, eventId).execute()
        } catch (e: UserRecoverableAuthIOException) {
            throw Exception("Calendar re-authorization required.", e)
        } catch (e: GoogleJsonResponseException) {
            throw Exception("Calendar API ${e.statusCode}: ${e.message}", e)
        }
    }

    suspend fun updateEvent(habit: Habit) {
        try {
            val eventId = habit.calendarEventId ?: error("Missing calendar event id")
            val service = buildService()
            val event = habit.toCalendarEvent()
            service.events().update(CALENDAR_ID, eventId, event).execute()
        } catch (e: UserRecoverableAuthIOException) {
            throw Exception("Calendar re-authorization required.", e)
        } catch (e: GoogleJsonResponseException) {
            throw Exception("Calendar API ${e.statusCode}: ${e.message}", e)
        }
    }

    suspend fun createTaskEvent(task: Task): String {
        try {
            val service = buildService()
            val event = task.toTaskCalendarEvent()
            val created = service.events().insert(CALENDAR_ID, event).execute()
            return created.id ?: error("Calendar insert returned no event id")
        } catch (e: UserRecoverableAuthIOException) {
            throw Exception(
                "Google needs you to grant Calendar access again — open Settings, disconnect Google Calendar, then sign in again.",
                e
            )
        } catch (e: GoogleJsonResponseException) {
            throw Exception("Calendar API ${e.statusCode}: ${e.message}", e)
        }
    }

    suspend fun updateTaskEvent(task: Task) {
        try {
            val eventId = task.calendarEventId ?: error("Missing calendar event id")
            val service = buildService()
            val event = task.toTaskCalendarEvent()
            service.events().update(CALENDAR_ID, eventId, event).execute()
        } catch (e: UserRecoverableAuthIOException) {
            throw Exception("Calendar re-authorization required.", e)
        } catch (e: GoogleJsonResponseException) {
            throw Exception("Calendar API ${e.statusCode}: ${e.message}", e)
        }
    }

    /**
     * DTSTART for a recurring series must fall on a day that the RRULE actually includes,
     * or Google rejects the event or produces an empty series.
     */
    private fun Habit.firstScheduledDateOnOrAfter(from: LocalDate): LocalDate {
        var d = from
        repeat(800) {
            if (isScheduledOn(d)) return d
            d = d.plusDays(1)
        }
        return from
    }

    private fun Habit.toCalendarEvent(): Event {
        val startDate = firstScheduledDateOnOrAfter(LocalDate.now())
        val reminderHour = executionTime?.hour ?: 8
        val reminderMinute = executionTime?.minute ?: 0
        val zone = ZoneId.systemDefault()
        val startZdt = ZonedDateTime.of(startDate, LocalTime.of(reminderHour, reminderMinute), zone)
        val endZdt = startZdt.plusMinutes(30)
        val startDateTime = startZdt.toInstant().toEpochMilli()
        val endDateTime = endZdt.toInstant().toEpochMilli()

        val recurrenceRule = recurrenceRrule()

        val zoneId = zone.id
        return Event().apply {
            summary = "$icon $name"
            description =
                "Habs habit reminder — check off in the app when done. " +
                    "(Shown as a Calendar event, not Google Tasks.)"
            start = EventDateTime()
                .setDateTime(com.google.api.client.util.DateTime(startDateTime))
                .setTimeZone(zoneId)
            end = EventDateTime()
                .setDateTime(com.google.api.client.util.DateTime(endDateTime))
                .setTimeZone(zoneId)
            recurrence = listOf(recurrenceRule)
            colorId = "9"
            reminders = Event.Reminders().apply {
                useDefault = false
                overrides = listOf(
                    EventReminder().setMethod("popup").setMinutes(10)
                )
            }
        }
    }

    private fun Task.toTaskCalendarEvent(): Event {
        val due = LocalDate.parse(dueDateKey)
        val zone = ZoneId.systemDefault()
        val zoneId = zone.id
        val summaryPrefix = if (isCompleted) "✓ " else ""
        return Event().apply {
            summary = "${summaryPrefix}📋 $title"
            description =
                "Habs task — check off in the app when done. " +
                    "(Shown as a Calendar event, not Google Tasks.)"
            if (dueTime != null) {
                val startZdt = ZonedDateTime.of(due, dueTime, zone)
                val endZdt = startZdt.plusHours(1)
                start = EventDateTime()
                    .setDateTime(ApiDateTime(startZdt.toInstant().toEpochMilli()))
                    .setTimeZone(zoneId)
                end = EventDateTime()
                    .setDateTime(ApiDateTime(endZdt.toInstant().toEpochMilli()))
                    .setTimeZone(zoneId)
            } else {
                val iso = DateTimeFormatter.ISO_LOCAL_DATE
                val startStr = due.format(iso)
                val endStr = due.plusDays(1).format(iso)
                start = EventDateTime().setDate(ApiDateTime(startStr))
                end = EventDateTime().setDate(ApiDateTime(endStr))
            }
            colorId = "5"
            reminders = Event.Reminders().apply {
                useDefault = false
                overrides = listOf(
                    EventReminder().setMethod("popup").setMinutes(10)
                )
            }
        }
    }
}
