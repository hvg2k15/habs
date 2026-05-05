package com.habs.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.data.repository.UserPreferencesRepository
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitWithCompletion
import com.habs.domain.model.Task
import com.habs.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TodayUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val habits: List<HabitWithCompletion> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val tasksDoneCount: Int = 0,
    val tasksTotalCount: Int = 0,
    val toastMessage: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayHabits: GetTodayHabitsUseCase,
    private val getTodayTasks: GetTodayTasksUseCase,
    private val toggleCompletion: ToggleHabitCompletionUseCase,
    private val addHabit: AddHabitUseCase,
    private val addTask: AddTaskUseCase,
    private val toggleTaskCompletion: ToggleTaskCompletionUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    userPreferences: UserPreferencesRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    /** Default “Sync to Google Calendar” when opening the new-habit sheet (Settings: auto-sync). */
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

    init {
        viewModelScope.launch {
            _selectedDate
                .onEach { d ->
                    // Update the visible day immediately so the header / picker match the new date
                    // while habits/tasks reload (avoids stale “today” UI when the inner Flow is slow).
                    _uiState.update { prev ->
                        prev.copy(
                            selectedDate = d,
                            isLoading = true
                        )
                    }
                }
                .flatMapLatest { d ->
                    combine(
                        getTodayHabits(d),
                        getTodayTasks(d)
                    ) { habits, tasks -> Triple(d, habits, tasks) }
                }
                .collect { (d, habits, tasks) ->
                    _uiState.update {
                        it.copy(
                            selectedDate = d,
                            habits = habits,
                            tasks = tasks,
                            isLoading = false,
                            completedCount = habits.count { h -> h.completedToday },
                            totalCount = habits.size,
                            tasksDoneCount = tasks.count { t -> t.isCompleted },
                            tasksTotalCount = tasks.size
                        )
                    }
                }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun shiftSelectedDayBy(deltaDays: Long) {
        setSelectedDate(_selectedDate.value.plusDays(deltaDays))
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            toggleCompletion(habitId, _selectedDate.value)
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

    fun removeHabit(habit: Habit) {
        viewModelScope.launch {
            deleteHabitUseCase(habit)
            showToast("\"${habit.name}\" deleted")
        }
    }

    fun addNewTask(title: String, dueDate: LocalDate, dueTime: LocalTime?, calendarSynced: Boolean) {
        viewModelScope.launch {
            addTask(title, dueDate, dueTime, calendarSynced).onSuccess { outcome ->
                val msg = buildString {
                    append("Task added")
                    if (calendarSynced && outcome.calendarSyncWarning == null) {
                        append(" — on Google Calendar")
                    }
                    outcome.calendarSyncWarning?.let { append(". ").append(it) }
                }
                showToast(msg)
            }.onFailure { e ->
                showToast(e.message ?: "Could not add task")
            }
        }
    }

    fun toggleTaskDone(task: Task) {
        viewModelScope.launch { toggleTaskCompletion(task, _selectedDate.value) }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task)
            showToast("Task removed")
        }
    }

    fun dismissToast() = _uiState.update { it.copy(toastMessage = null) }

    private fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }
}
