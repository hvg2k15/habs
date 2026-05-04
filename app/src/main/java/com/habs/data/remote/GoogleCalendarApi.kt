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
import com.habs.domain.model.Frequency
import com.habs.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
                "Google needs you to grant Calendar access again — open the Calendar tab, sign out, then sign in.",
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

    /**
     * DTSTART for a recurring series must fall on a day that the RRULE actually includes,
     * or Google rejects the event or produces an empty series.
     */
    private fun Habit.firstScheduledDateOnOrAfter(from: LocalDate): LocalDate {
        var d = from
        repeat(14) {
            if (frequency.isScheduledFor(d.dayOfWeek)) return d
            d = d.plusDays(1)
        }
        return from
    }

    private fun Habit.toCalendarEvent(): Event {
        val startDate = firstScheduledDateOnOrAfter(LocalDate.now())
        val reminderHour = reminderTime?.hour ?: 8
        val reminderMinute = reminderTime?.minute ?: 0
        val zone = ZoneId.systemDefault()
        val startZdt = ZonedDateTime.of(startDate, LocalTime.of(reminderHour, reminderMinute), zone)
        val endZdt = startZdt.plusMinutes(30)
        val startDateTime = startZdt.toInstant().toEpochMilli()
        val endDateTime = endZdt.toInstant().toEpochMilli()

        val recurrenceRule = when (frequency) {
            Frequency.DAILY -> "RRULE:FREQ=DAILY"
            Frequency.WEEKDAYS -> "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
            Frequency.THREE_PER_WEEK -> "RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR"
        }

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
}
