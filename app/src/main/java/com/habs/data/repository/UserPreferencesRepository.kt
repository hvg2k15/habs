package com.habs.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.habs.data.preferences.habsPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore get() = context.habsPreferencesDataStore

    /** When true, new habits default to “sync to Google Calendar” in the add-habit sheet. Default on. */
    val calendarAutoSyncNewHabits: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_CALENDAR_AUTO_SYNC_NEW] ?: true
    }

    suspend fun setCalendarAutoSyncNewHabits(enabled: Boolean) {
        dataStore.edit { it[KEY_CALENDAR_AUTO_SYNC_NEW] = enabled }
    }

    /** When true, new tasks default to “sync to Google Calendar” in the add-task sheet. Default on. */
    val calendarAutoSyncNewTasks: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_CALENDAR_AUTO_SYNC_NEW_TASKS] ?: true
    }

    suspend fun setCalendarAutoSyncNewTasks(enabled: Boolean) {
        dataStore.edit { it[KEY_CALENDAR_AUTO_SYNC_NEW_TASKS] = enabled }
    }

    companion object {
        private val KEY_CALENDAR_AUTO_SYNC_NEW = booleanPreferencesKey("calendar_auto_sync_new_habits")
        private val KEY_CALENDAR_AUTO_SYNC_NEW_TASKS = booleanPreferencesKey("calendar_auto_sync_new_tasks")
    }
}
