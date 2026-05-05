package com.habs.presentation.stats

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habs.presentation.theme.habsTonalTopAppBarColors
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun HabitStatsDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenCalendarHeatmap: () -> Unit,
    viewModel: HabitStatsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.habit?.name ?: "Habit",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = habsTonalTopAppBarColors()
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
            uiState.notFound -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "This habit no longer exists.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            uiState.habitStats != null && uiState.habit != null -> {
                val stats = uiState.habitStats!!
                val monthCount = remember { viewModel.monthPagerPageCount() }
                val pagerState = rememberPagerState(
                    initialPage = (monthCount - 1).coerceAtLeast(0),
                    pageCount = { monthCount }
                )
                val monthNavScope = rememberCoroutineScope()

                LaunchedEffect(uiState.preset, uiState.viewMonth, monthCount) {
                    if (uiState.preset != HabitStatsRangePreset.ONE_MONTH) return@LaunchedEffect
                    val target = viewModel.pagerPageForYearMonth(uiState.viewMonth)
                        .coerceIn(0, monthCount - 1)
                    if (pagerState.currentPage != target) {
                        pagerState.scrollToPage(target)
                    }
                }

                LaunchedEffect(pagerState.settledPage, uiState.preset) {
                    if (uiState.preset != HabitStatsRangePreset.ONE_MONTH) return@LaunchedEffect
                    val ym = viewModel.yearMonthForPagerPage(pagerState.settledPage)
                    viewModel.setViewMonth(ym)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Range",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetChip(
                                label = "1 mo",
                                selected = uiState.preset == HabitStatsRangePreset.ONE_MONTH,
                                onClick = { viewModel.setPreset(HabitStatsRangePreset.ONE_MONTH) }
                            )
                            PresetChip(
                                label = "3 mo",
                                selected = uiState.preset == HabitStatsRangePreset.THREE_MONTHS,
                                onClick = { viewModel.setPreset(HabitStatsRangePreset.THREE_MONTHS) }
                            )
                            PresetChip(
                                label = "6 mo",
                                selected = uiState.preset == HabitStatsRangePreset.SIX_MONTHS,
                                onClick = { viewModel.setPreset(HabitStatsRangePreset.SIX_MONTHS) }
                            )
                            PresetChip(
                                label = "12 mo",
                                selected = uiState.preset == HabitStatsRangePreset.TWELVE_MONTHS,
                                onClick = { viewModel.setPreset(HabitStatsRangePreset.TWELVE_MONTHS) }
                            )
                            PresetChip(
                                label = "YTD",
                                selected = uiState.preset == HabitStatsRangePreset.YEAR_TO_DATE,
                                onClick = { viewModel.setPreset(HabitStatsRangePreset.YEAR_TO_DATE) }
                            )
                        }
                    }

                    if (uiState.preset == HabitStatsRangePreset.ONE_MONTH) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Swipe or use arrows to change month",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val p = pagerState.currentPage
                                        if (p > 0) {
                                            monthNavScope.launch {
                                                pagerState.animateScrollToPage(p - 1)
                                            }
                                        }
                                    },
                                    enabled = pagerState.currentPage > 0
                                ) {
                                    Icon(
                                        Icons.Default.ChevronLeft,
                                        contentDescription = "Previous month"
                                    )
                                }
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) { page ->
                                    val ym = viewModel.yearMonthForPagerPage(page)
                                    val label = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                                    Box(
                                        Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val p = pagerState.currentPage
                                        if (p < monthCount - 1) {
                                            monthNavScope.launch {
                                                pagerState.animateScrollToPage(p + 1)
                                            }
                                        }
                                    },
                                    enabled = pagerState.currentPage < monthCount - 1
                                ) {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Next month"
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        val habitColor = try {
                            Color(android.graphics.Color.parseColor(stats.habit.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        val notActiveLegend =
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)

                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            HabitDetailTitleBlock(
                                habitStats = stats,
                                rangeHeadline = uiState.rangeLabel,
                                rangeSubline = "${uiState.windowStart} → ${uiState.windowEnd}",
                                dense = false
                            )
                            Text(
                                "Weeks in range (Mon → Sun)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                            )
                            HabitWeekRingsStrip(
                                habitStats = stats,
                                rangeStart = uiState.windowStart,
                                rangeEnd = uiState.windowEnd,
                                habitColor = habitColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedButton(
                                onClick = onOpenCalendarHeatmap,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Open calendar heatmap")
                            }
                            Spacer(Modifier.weight(1f))
                            ComplianceLegend(
                                habitColor = habitColor,
                                notActiveColor = notActiveLegend
                            )
                            Text(
                                "Ring segments follow the heatmap legend. Open the heatmap for month grids.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

