package com.habs.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import com.habs.domain.model.HabitStats
import com.habs.domain.model.habitCreatedLocalDate
import com.habs.domain.model.isScheduledOn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

internal val HeatmapDateKeyFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

internal fun mondayColumnIndex(d: LocalDate): Int =
    (d.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7

/** Rows of the weekday grid for one calendar month (excluding the DOW header row). */
internal fun heatmapWeekRowsForMonth(ym: YearMonth): Int {
    val first = ym.atDay(1)
    val padStart = mondayColumnIndex(first)
    val lastDay = ym.lengthOfMonth()
    val totalCells = padStart + lastDay
    return (totalCells + 6) / 7
}

/** Minimum width used when estimating how many months fit across (with [HeatmapGridGap]). */
internal val HeatmapAdaptiveMinMonthWidth = 80.dp

/** Must match LazyVerticalGrid horizontal/vertical spacedBy for tile math. */
internal val HeatmapGridGap = 3.dp

/**
 * How many month-calendars fit across [maxWidth] at roughly [minMonthWidth] each (+ [gap]).
 */
internal fun idealMonthColumnsForWidth(
    maxWidth: Dp,
    minMonthWidth: Dp = HeatmapAdaptiveMinMonthWidth,
    gap: Dp = HeatmapGridGap,
): Int {
    val w = maxWidth.value
    val minW = minMonthWidth.value
    val g = gap.value
    if (w <= minW) return 1
    return ((w + g) / (minW + g)).toInt().coerceAtLeast(1)
}

/**
 * Columns for the grid. Tile size scales with slot width (÷7 day columns); on phones, forcing
 * 3 months into 3 columns makes tiny cells — cap at 2 columns when width is tight so slots are wider.
 */
internal fun heatmapGridColumnCount(maxWidth: Dp, monthCount: Int): Int {
    if (monthCount <= 1) return 1
    val ideal = idealMonthColumnsForWidth(maxWidth)
    val capped = when {
        monthCount <= 3 -> {
            when {
                maxWidth < 520.dp -> min(ideal, 2)
                maxWidth < 720.dp -> min(ideal, 3)
                else -> min(ideal, monthCount)
            }
        }
        monthCount <= 6 -> {
            // Same idea as 3‑month: 3 skinny columns on phones waste width → tiny day cells.
            when {
                maxWidth < 520.dp -> min(ideal, 2)
                maxWidth < 720.dp -> min(ideal, 3)
                else -> min(ideal, 3)
            }
        }
        else -> {
            when {
                maxWidth < 520.dp -> min(ideal, 2)
                maxWidth < 720.dp -> min(ideal, 3)
                else -> min(ideal, 4)
            }
        }
    }
    return min(capped, monthCount).coerceAtLeast(1)
}

/**
 * Day-tile size for mini-months in a grid ([heatmapGridColumnCount] must match [GridCells.Fixed]).
 *
 * Uses **slot width** only. Taking [min] with a per-row height budget makes 12‑month (many rows)
 * layouts height‑limited → tiny cells centered in wide columns and wasted side/center space.
 * The grid scrolls vertically when needed.
 */
internal fun fitnessDayCellSizeForMonthGrid(
    maxWidth: Dp,
    months: List<YearMonth>,
    gridCols: Int,
    gridGap: Dp = HeatmapGridGap,
): Dp {
    // Match [MonthlyComplianceHeatmap] for cells > 22.dp (day gaps 3.dp once tiles grow).
    val cellGap = 3f
    val colGap = gridGap.value

    val n = months.size
    if (n == 0 || gridCols < 1) return 28.dp

    val wPx = maxWidth.value
    if (wPx <= 0f) return 14.dp

    val slotW = (wPx - (gridCols - 1).coerceAtLeast(0) * colGap) / gridCols.coerceAtLeast(1)
    val cellFromW = (slotW - 6 * cellGap) / 7f

    return cellFromW.coerceIn(8f, 108f).dp
}

internal fun fitnessDayCellSizeForSingleMonth(
    maxWidth: Dp,
    maxHeight: Dp,
    ym: YearMonth,
): Dp {
    val cellGap = 2f
    val rowGap = 2f
    val dowFrac = 0.55f
    val wPx = maxWidth.value
    val hPx = maxHeight.value
    if (wPx <= 0f || hPx <= 0f) return 28.dp
    val cellFromWidth = (wPx - 6 * cellGap) / 7f
    val br = heatmapWeekRowsForMonth(ym)
    val coeff = dowFrac + br
    val constant = max(0, br - 1) * rowGap
    val cellFromHeight = (hPx - constant) / coeff.coerceAtLeast(0.01f)
    return min(cellFromWidth, cellFromHeight).coerceIn(4f, 72f).dp
}

internal enum class ComplianceCell {
    Future,
    BeforeHabit,
    NotScheduled,
    Done,
    Missed
}

internal fun onColorForAccent(accent: Color): Color {
    val l = 0.2126f * accent.red + 0.7152f * accent.green + 0.0722f * accent.blue
    return if (l > 0.55f) Color(0xFF1D1B20) else Color.White
}

internal fun complianceCell(
    habitStats: HabitStats,
    d: LocalDate,
    today: LocalDate
): ComplianceCell {
    val habit = habitStats.habit
    if (d.isAfter(today)) return ComplianceCell.Future
    if (d.isBefore(habitCreatedLocalDate(habit))) return ComplianceCell.BeforeHabit
    if (!habit.isScheduledOn(d)) return ComplianceCell.NotScheduled
    val key = d.format(HeatmapDateKeyFmt)
    return if (habitStats.dailyCompletions[key] == true) ComplianceCell.Done else ComplianceCell.Missed
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ComplianceLegend(
    habitColor: Color,
    notActiveColor: Color
) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LegendSwatch(habitColor, "Done")
        LegendSwatch(MaterialTheme.colorScheme.error.copy(alpha = 0.45f), "Missed")
        LegendSwatch(MaterialTheme.colorScheme.surfaceContainerHigh, "Off schedule")
        LegendSwatch(notActiveColor, "Not active")
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun MonthlyComplianceHeatmap(
    habitStats: HabitStats,
    viewMonth: YearMonth,
    today: LocalDate,
    habitColor: Color,
    /** Shrinks when more months share the same viewport (Fitness-style). */
    cellSize: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val ym = viewMonth
    val first = ym.atDay(1)
    val lastDayOfMonth = ym.lengthOfMonth()
    val padStart = mondayColumnIndex(first)
    val totalCells = padStart + lastDayOfMonth
    val rows = (totalCells + 6) / 7

    val cellGap = if (cellSize <= 22.dp) 2.dp else 3.dp
    val rowGap = if (cellSize <= 22.dp) 3.dp else 4.dp
    val corner = (cellSize.value / 4.5f).dp.coerceIn(3.dp, 6.dp)
    val futureBorder = 0.5.dp
    val beforeBorder = if (cellSize <= 22.dp) 0.5.dp else 1.dp
    val dayFontSize = max(8f, min(12f, cellSize.value * 0.42f)).sp
    val dowFontSize = if (cellSize <= 22.dp) 9.sp else 11.sp

    val gridWidth = cellSize * 7 + cellGap * 6

    Column(
        modifier = modifier.width(gridWidth),
        verticalArrangement = Arrangement.spacedBy(rowGap)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cellGap)
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.width(cellSize),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = dowFontSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        (0 until rows).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cellGap)
            ) {
                (0..6).forEach { col ->
                    val idx = row * 7 + col - padStart + 1
                    val cellModifier = Modifier
                        .size(cellSize)
                        .clip(RoundedCornerShape(corner))
                    when {
                        idx < 1 || idx > lastDayOfMonth -> Box(cellModifier)
                        else -> {
                            val d = ym.atDay(idx)
                            val state = complianceCell(habitStats, d, today)
                            val notActiveBg =
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
                            val bg = when (state) {
                                ComplianceCell.Future -> Color.Transparent
                                ComplianceCell.BeforeHabit -> notActiveBg
                                ComplianceCell.NotScheduled -> MaterialTheme.colorScheme.surfaceContainerHigh
                                ComplianceCell.Done -> habitColor
                                ComplianceCell.Missed -> MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                            }
                            Box(
                                cellModifier
                                    .background(bg)
                                    .then(
                                        if (state == ComplianceCell.Future) Modifier.border(
                                            futureBorder,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            RoundedCornerShape(corner)
                                        ) else if (state == ComplianceCell.BeforeHabit) Modifier.border(
                                            beforeBorder,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                            RoundedCornerShape(corner)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$idx",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = dayFontSize),
                                    fontWeight = if (d == today) FontWeight.Bold else FontWeight.Normal,
                                    color = when (state) {
                                        ComplianceCell.Done -> onColorForAccent(habitColor)
                                        ComplianceCell.Missed -> MaterialTheme.colorScheme.onErrorContainer
                                        ComplianceCell.Future -> MaterialTheme.colorScheme.outline
                                        ComplianceCell.BeforeHabit ->
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DateRangeComplianceHeatmap(
    habitStats: HabitStats,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    today: LocalDate,
    habitColor: Color
) {
    val padStart = mondayColumnIndex(rangeStart)
    val days = ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1
    val totalCells = padStart + days
    val cols = (totalCells + 6) / 7
    val cell = 11.dp

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Each column is one week (Mon → Sun, top to bottom).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            (0 until cols).forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    (0..6).forEach { dow ->
                        val cellIndex = week * 7 + dow
                        val dayOffset = cellIndex - padStart
                        val d = rangeStart.plusDays(dayOffset.toLong())
                        val inRange = dayOffset in 0 until days && !d.isAfter(rangeEnd)
                        val bg = if (!inRange) {
                            Color.Transparent
                        } else {
                            when (complianceCell(habitStats, d, today)) {
                                ComplianceCell.Future -> Color.Transparent
                                ComplianceCell.BeforeHabit ->
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
                                ComplianceCell.NotScheduled -> MaterialTheme.colorScheme.surfaceContainerHigh
                                ComplianceCell.Done -> habitColor
                                ComplianceCell.Missed -> MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                            }
                        }
                        Box(
                            Modifier
                                .size(cell)
                                .clip(RoundedCornerShape(2.dp))
                                .background(bg)
                        )
                    }
                }
            }
        }

        val monthsInRange = buildList {
            var ym = YearMonth.from(rangeStart)
            val lastYm = YearMonth.from(rangeEnd)
            while (!ym.isAfter(lastYm)) {
                add(ym)
                ym = ym.plusMonths(1)
            }
        }
        if (monthsInRange.isNotEmpty()) {
            Text(
                "By month in range",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val monthFmt = DateTimeFormatter.ofPattern("yyyy-MM")
                val rates = monthsInRange.map { ym ->
                    val key = ym.format(monthFmt)
                    key to (habitStats.monthlyRates[key] ?: 0f)
                }
                val maxR = rates.maxOfOrNull { it.second }?.coerceAtLeast(0.05f) ?: 1f
                rates.forEach { (key, rate) ->
                    val h = (6 + 36 * (rate / maxR)).dp
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(22.dp)
                                .height(h)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    lerp(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        habitColor,
                                        rate.coerceIn(0f, 1f)
                                    )
                                )
                        )
                        val label = try {
                            YearMonth.parse(key).month.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                        } catch (_: Exception) {
                            "?"
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HabitDetailTitleBlock(
    habitStats: HabitStats,
    rangeHeadline: String,
    rangeSubline: String,
    /** Tighter typography when the heatmap stacks many months. */
    dense: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(habitStats.habit.icon, fontSize = if (dense) 20.sp else 28.sp)
        Column(Modifier.weight(1f)) {
            Text(
                habitStats.habit.name,
                style = if (dense) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            val pct = (habitStats.completionRate * 100).toInt()
            Text(
                "$rangeHeadline · $pct% · streak ${habitStats.currentStreak}d",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (!dense) {
                Text(
                    rangeSubline,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun HabitDetailHeatmapSection(
    habitStats: HabitStats,
    rangeHeadline: String,
    rangeSubline: String,
    monthGridYearMonth: YearMonth?,
    /** Full month grids for multi-month / YTD presets (oldest → newest). */
    stackedCalendarMonths: List<YearMonth>? = null,
    rangeChartFrom: LocalDate?,
    rangeChartTo: LocalDate?,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habitStats.habit.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val stackedMonths = stackedCalendarMonths.orEmpty()
    val stackedCount = stackedMonths.size
    val monthTitleFmt = DateTimeFormatter.ofPattern("MMMM yyyy")
    val compactMonthFmt = DateTimeFormatter.ofPattern("MMM yyyy")
    val denseHeader = stackedCount > 0
    val notActiveLegend =
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)

    Column(modifier.fillMaxSize()) {
        HabitDetailTitleBlock(
            habitStats = habitStats,
            rangeHeadline = rangeHeadline,
            rangeSubline = rangeSubline,
            dense = denseHeader
        )

        Spacer(Modifier.height(if (denseHeader) 4.dp else 8.dp))

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val gridCols =
                if (stackedMonths.isNotEmpty()) heatmapGridColumnCount(maxWidth, stackedCount) else 1
            val cell = when {
                stackedMonths.isNotEmpty() ->
                    fitnessDayCellSizeForMonthGrid(
                        maxWidth,
                        stackedMonths,
                        gridCols,
                        HeatmapGridGap
                    )
                monthGridYearMonth != null ->
                    fitnessDayCellSizeForSingleMonth(maxWidth, maxHeight, monthGridYearMonth)
                else -> 12.dp
            }
            when {
                monthGridYearMonth != null ->
                    MonthlyComplianceHeatmap(
                        habitStats = habitStats,
                        viewMonth = monthGridYearMonth,
                        today = today,
                        habitColor = habitColor,
                        cellSize = cell,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                stackedMonths.isNotEmpty() ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridCols),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.spacedBy(HeatmapGridGap),
                        verticalArrangement = Arrangement.spacedBy(
                            HeatmapGridGap,
                            Alignment.CenterVertically
                        ),
                        userScrollEnabled = true
                    ) {
                        items(
                            count = stackedMonths.size,
                            key = { stackedMonths[it].toString() }
                        ) { index ->
                            val ym = stackedMonths[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(Alignment.Top)
                            ) {
                                Text(
                                    ym.format(if (stackedCount > 8) compactMonthFmt else monthTitleFmt),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                MonthlyComplianceHeatmap(
                                    habitStats = habitStats,
                                    viewMonth = ym,
                                    today = today,
                                    habitColor = habitColor,
                                    cellSize = cell,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                rangeChartFrom != null && rangeChartTo != null ->
                    DateRangeComplianceHeatmap(
                        habitStats = habitStats,
                        rangeStart = rangeChartFrom,
                        rangeEnd = rangeChartTo,
                        today = today,
                        habitColor = habitColor
                    )
            }
        }

        if (stackedCount > 0) {
            Spacer(Modifier.height(6.dp))
            ComplianceLegend(habitColor = habitColor, notActiveColor = notActiveLegend)
            if (stackedCount <= 6) {
                Text(
                    "Months are in a grid (oldest top-left). Scroll if needed. " +
                        "Light border: before habit existed.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            Spacer(Modifier.height(6.dp))

            ComplianceLegend(habitColor = habitColor, notActiveColor = notActiveLegend)

            if (monthGridYearMonth != null ||
                (rangeChartFrom != null && rangeChartTo != null)
            ) {
                Text(
                    "Bordered light cells: before habit existed. Grays: off-days. Color: scheduled.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
