package com.habs.presentation.today

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habs.domain.model.Frequency
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitRepeatDraft
import com.habs.domain.model.HabitWithCompletion
import com.habs.domain.model.Task
import com.habs.domain.model.repeatSummary
import com.habs.presentation.theme.habsTonalTopAppBarColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val TodayPreviewMaxItems = 8

/** Shared compact layout for [HabitCard] / [TaskCard]. */
private val ListItemCardPaddingH = 10.dp
private val ListItemCardPaddingV = 7.dp
private val ListItemRowGap = 8.dp
private val ListItemAccentH = 40.dp
private val ListItemIconBox = 36.dp
private val ListItemActionSize = 40.dp
private val ListItemActionGap = 4.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateFullList: () -> Unit = {},
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

    val usePreview = uiState.habits.size + uiState.tasks.size > TodayPreviewMaxItems
    val previewHabits = if (usePreview) uiState.habits.take(4) else uiState.habits
    val previewTasks = if (usePreview) uiState.tasks.take(4) else uiState.tasks

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TodayTopBar(
                selectedDate = uiState.selectedDate,
                onDateLineClick = { showDatePicker = true },
                onGoToToday = { viewModel.goToToday() },
                onPrevDay = { viewModel.shiftSelectedDayBy(-1) },
                onNextDay = { viewModel.shiftSelectedDayBy(1) },
                completedCount = uiState.completedCount,
                totalCount = uiState.totalCount
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
        },
        bottomBar = {
            HabsBottomBar(
                selectedIndex = 0,
                onNavigateToToday = {},
                onNavigateToStats = onNavigateToStats,
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
                    shape = MaterialTheme.shapes.medium,
                    action = {
                        TextButton(onClick = { viewModel.dismissToast() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) { Text(msg) }
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    items(previewHabits, key = { "h${it.habit.id}" }) { hwc ->
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
                    items(previewTasks, key = { "t${it.id}" }) { task ->
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
                    if (usePreview) {
                        item {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onNavigateFullList),
                                shape = MaterialTheme.shapes.large,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "View full list",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${uiState.habits.size} habits · ${uiState.tasks.size} tasks — not all fit here",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTopBar(
    selectedDate: LocalDate,
    onDateLineClick: () -> Unit,
    onGoToToday: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    completedCount: Int,
    totalCount: Int
) {
    val today = LocalDate.now()
    val isViewingToday = selectedDate == today
    val dateLine = if (isViewingToday) {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    } else {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
    }
    TopAppBar(
        title = {
            Column(
                Modifier
                    .clickable(onClick = onDateLineClick)
                    .padding(end = 4.dp)
            ) {
                Text(
                    "Habs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    dateLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(onClick = onPrevDay, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
            }
            IconButton(onClick = onNextDay, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
            if (!isViewingToday) {
                TextButton(onClick = onGoToToday) { Text("Today", maxLines = 1) }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Text(
                    "🔥 $completedCount/$totalCount",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(4.dp))
        },
        colors = habsTonalTopAppBarColors()
    )
}

@Composable
fun ProgressSection(
    viewingDate: LocalDate,
    completed: Int,
    total: Int,
    tasksDone: Int,
    tasksTotal: Int
) {
    val today = LocalDate.now()
    val dayPhrase = if (viewingDate == today) {
        "today"
    } else {
        viewingDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }
    val habitProgress = if (total > 0) completed.toFloat() / total else 0f
    val taskProgress = if (tasksTotal > 0) tasksDone.toFloat() / tasksTotal else 0f
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                if (viewingDate == today) "Progress today" else "Progress on $dayPhrase",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            CompactProgressRow(
                label = "Habits",
                done = completed,
                outOf = total,
                progress = habitProgress,
                barColor = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            CompactProgressRow(
                label = "Tasks",
                done = tasksDone,
                outOf = tasksTotal,
                progress = taskProgress,
                barColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun CompactProgressRow(
    label: String,
    done: Int,
    outOf: Int,
    progress: Float,
    barColor: Color
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "$done / $outOf",
                style = MaterialTheme.typography.labelSmall,
                color = barColor,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(MaterialTheme.shapes.extraSmall),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun HabitSectionHeader(onAddHabit: () -> Unit) {
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "HABITS",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                "Recurring rhythm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onAddHabit) { Text("Add habit") }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun TaskSectionHeader(onAddTask: () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(22.dp)
            )
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    "TASKS",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Text(
                "One-off to-dos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onAddTask) { Text("Add task") }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun EmptyHabitsHint(viewingDate: LocalDate) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (viewingDate == LocalDate.now()) "No habits today"
                else "No habits on this day",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "When you add habits, they appear here on days they are scheduled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyTasksHint(viewingDate: LocalDate) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "No tasks",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                if (viewingDate == LocalDate.now()) {
                    "Tasks are separate from habits. Due today or earlier show here until done."
                } else {
                    "No tasks due on this day. Pick another date or add a task with this due date."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    viewingDate: LocalDate,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val viewingKey = viewingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val todayKey = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val overdue = !task.isCompleted && task.dueDateKey < todayKey

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove task?") },
            text = { Text("“${task.title}” will be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ListItemCardPaddingH, vertical = ListItemCardPaddingV),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ListItemRowGap)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, ListItemAccentH)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Box(
                modifier = Modifier
                    .size(ListItemIconBox)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
                ) {
                    Text(
                        "TASK",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    buildString {
                        when {
                            overdue -> append("Overdue · due ${task.dueDateKey}")
                            task.dueDateKey != viewingKey -> append("Due ${task.dueDateKey}")
                            viewingDate == LocalDate.now() -> append("Due today")
                            else -> append("Due this day")
                        }
                        task.dueTime?.let { t ->
                            append(" · ")
                            append(
                                t.format(
                                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                                        .withLocale(Locale.getDefault())
                                )
                            )
                        }
                        if (task.calendarSynced) append(" · Calendar")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(ListItemActionGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(ListItemActionSize),
                    shape = MaterialTheme.shapes.medium,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (task.isCompleted) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (task.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed. Tap to undo.",
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Mark complete",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(ListItemActionSize)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Remove task") },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirm = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    defaultDueDate: LocalDate,
    defaultCalendarSynced: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, dueDate: LocalDate, dueTime: LocalTime?, calendarSynced: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(defaultDueDate) }
    LaunchedEffect(defaultDueDate) { dueDate = defaultDueDate }

    var calendarSync by remember(defaultCalendarSynced) { mutableStateOf(defaultCalendarSynced) }
    LaunchedEffect(defaultCalendarSynced) { calendarSync = defaultCalendarSynced }

    var deadlineTimeEnabled by remember { mutableStateOf(true) }
    var deadlineTime by remember { mutableStateOf(LocalTime.of(17, 0)) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }

    val dayFmt = remember { DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy") }
    val timeChipFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val deadlinePresets = remember {
        listOf(
            LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(12, 30),
            LocalTime.of(17, 0), LocalTime.of(18, 0), LocalTime.of(20, 0)
        )
    }

    if (showDueDatePicker) {
        key(dueDate) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = localDateToMaterialDatePickerMillis(dueDate)
            )
            DatePickerDialog(
                onDismissRequest = { showDueDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let { ms ->
                                dueDate = materialDatePickerMillisToLocalDate(ms)
                            }
                            showDueDatePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDueDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }

    if (showDeadlineTimePicker) {
        key(deadlineTime) {
            val pickerState = rememberTimePickerState(
                initialHour = deadlineTime.hour,
                initialMinute = deadlineTime.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showDeadlineTimePicker = false },
                title = { Text("Deadline time") },
                text = { TimePicker(state = pickerState) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deadlineTime = LocalTime.of(pickerState.hour, pickerState.minute)
                            deadlineTimeEnabled = true
                            showDeadlineTimePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeadlineTimePicker = false }) { Text("Cancel") }
                }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(
                "Pick a due date and optional time. Tasks stay off the Stats screen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What do you need to do?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDueDatePicker = true },
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Due date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            dueDate.format(dayFmt),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deadline time", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = deadlineTimeEnabled,
                    onCheckedChange = { deadlineTimeEnabled = it }
                )
            }
            if (deadlineTimeEnabled) {
                Text(
                    deadlineTime.format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { showDeadlineTimePicker = true }) {
                    Text("Choose exact time…")
                }
                Text(
                    "Quick picks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    deadlinePresets.forEach { preset ->
                        FilterChip(
                            selected = deadlineTime == preset,
                            onClick = {
                                deadlineTime = preset
                                deadlineTimeEnabled = true
                            },
                            label = { Text(preset.format(timeChipFmt), fontSize = 12.sp) }
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Sync to Google Calendar", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Due date appears as an event on your calendar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = calendarSync, onCheckedChange = { calendarSync = it })
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            dueDate,
                            if (deadlineTimeEnabled) deadlineTime else null,
                            calendarSync
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                enabled = title.isNotBlank()
            ) {
                Text("Save task", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrPickBottomSheet(
    onDismiss: () -> Unit,
    onPickHabit: () -> Unit,
    onPickTask: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add to Today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(
                "Habits are tracked over time; tasks are simple check-offs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            FilledTonalButton(
                onClick = onPickHabit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.Repeat, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("New habit", fontWeight = FontWeight.SemiBold)
                }
            }
            FilledTonalButton(
                onClick = onPickTask,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.Assignment, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("New task", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithCompletion: HabitWithCompletion,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitWithCompletion.habit
    val done = habitWithCompletion.completedToday
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val color = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove habit?") },
            text = {
                Text(
                    "“${habit.name}” will be removed from Habs. Completion history for this habit is deleted too."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ListItemCardPaddingH, vertical = ListItemCardPaddingV),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ListItemRowGap)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, ListItemAccentH)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .size(ListItemIconBox)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon, fontSize = 19.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.22f)
                ) {
                    Text(
                        "HABIT",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    buildString {
                        append(habit.repeatSummary())
                        habit.executionTime?.let { t ->
                            append(" · ")
                            append(t.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())))
                        }
                        if (habit.calendarSynced) append(" · Calendar")
                        if (habitWithCompletion.currentStreak > 0) append(" · Streak ${habitWithCompletion.currentStreak}d")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(ListItemActionGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(ListItemActionSize),
                    shape = MaterialTheme.shapes.medium,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (done) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (done) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (done) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed. Tap to undo.",
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Mark complete",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(ListItemActionSize)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Remove habit") },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirm = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitBottomSheet(
    defaultCalendarSynced: Boolean,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🏃") }
    var repeatDraft by remember { mutableStateOf(HabitRepeatDraft()) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var calendarSync by remember(defaultCalendarSynced) { mutableStateOf(defaultCalendarSynced) }
    var executionTimeEnabled by remember { mutableStateOf(true) }
    var executionTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var showExecutionTimePicker by remember { mutableStateOf(false) }

    val icons = listOf("🏃", "💧", "📖", "🧘", "💪", "🎵", "✍️", "🥗", "😴", "🧹")
    val colors = listOf("#6750A4", "#1565C0", "#2E7D32", "#AD1457", "#E65100", "#00695C")
    var selectedColor by remember { mutableStateOf(colors.first()) }

    val executionTimePresets = remember {
        listOf(
            LocalTime.of(6, 0), LocalTime.of(7, 0), LocalTime.of(7, 30),
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(12, 0),
            LocalTime.of(12, 30), LocalTime.of(17, 0), LocalTime.of(18, 0),
            LocalTime.of(19, 0), LocalTime.of(20, 0), LocalTime.of(21, 0)
        )
    }
    val timeChipFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }

    if (showExecutionTimePicker) {
        key(executionTime) {
            val pickerState = rememberTimePickerState(
                initialHour = executionTime.hour,
                initialMinute = executionTime.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showExecutionTimePicker = false },
                title = { Text("Time of day") },
                text = { TimePicker(state = pickerState) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            executionTime = LocalTime.of(pickerState.hour, pickerState.minute)
                            executionTimeEnabled = true
                            showExecutionTimePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showExecutionTimePicker = false }) { Text("Cancel") }
                }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        if (showRepeatPicker) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp)
            ) {
                HabitRepeatPickerPane(
                    locale = Locale.getDefault(),
                    draft = repeatDraft,
                    onApply = { repeatDraft = it },
                    onBack = { showRepeatPicker = false }
                )
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRepeatPicker = true },
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Repeat",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            repeatDraft.summary(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Same style as Google Calendar — tap to change",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Execution time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "When you plan to do this habit. Shown on the card and used for Google Calendar events.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set time of day", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = executionTimeEnabled,
                        onCheckedChange = { executionTimeEnabled = it }
                    )
                }
                if (executionTimeEnabled) {
                    Text(
                        executionTime.format(
                            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showExecutionTimePicker = true }) {
                        Text("Choose exact time…")
                    }
                    Text(
                        "Quick picks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        executionTimePresets.forEach { preset ->
                            FilterChip(
                                selected = executionTime == preset,
                                onClick = {
                                    executionTime = preset
                                    executionTimeEnabled = true
                                },
                                label = { Text(preset.format(timeChipFmt), fontSize = 12.sp) }
                            )
                        }
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
                                frequency = repeatDraft.frequency,
                                weeklyDayMask = repeatDraft.weeklyDayMask,
                                repeatIntervalDays = repeatDraft.repeatIntervalDays,
                                repeatAnchorDateKey = repeatDraft.repeatAnchorDateKey,
                                executionTime = if (executionTimeEnabled) executionTime else null,
                                calendarSynced = calendarSync
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                enabled = name.isNotBlank() &&
                    (repeatDraft.frequency != Frequency.CUSTOM ||
                        (repeatDraft.weeklyDayMask != null && repeatDraft.weeklyDayMask != 0))
            ) { Text("Save habit", fontWeight = FontWeight.SemiBold) }
        }
        }
    }
}

@Composable
fun HabsBottomBar(
    selectedIndex: Int,
    onNavigateToToday: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
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
            onClick = onNavigateToSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}