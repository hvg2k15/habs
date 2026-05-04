package com.habs.presentation.calendar

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habs.domain.model.Habit
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import com.habs.domain.usecase.SyncHabitToCalendarUseCase
import com.habs.presentation.today.HabsBottomBar
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CalendarUiState(
    val habits: List<Habit> = emptyList(),
    val isSignedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calendarRepository: CalendarRepository,
    private val syncHabit: SyncHabitToCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Run sign-in check on IO thread — never block main thread with network/disk calls
            val signedIn = withContext(Dispatchers.IO) {
                runCatching { calendarRepository.isSignedIn() }.getOrDefault(false)
            }
            _uiState.update { it.copy(isSignedIn = signedIn) }

            // Collect habits separately so sign-in state shows immediately
            habitRepository.getAllHabits()
                .catch { _uiState.update { s -> s.copy(isLoading = false) } }
                .onEach { habits ->
                    _uiState.update { it.copy(habits = habits, isLoading = false) }
                }
                .launchIn(viewModelScope)
        }
    }

    fun calendarSignInIntent(activity: Activity) =
        calendarRepository.calendarSignInIntent(activity)

    fun onCalendarSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            // Do not require activity result RESULT_OK — some devices/Play Services return other codes
            // while still sending an Intent; Google recommends parsing via getSignedInAccountFromIntent.
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
                        it.copy(isSignedIn = true, message = "Signed in — habits sync to your calendar")
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

    fun toggleSync(habit: Habit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = withContext(Dispatchers.IO) {
                runCatching { syncHabit(habit) }.getOrElse { Result.failure(it) }
            }
            result
                .onSuccess {
                    val msg = if (!habit.calendarSynced) "\"${habit.name}\" synced to Calendar"
                    else "Removed from Calendar"
                    _uiState.update { it.copy(message = msg) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(message = "Sync failed: ${e.message}") }
                }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            var synced = 0
            withContext(Dispatchers.IO) {
                _uiState.value.habits.filter { !it.calendarSynced }.forEach { habit ->
                    runCatching { syncHabit(habit) }.onSuccess { synced++ }
                }
            }
            _uiState.update { it.copy(isSyncing = false, message = "Synced $synced habits to Calendar") }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as? Activity

    val calendarSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onCalendarSignInResult(result.data)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Calendar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.isSignedIn) {
                        IconButton(onClick = { viewModel.syncAll() }) {
                            Icon(
                                Icons.Default.Sync, "Sync all",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            HabsBottomBar(
                selectedIndex = 2,
                onNavigateToToday = onNavigateBack,
                onNavigateToStats = onNavigateToStats,
                onNavigateToCalendar = {},
                onNavigateToSettings = onNavigateToSettings
            )
        },
        snackbarHost = {
            uiState.message?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2800)
                    viewModel.dismissMessage()
                }
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    AuthCard(
                        isSignedIn = uiState.isSignedIn,
                        onSignIn = {
                            activity?.let { a ->
                                calendarSignInLauncher.launch(viewModel.calendarSignInIntent(a))
                            }
                        },
                        onSignOut = { viewModel.signOut() }
                    )
                }
                if (uiState.isSignedIn) {
                    item {
                        Text(
                            "Habits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(uiState.habits, key = { it.id }) { habit ->
                        HabitSyncCard(
                            habit = habit,
                            isSyncing = uiState.isSyncing,
                            onToggle = { viewModel.toggleSync(habit) }
                        )
                    }
                }
            }
        }
    }
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
                        if (isSignedIn) "Turn habits on below to add recurring calendar events"
                        else "Sign in, then enable each habit to create recurring events on your calendar",
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

@Composable
fun HabitSyncCard(habit: Habit, isSyncing: Boolean, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(habit.icon, style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f)) {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    if (habit.calendarSynced) "Synced · recurring event active" else "Not synced",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (habit.calendarSynced) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Switch(checked = habit.calendarSynced, onCheckedChange = { onToggle() })
            }
        }
    }
}