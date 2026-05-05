package com.habs.presentation.calendar

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.domain.model.Habit
import com.habs.domain.model.Task
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import com.habs.domain.repository.TaskRepository
import com.habs.domain.usecase.SyncHabitToCalendarUseCase
import com.habs.domain.usecase.SyncTaskToCalendarUseCase
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CalendarUiState(
    val habits: List<Habit> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val isSignedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val syncHabit: SyncHabitToCalendarUseCase,
    private val syncTask: SyncTaskToCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val signedIn = withContext(Dispatchers.IO) {
                runCatching { calendarRepository.isSignedIn() }.getOrDefault(false)
            }
            _uiState.update { it.copy(isSignedIn = signedIn) }

            combine(
                habitRepository.getAllHabits(),
                taskRepository.observeAllTasks()
            ) { habits, tasks -> habits to tasks }
                .catch { _uiState.update { s -> s.copy(isLoading = false) } }
                .onEach { (habits, tasks) ->
                    _uiState.update { it.copy(habits = habits, tasks = tasks, isLoading = false) }
                }
                .launchIn(viewModelScope)
        }
    }

    fun calendarSignInIntent(activity: Activity) =
        calendarRepository.calendarSignInIntent(activity)

    fun onCalendarSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            if (data == null) {
                _uiState.update {
                    it.copy(message = "No sign-in data from Google. Try again or update Google Play services.")
                }
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                runCatching { calendarRepository.completeCalendarSignIn(data) }
                    .getOrElse { Result.failure(it) }
            }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(isSignedIn = true, message = "Signed in — use Sync below for habits and tasks")
                    }
                }
                .onFailure { e ->
                    val api = e as? com.google.android.gms.common.api.ApiException
                    val msg = when (api?.statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                            "Sign-in was cancelled."
                        com.google.android.gms.common.ConnectionResult.NETWORK_ERROR ->
                            "Network error — check connection and try again"
                        com.google.android.gms.common.api.CommonStatusCodes.DEVELOPER_ERROR ->
                            "Developer error (10): In Google Cloud → APIs & Services → Credentials, " +
                                "create an Android OAuth client with package com.habs and your app’s SHA-1 " +
                                "(run ./gradlew signingReport). Enable the Google Calendar API for the project."
                        else -> api?.let { "Sign-in failed (${it.statusCode}): ${e.message}" }
                            ?: "Sign-in failed: ${e.message}"
                    }
                    _uiState.update { it.copy(message = msg) }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { calendarRepository.signOut() }
            _uiState.update { it.copy(isSignedIn = false, message = "Signed out") }
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val habitsToSync = _uiState.value.habits.filter { !it.calendarSynced }
            val tasksToSync = _uiState.value.tasks.filter { !it.calendarSynced && !it.isCompleted }
            var habitsSynced = 0
            var tasksSynced = 0
            withContext(Dispatchers.IO) {
                habitsToSync.forEach { habit ->
                    runCatching { syncHabit(habit) }.onSuccess { habitsSynced++ }
                }
                tasksToSync.forEach { task ->
                    runCatching { syncTask(task) }.onSuccess { tasksSynced++ }
                }
            }
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    message = "Synced $habitsSynced habits and $tasksSynced tasks to Calendar"
                )
            }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(message = null) }
}

@Composable
fun AuthCard(isSignedIn: Boolean, onSignIn: () -> Unit, onSignOut: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        if (isSignedIn) "Connected to Google Calendar" else "Connect Google Calendar",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (isSignedIn) {
                            "Use Sync below to add calendar events for habits and tasks not synced yet."
                        } else {
                            "Sign in with Google, then sync habits and tasks from Settings when you are ready."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = if (isSignedIn) onSignOut else onSignIn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSignedIn) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isSignedIn) "Disconnect" else "Sign in with Google")
            }
        }
    }
}
