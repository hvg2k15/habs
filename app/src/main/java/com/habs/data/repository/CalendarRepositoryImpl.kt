package com.habs.data.repository

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.habs.data.remote.GoogleCalendarApi
import com.habs.domain.model.Habit
import com.habs.domain.repository.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("habs_prefs")
private val ACCOUNT_KEY = stringPreferencesKey("google_account")

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarApi: GoogleCalendarApi
) : CalendarRepository {

    override suspend fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && !account.isExpired
    }

    override suspend fun signIn(activity: Activity): Result<Unit> {
        return try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar")
                )
                .build()
            val client = GoogleSignIn.getClient(activity, gso)
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                calendarApi.setAccount(account.email ?: "")
                context.dataStore.edit { it[ACCOUNT_KEY] = account.email ?: "" }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
        context.dataStore.edit { it.remove(ACCOUNT_KEY) }
    }

    override suspend fun syncHabitToCalendar(habit: Habit): Result<String> {
        return try {
            val accountName = context.dataStore.data
                .map { it[ACCOUNT_KEY] }.first() ?: return Result.failure(Exception("Not signed in"))
            calendarApi.setAccount(accountName)
            val eventId = calendarApi.createRecurringEvent(habit)
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeHabitFromCalendar(calendarEventId: String): Result<Unit> {
        return try {
            calendarApi.deleteEvent(calendarEventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCalendarEvent(habit: Habit): Result<Unit> {
        return try {
            calendarApi.updateEvent(habit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
