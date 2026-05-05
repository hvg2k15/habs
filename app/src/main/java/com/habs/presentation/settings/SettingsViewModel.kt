package com.habs.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    val calendarAutoSyncNewHabits: StateFlow<Boolean> =
        userPreferences.calendarAutoSyncNewHabits.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            true
        )

    val calendarAutoSyncNewTasks: StateFlow<Boolean> =
        userPreferences.calendarAutoSyncNewTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            true
        )

    fun setCalendarAutoSyncNewHabits(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setCalendarAutoSyncNewHabits(enabled)
        }
    }

    fun setCalendarAutoSyncNewTasks(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setCalendarAutoSyncNewTasks(enabled)
        }
    }
}
