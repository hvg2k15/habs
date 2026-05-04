package com.habs.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitWithCompletion
import com.habs.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val habits: List<HabitWithCompletion> = emptyList(),
    val isLoading: Boolean = true,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val toastMessage: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayHabits: GetTodayHabitsUseCase,
    private val toggleCompletion: ToggleHabitCompletionUseCase,
    private val addHabit: AddHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase,
    private val syncToCalendar: SyncHabitToCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTodayHabits().collect { habits ->
                _uiState.update {
                    it.copy(
                        habits = habits,
                        isLoading = false,
                        completedCount = habits.count { h -> h.completedToday },
                        totalCount = habits.size
                    )
                }
            }
        }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            toggleCompletion(habitId)
        }
    }

    fun addNewHabit(habit: Habit) {
        viewModelScope.launch {
            addHabit(habit).onSuccess { outcome ->
                val msg = buildString {
                    append("\"${habit.name}\" added")
                    if (habit.calendarSynced && outcome.calendarSyncWarning == null) {
                        append(" — synced to Google Calendar")
                    }
                    outcome.calendarSyncWarning?.let { append(". ").append(it) }
                }
                showToast(msg)
            }.onFailure { e ->
                showToast(e.message ?: "Could not add habit")
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            deleteHabit.invoke(habit)
            showToast("\"${habit.name}\" deleted")
        }
    }

    fun toggleCalendarSync(habit: Habit) {
        viewModelScope.launch {
            syncToCalendar(habit).onSuccess {
                val msg = if (!habit.calendarSynced) "Synced to Google Calendar"
                else "Removed from Google Calendar"
                showToast(msg)
            }.onFailure {
                showToast("Calendar sync failed. Are you signed in?")
            }
        }
    }

    fun dismissToast() = _uiState.update { it.copy(toastMessage = null) }

    private fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }
}
