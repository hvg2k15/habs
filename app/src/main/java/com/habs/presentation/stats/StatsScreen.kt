package com.habs.presentation.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habs.domain.model.HabitStats
import com.habs.domain.model.OverallStats
import com.habs.presentation.today.HabsBottomBar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                selectedIndex = 1,
                onNavigateToToday = onNavigateBack,
                onNavigateToStats = {},
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = uiState.period.ordinal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Tab(
                    selected = uiState.period == StatsPeriod.MONTHLY,
                    onClick = { viewModel.setPeriod(StatsPeriod.MONTHLY) },
                    text = { Text("Monthly", color = MaterialTheme.colorScheme.onPrimary) }
                )
                Tab(
                    selected = uiState.period == StatsPeriod.YEARLY,
                    onClick = { viewModel.setPeriod(StatsPeriod.YEARLY) },
                    text = { Text("Yearly", color = MaterialTheme.colorScheme.onPrimary) }
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    uiState.overallStats?.let { stats ->
                        item { MetricGrid(stats) }
                        item { HabitCompletionBars(uiState.habitStats) }
                        if (uiState.period == StatsPeriod.YEARLY) {
                            item { YearlyBarChart(stats) }
                        }
                        item { ActivityHeatmap(uiState.habitStats) }
                    }
                    if (uiState.overallStats == null && !uiState.isLoading) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No data yet.\nStart tracking habits to see stats!",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricGrid(stats: OverallStats) {
    val pct = (stats.averageCompletionRate * 100).toInt()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("Avg completion", "$pct%", Modifier.weight(1f))
        MetricCard("Perfect days", "${stats.perfectDays}", Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("Total check-ins", "${stats.totalCheckIns}", Modifier.weight(1f))
        MetricCard("Best streak", "${stats.bestStreak} days", Modifier.weight(1f))
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HabitCompletionBars(habitStats: List<HabitStats>) {
    if (habitStats.isEmpty()) return
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Habit completion",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            habitStats.forEach { stats ->
                val color = try {
                    Color(android.graphics.Color.parseColor(stats.habit.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                val pct = (stats.completionRate * 100).toInt()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stats.habit.icon, fontSize = 14.sp, modifier = Modifier.width(20.dp))
                    Text(
                        stats.habit.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(80.dp),
                        maxLines = 1
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(stats.completionRate.coerceIn(0f, 1f))
                                .background(color)
                        )
                    }
                    Text(
                        "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun YearlyBarChart(stats: OverallStats) {
    val months = stats.monthlyOverview
    if (months.isEmpty()) return
    val maxRate = months.maxOfOrNull { it.completionRate }?.coerceAtLeast(0.01f) ?: 0.01f

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Monthly completion — ${LocalDate.now().year}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                months.forEach { mo ->
                    val heightFrac = if (mo.completionRate > 0) mo.completionRate / maxRate else 0.04f
                    val isFuture = mo.completionRate == 0f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFrac)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(
                                if (isFuture) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                            )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                months.forEach { mo ->
                    val label = try {
                        YearMonth.parse(mo.monthKey).month
                            .getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    } catch (e: Exception) { "?" }
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityHeatmap(habitStats: List<HabitStats>) {
    val today = LocalDate.now()
    val startDate = today.minusDays(118)
    val allCompletedDates = habitStats.flatMap { it.dailyCompletions.keys }.toSet()
    val purpleLight = Color(0xFFEDE7F6)
    val purpleDark = Color(0xFF6750A4)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Activity heatmap",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(10.dp))
            val weeks = 17
            val dayOffsetStart = startDate.dayOfWeek.value % 7
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                (0..6).forEach { dayOfWeek ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        (0 until weeks).forEach { week ->
                            val dayOffset = week * 7 + dayOfWeek - dayOffsetStart
                            val date = startDate.plusDays(dayOffset.toLong())
                            val isFuture = date.isAfter(today)
                            val isInRange = !date.isBefore(startDate) && !isFuture
                            val dateKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val isDone = dateKey in allCompletedDates
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            !isInRange -> Color.Transparent
                                            isDone -> purpleDark
                                            else -> purpleLight
                                        }
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(0.1f, 0.3f, 0.6f, 0.8f, 1f).forEach { frac ->
                    Box(
                        Modifier.size(12.dp).clip(RoundedCornerShape(2.dp))
                            .background(lerp(purpleLight, purpleDark, frac))
                    )
                }
                Text(
                    "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}