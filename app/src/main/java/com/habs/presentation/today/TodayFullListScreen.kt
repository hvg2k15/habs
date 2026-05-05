package com.habs.presentation.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habs.presentation.theme.habsTonalTopAppBarColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodayFullListScreen(
    onNavigateBack: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val calendarSyncDefault by viewModel.calendarAutoSyncNewHabits.collectAsStateWithLifecycle()
    val calendarTaskSyncDefault by viewModel.calendarAutoSyncNewTasks.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    var showAddSheet by remember { mutableStateOf(false) }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var showAddPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val isViewingToday = uiState.selectedDate == today
    val dateLine = if (isViewingToday) {
        uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    } else {
        uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = {
                    Column(Modifier.clickable { showDatePicker = true }) {
                        Text(
                            "Full list",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            dateLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shiftSelectedDayBy(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
                    }
                    IconButton(onClick = { viewModel.shiftSelectedDayBy(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
                    }
                    if (!isViewingToday) {
                        TextButton(onClick = { viewModel.goToToday() }) { Text("Today") }
                    }
                },
                colors = habsTonalTopAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPicker = true },
                shape = MaterialTheme.shapes.large,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                ),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add habit or task")
            }
        }
    ) { padding ->
        DaySwipePagerShell(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            pagerState = pagerState,
            onEdgeSettled = { viewModel.shiftSelectedDayBy(it) }
        ) {
            if (uiState.isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        ProgressSection(
                            viewingDate = uiState.selectedDate,
                            completed = uiState.completedCount,
                            total = uiState.totalCount,
                            tasksDone = uiState.tasksDoneCount,
                            tasksTotal = uiState.tasksTotalCount
                        )
                    }
                    item {
                        HabitSectionHeader(
                            onAddHabit = {
                                showAddPicker = false
                                showAddSheet = true
                            }
                        )
                    }
                    items(uiState.habits, key = { "h${it.habit.id}" }) { hwc ->
                        HabitCard(
                            habitWithCompletion = hwc,
                            onToggle = { viewModel.toggleHabit(hwc.habit.id) },
                            onDelete = { viewModel.removeHabit(hwc.habit) }
                        )
                    }
                    if (uiState.habits.isEmpty()) {
                        item { EmptyHabitsHint(uiState.selectedDate) }
                    }
                    item {
                        TaskSectionHeader(
                            onAddTask = {
                                showAddPicker = false
                                showAddTaskSheet = true
                            }
                        )
                    }
                    items(uiState.tasks, key = { "t${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            viewingDate = uiState.selectedDate,
                            onToggle = { viewModel.toggleTaskDone(task) },
                            onDelete = { viewModel.removeTask(task) }
                        )
                    }
                    if (uiState.tasks.isEmpty()) {
                        item { EmptyTasksHint(uiState.selectedDate) }
                    }
                }
            }
        }
    }

    if (showAddPicker) {
        AddOrPickBottomSheet(
            onDismiss = { showAddPicker = false },
            onPickHabit = {
                showAddPicker = false
                showAddSheet = true
            },
            onPickTask = {
                showAddPicker = false
                showAddTaskSheet = true
            }
        )
    }
    if (showAddSheet) {
        AddHabitBottomSheet(
            defaultCalendarSynced = calendarSyncDefault,
            onDismiss = { showAddSheet = false },
            onSave = { habit ->
                viewModel.addNewHabit(habit)
                showAddSheet = false
            }
        )
    }
    if (showAddTaskSheet) {
        AddTaskBottomSheet(
            defaultDueDate = uiState.selectedDate,
            defaultCalendarSynced = calendarTaskSyncDefault,
            onDismiss = { showAddTaskSheet = false },
            onSave = { title, dueDate, dueTime, calendarSynced ->
                viewModel.addNewTask(title, dueDate, dueTime, calendarSynced)
                showAddTaskSheet = false
            }
        )
    }

    if (showDatePicker) {
        key(uiState.selectedDate) {
            val initialMillis = localDateToMaterialDatePickerMillis(uiState.selectedDate)
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = pickerState.selectedDateMillis ?: initialMillis
                            viewModel.setSelectedDate(materialDatePickerMillisToLocalDate(millis))
                            showDatePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }
}
