package com.habs.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habs.domain.model.HabitStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * One ring per week (Mon → Sun); each segment is a day in [rangeStart, rangeEnd].
 * Weeks that overlap the range are shown in a horizontal strip (scroll when many).
 */
@Composable
internal fun HabitWeekRingsStrip(
    habitStats: HabitStats,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    habitColor: Color,
    modifier: Modifier = Modifier,
    ringSize: Dp = 52.dp,
    strokeWidth: Dp = 5.5.dp,
) {
    val today = LocalDate.now()
    val weeks = remember(rangeStart, rangeEnd) { weekStartsInRange(rangeStart, rangeEnd) }
    if (weeks.isEmpty()) return

    val weekLabelFmt = DateTimeFormatter.ofPattern("MMM d")
    val scheme = MaterialTheme.colorScheme
    val track = scheme.surfaceContainerHighest
    val notActive = scheme.surfaceContainerHighest.copy(alpha = 0.85f)
    val offSchedule = scheme.surfaceContainerHigh
    val missed = scheme.error.copy(alpha = 0.45f)
    val futureTint = track.copy(alpha = 0.35f)

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        items(weeks, key = { it.toString() }) { weekMonday ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(ringSize + 8.dp)
            ) {
                WeekComplianceRing(
                    weekMonday = weekMonday,
                    habitStats = habitStats,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    today = today,
                    habitColor = habitColor,
                    track = track,
                    notActive = notActive,
                    offSchedule = offSchedule,
                    missed = missed,
                    futureTint = futureTint,
                    modifier = Modifier.size(ringSize),
                    strokeWidth = strokeWidth
                )
                Text(
                    weekMonday.format(weekLabelFmt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(28.dp)
                )
            }
        }
    }
}

private fun weekStartsInRange(rangeStart: LocalDate, rangeEnd: LocalDate): List<LocalDate> {
    if (rangeEnd.isBefore(rangeStart)) return emptyList()
    val firstMonday = rangeStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val lastSunday = rangeEnd.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val out = ArrayList<LocalDate>()
    var w = firstMonday
    while (!w.isAfter(lastSunday)) {
        if (!w.plusDays(6).isBefore(rangeStart) && !w.isAfter(rangeEnd)) {
            out.add(w)
        }
        w = w.plusWeeks(1)
    }
    return out
}

@Composable
private fun WeekComplianceRing(
    weekMonday: LocalDate,
    habitStats: HabitStats,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    today: LocalDate,
    habitColor: Color,
    track: Color,
    notActive: Color,
    offSchedule: Color,
    missed: Color,
    futureTint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp,
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        val r = (minOf(w, h) / 2f) - strokePx / 2f
        val oval = Rect(c.x - r, c.y - r, c.x + r, c.y + r)
        val sweepPer = 360f / 7f
        val gap = 1.8f

        drawArc(
            color = track,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(oval.left, oval.top),
            size = Size(oval.width, oval.height),
            style = Stroke(width = strokePx, cap = StrokeCap.Butt)
        )

        for (i in 0 until 7) {
            val d = weekMonday.plusDays(i.toLong())
            val state = when {
                d.isBefore(rangeStart) || d.isAfter(rangeEnd) -> SegmentState.OutOfRange
                else -> complianceCell(habitStats, d, today).toSegmentState()
            }
            val color = state.toColor(
                habitColor = habitColor,
                notActive = notActive,
                track = track,
                offSchedule = offSchedule,
                missed = missed,
                futureTint = futureTint
            )
            drawArc(
                color = color,
                startAngle = -90f + i * sweepPer + gap,
                sweepAngle = (sweepPer - 2f * gap).coerceAtLeast(0.5f),
                useCenter = false,
                topLeft = Offset(oval.left, oval.top),
                size = Size(oval.width, oval.height),
                style = Stroke(width = strokePx, cap = StrokeCap.Butt)
            )
        }
    }
}

private enum class SegmentState {
    OutOfRange,
    Future,
    BeforeHabit,
    NotScheduled,
    Done,
    Missed
}

private fun ComplianceCell.toSegmentState(): SegmentState = when (this) {
    ComplianceCell.Future -> SegmentState.Future
    ComplianceCell.BeforeHabit -> SegmentState.BeforeHabit
    ComplianceCell.NotScheduled -> SegmentState.NotScheduled
    ComplianceCell.Done -> SegmentState.Done
    ComplianceCell.Missed -> SegmentState.Missed
}

private fun SegmentState.toColor(
    habitColor: Color,
    notActive: Color,
    track: Color,
    offSchedule: Color,
    missed: Color,
    futureTint: Color,
): Color = when (this) {
    SegmentState.OutOfRange -> Color.Transparent
    SegmentState.Future -> futureTint
    SegmentState.BeforeHabit -> notActive
    SegmentState.NotScheduled -> offSchedule
    SegmentState.Done -> habitColor
    SegmentState.Missed -> missed
}
