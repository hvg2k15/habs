package com.habs.data.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.services.calendar.CalendarScopes
import com.habs.data.preferences.habsPreferencesDataStore
import com.habs.data.remote.GoogleCalendarApi
import com.habs.domain.model.Habit
import com.habs.domain.model.Task
import com.habs.domain.repository.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val ACCOUNT_KEY = stringPreferencesKey("google_account")

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarApi: GoogleCalendarApi
) : CalendarRepository {

    private fun calendarScope() = Scope(CalendarScopes.CALENDAR)

    /**
     * Do not use [GoogleSignInOptions.Builder.requestIdToken] with a desktop/“installed” OAuth
     * client id — Play services returns [com.google.android.gms.common.api.CommonStatusCodes.DEVELOPER_ERROR] (10).
     * Calendar access uses [GoogleAccountCredential] + the same Calendar scope after sign-in.
     */
    private fun googleSignInOptions(): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(calendarScope())
            .build()

    /**
     * Requires an active Google Sign-In session with Calendar scope.
     * Keeps DataStore in sync for debugging; credential always comes from Play services.
     */
    private suspend fun resolveAccountEmail(): String? = withContext(Dispatchers.IO) {
        val last = GoogleSignIn.getLastSignedInAccount(context) ?: run {
            context.habsPreferencesDataStore.edit { it.remove(ACCOUNT_KEY) }
            return@withContext null
        }
        if (!GoogleSignIn.hasPermissions(last, calendarScope())) {
            return@withContext null
        }
        val email = last.email ?: return@withContext null
        calendarApi.setAccount(email)
        context.habsPreferencesDataStore.edit { prefs -> prefs[ACCOUNT_KEY] = email }
        email
    }

    override suspend fun isSignedIn(): Boolean = resolveAccountEmail() != null

    override fun calendarSignInIntent(activity: Activity): Intent =
        GoogleSignIn.getClient(activity, googleSignInOptions()).signInIntent

    override suspend fun completeCalendarSignIn(data: Intent?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val account = Tasks.await(GoogleSignIn.getSignedInAccountFromIntent(data))
                val email = account.email
                    ?: return@withContext Result.failure(IllegalStateException("No Google account email"))
                if (!GoogleSignIn.hasPermissions(account, calendarScope())) {
                    return@withContext Result.failure(
                        SecurityException("Calendar access was not granted")
                    )
                }
                calendarApi.setAccount(email)
                context.habsPreferencesDataStore.edit { prefs -> prefs[ACCOUNT_KEY] = email }
                Result.success(Unit)
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                Tasks.await(GoogleSignIn.getClient(context, googleSignInOptions()).signOut())
            } catch (_: Exception) {
                // Still clear local prefs if sign-out fails
            }
            context.habsPreferencesDataStore.edit { it.remove(ACCOUNT_KEY) }
        }
    }

    override suspend fun syncHabitToCalendar(habit: Habit): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                resolveAccountEmail()
                    ?: return@withContext Result.failure(
                        Exception("Sign in with Google and allow Calendar access")
                    )
                val eventId = calendarApi.createRecurringEvent(habit)
                Result.success(eventId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun removeHabitFromCalendar(calendarEventId: String): Result<Unit> =
        deleteCalendarEvent(calendarEventId)

    override suspend fun removeTaskFromCalendar(calendarEventId: String): Result<Unit> =
        deleteCalendarEvent(calendarEventId)

    private suspend fun deleteCalendarEvent(calendarEventId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                resolveAccountEmail()
                    ?: return@withContext Result.failure(
                        Exception("Sign in with Google and allow Calendar access")
                    )
                calendarApi.deleteEvent(calendarEventId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateCalendarEvent(habit: Habit): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                resolveAccountEmail()
                    ?: return@withContext Result.failure(
                        Exception("Sign in with Google and allow Calendar access")
                    )
                calendarApi.updateEvent(habit)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun syncTaskToCalendar(task: Task): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                resolveAccountEmail()
                    ?: return@withContext Result.failure(
                        Exception("Sign in with Google and allow Calendar access")
                    )
                val eventId = calendarApi.createTaskEvent(task)
                Result.success(eventId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTaskCalendarEvent(task: Task): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                resolveAccountEmail()
                    ?: return@withContext Result.failure(
                        Exception("Sign in with Google and allow Calendar access")
                    )
                calendarApi.updateTaskEvent(task)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
