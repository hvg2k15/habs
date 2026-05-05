package com.habs.presentation.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habs.presentation.calendar.AuthCard
import com.habs.presentation.calendar.CalendarViewModel
import com.habs.presentation.theme.habsTonalTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var dailyReminderTime by remember { mutableStateOf("08:00 AM") }
    val calendarAutoSync by viewModel.calendarAutoSyncNewHabits.collectAsState()
    val calendarAutoSyncTasks by viewModel.calendarAutoSyncNewTasks.collectAsState()
    val calendarUiState by calendarViewModel.uiState.collectAsState()
    var weekStartsMonday by remember { mutableStateOf(true) }

    val activity = LocalContext.current as? Activity
    val calendarSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        calendarViewModel.onCalendarSignInResult(result.data)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = habsTonalTopAppBarColors()
            )
        },
        snackbarHost = {
            calendarUiState.message?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2800)
                    calendarViewModel.dismissMessage()
                }
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                SettingsSectionLabel("Notifications")
            }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Daily reminders",
                        subtitle = "Get reminded to complete your habits",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    if (notificationsEnabled) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow(
                            icon = Icons.Default.Schedule,
                            title = "Reminder time",
                            value = dailyReminderTime
                        )
                    }
                }
            }

            item { SettingsSectionLabel("Google Calendar") }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.CalendarMonth,
                        title = "Auto-sync new habits",
                        subtitle = "Automatically sync habits to Calendar when added",
                        checked = calendarAutoSync,
                        onCheckedChange = { viewModel.setCalendarAutoSyncNewHabits(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggleRow(
                        icon = Icons.Default.Assignment,
                        title = "Auto-sync new tasks",
                        subtitle = "Automatically add new tasks as calendar events on their due date",
                        checked = calendarAutoSyncTasks,
                        onCheckedChange = { viewModel.setCalendarAutoSyncNewTasks(it) }
                    )
                }
            }
            item {
                if (calendarUiState.isLoading) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuthCard(
                            isSignedIn = calendarUiState.isSignedIn,
                            onSignIn = {
                                activity?.let { a ->
                                    calendarSignInLauncher.launch(calendarViewModel.calendarSignInIntent(a))
                                }
                            },
                            onSignOut = { calendarViewModel.signOut() }
                        )
                        if (calendarUiState.isSignedIn) {
                            FilledTonalButton(
                                onClick = { calendarViewModel.syncAll() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !calendarUiState.isSyncing,
                                shape = MaterialTheme.shapes.large
                            ) {
                                if (calendarUiState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                } else {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Sync habits & tasks")
                            }
                        }
                    }
                }
            }

            item { SettingsSectionLabel("Display") }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.CalendarViewWeek,
                        title = "Week starts on Monday",
                        subtitle = "Affects the calendar week layout",
                        checked = weekStartsMonday,
                        onCheckedChange = { weekStartsMonday = it }
                    )
                }
            }

            item { SettingsSectionLabel("About") }
            item {
                SettingsCard {
                    SettingsInfoRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        value = "1.0.0"
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsInfoRow(
                        icon = Icons.Default.Code,
                        title = "Package",
                        value = "com.habs"
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionLabel(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
