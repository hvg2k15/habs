package com.habs.presentation.today

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habs.domain.model.Frequency
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitWithCompletion
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TodayTopBar(
                completedCount = uiState.completedCount,
                totalCount = uiState.totalCount
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add habit") },
                text = { Text("New habit") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        bottomBar = {
            HabsBottomBar(
                selectedIndex = 0,
                onNavigateToToday = {},
                onNavigateToStats = onNavigateToStats,
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        snackbarHost = {
            uiState.toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2800)
                    viewModel.dismissToast()
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissToast() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.primaryContainer)
                        }
                    }
                ) { Text(msg) }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    ProgressSection(
                        completed = uiState.completedCount,
                        total = uiState.totalCount
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Today's habits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(uiState.habits, key = { it.habit.id }) { hwc ->
                    HabitCard(
                        habitWithCompletion = hwc,
                        onToggle = { viewModel.toggleHabit(hwc.habit.id) },
                        onCalendarSync = { viewModel.toggleCalendarSync(hwc.habit) },
                        onDelete = { viewModel.deleteHabit(hwc.habit) }
                    )
                }
                if (uiState.habits.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌱", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No habits yet", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Tap + to add your first habit",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddHabitBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { habit ->
                viewModel.addNewHabit(habit)
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTopBar(completedCount: Int, totalCount: Int) {
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    TopAppBar(
        title = {
            Column {
                Text("Habs", style = MaterialTheme.typography.headlineMedium)
                Text(
                    today,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        },
        actions = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                Text(
                    "🔥 $completedCount/$totalCount",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(12.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun ProgressSection(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Today's progress",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "$completed / $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(Modifier.height(6.dp))
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primaryContainer
    )
}

@Composable
fun HabitCard(
    habitWithCompletion: HabitWithCompletion,
    onToggle: () -> Unit,
    onCalendarSync: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitWithCompletion.habit
    val done = habitWithCompletion.completedToday
    val color = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    buildString {
                        append(habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() })
                        if (habit.calendarSynced) append(" · 📅")
                        if (habitWithCompletion.currentStreak > 0) append(" · 🔥 ${habitWithCompletion.currentStreak}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onCalendarSync,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (habit.calendarSynced) Color(0xFFD7F0E3)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Text(if (habit.calendarSynced) "✓" else "📅", fontSize = 13.sp)
            }
            FilledIconButton(
                onClick = onToggle,
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (done) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (done) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (done) Icon(
                    Icons.Default.Check,
                    contentDescription = "Done",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitBottomSheet(onDismiss: () -> Unit, onSave: (Habit) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🏃") }
    var selectedFrequency by remember { mutableStateOf(Frequency.DAILY) }
    var calendarSync by remember { mutableStateOf(true) }

    val icons = listOf("🏃", "💧", "📖", "🧘", "💪", "🎵", "✍️", "🥗", "😴", "🧹")
    val colors = listOf("#6750A4", "#1565C0", "#2E7D32", "#AD1457", "#E65100", "#00695C")
    var selectedColor by remember { mutableStateOf(colors.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New habit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name") },
                placeholder = { Text("e.g. Morning run") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Icon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.take(5).forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (icon == selectedIcon) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    if (icon == selectedIcon) 2.dp else 0.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) { Text(icon, fontSize = 20.sp) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.drop(5).forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (icon == selectedIcon) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    if (icon == selectedIcon) 2.dp else 0.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) { Text(icon, fontSize = 20.sp) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Frequency",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Frequency.entries.forEach { freq ->
                        FilterChip(
                            selected = freq == selectedFrequency,
                            onClick = { selectedFrequency = freq },
                            label = {
                                Text(
                                    when (freq) {
                                        Frequency.DAILY -> "Daily"
                                        Frequency.WEEKDAYS -> "Weekdays"
                                        Frequency.THREE_PER_WEEK -> "3× / week"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sync to Google Calendar", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Add recurring reminders as events",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = calendarSync, onCheckedChange = { calendarSync = it })
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Habit(
                                name = name.trim(),
                                icon = selectedIcon,
                                colorHex = selectedColor,
                                frequency = selectedFrequency,
                                calendarSynced = calendarSync
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                enabled = name.isNotBlank()
            ) { Text("Save habit", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
fun HabsBottomBar(
    selectedIndex: Int,
    onNavigateToToday: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = onNavigateToToday,
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Today") },
            label = { Text("Today") }
        )
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = onNavigateToStats,
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            label = { Text("Stats") }
        )
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = onNavigateToCalendar,
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = onNavigateToSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}