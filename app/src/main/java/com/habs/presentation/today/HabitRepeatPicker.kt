package com.habs.presentation.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.rememberDatePickerState
import com.habs.domain.model.Frequency
import com.habs.domain.model.HabitRepeatDraft
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private fun dayBit(day: DayOfWeek): Int = 1 shl (day.value - 1)

private enum class RepeatEditMode { List, CustomDays, IntervalDays }

private fun isDailySelected(d: HabitRepeatDraft): Boolean =
    d.repeatIntervalDays == null && d.weeklyDayMask == null

private fun isCustomSelected(d: HabitRepeatDraft): Boolean = d.weeklyDayMask != null

private fun isIntervalSelected(d: HabitRepeatDraft): Boolean =
    d.repeatIntervalDays != null && d.repeatIntervalDays >= 2

private val IsoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitRepeatPickerPane(
    locale: Locale,
    draft: HabitRepeatDraft,
    onApply: (HabitRepeatDraft) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(RepeatEditMode.List) }
    var customDraft by remember(draft.weeklyDayMask, draft.repeatIntervalDays) {
        mutableIntStateOf(
            draft.weeklyDayMask?.takeIf { draft.repeatIntervalDays == null }
                ?: (dayBit(DayOfWeek.MONDAY) or dayBit(DayOfWeek.WEDNESDAY))
        )
    }
    var intervalText by remember(draft.repeatIntervalDays) {
        mutableStateOf(draft.repeatIntervalDays?.takeIf { it >= 2 }?.toString() ?: "2")
    }
    var intervalAnchorDate by remember {
        mutableStateOf(
            draft.repeatAnchorDateKey?.let { LocalDate.parse(it, IsoDate) } ?: LocalDate.now()
        )
    }
    var showIntervalAnchorPicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                when (mode) {
                    RepeatEditMode.List -> onBack()
                    RepeatEditMode.CustomDays,
                    RepeatEditMode.IntervalDays -> mode = RepeatEditMode.List
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                when (mode) {
                    RepeatEditMode.List -> "Repeat"
                    RepeatEditMode.CustomDays -> "Custom days"
                    RepeatEditMode.IntervalDays -> "Every N days"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        when (mode) {
            RepeatEditMode.List -> {
                RepeatSelectableRow(
                    title = "Daily",
                    subtitle = "Counts every calendar day",
                    selected = isDailySelected(draft),
                    onClick = {
                        onApply(HabitRepeatDraft())
                        onBack()
                    }
                )
                RepeatSelectableRow(
                    title = "Custom weekdays",
                    subtitle = "Choose which days of the week",
                    selected = isCustomSelected(draft),
                    onClick = {
                        customDraft = draft.weeklyDayMask?.takeIf { draft.repeatIntervalDays == null }
                            ?: (dayBit(DayOfWeek.MONDAY) or dayBit(DayOfWeek.WEDNESDAY))
                        mode = RepeatEditMode.CustomDays
                    }
                )
                RepeatSelectableRow(
                    title = "Every N days",
                    subtitle = "Repeat every 2–365 days from a start date",
                    selected = isIntervalSelected(draft),
                    onClick = {
                        intervalText = draft.repeatIntervalDays?.takeIf { it >= 2 }?.toString() ?: "2"
                        intervalAnchorDate =
                            draft.repeatAnchorDateKey?.let { LocalDate.parse(it, IsoDate) } ?: LocalDate.now()
                        mode = RepeatEditMode.IntervalDays
                    }
                )
            }

            RepeatEditMode.CustomDays -> {
                Text(
                    "Tap the days this habit should appear on Today and in stats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DayOfWeek.values().sortedBy { it.value }.forEach { day ->
                        val bit = dayBit(day)
                        val on = (customDraft and bit) != 0
                        FilterChip(
                            selected = on,
                            onClick = {
                                val toggled = customDraft xor bit
                                customDraft = if (toggled == 0) bit else toggled
                            },
                            label = {
                                Text(
                                    day.getDisplayName(TextStyle.SHORT, locale),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
                Text(
                    "At least one day must stay on.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onApply(
                                HabitRepeatDraft(
                                    frequency = Frequency.CUSTOM,
                                    weeklyDayMask = customDraft,
                                    repeatIntervalDays = null,
                                    repeatAnchorDateKey = null
                                )
                            )
                            mode = RepeatEditMode.List
                            onBack()
                        }
                    ) {
                        Text("Done")
                    }
                }
            }

            RepeatEditMode.IntervalDays -> {
                val dayFmt = remember(locale) {
                    DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy").withLocale(locale)
                }
                Text(
                    "Repeats every N calendar days starting on the date you pick below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { showIntervalAnchorPicker = true },
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
                                "First occurrence",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                intervalAnchorDate.format(dayFmt),
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
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("Number of days between repeats") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val n = intervalText.toIntOrNull()?.coerceIn(2, 365) ?: return@TextButton
                            val anchor = intervalAnchorDate.format(IsoDate)
                            onApply(
                                HabitRepeatDraft(
                                    frequency = Frequency.DAILY,
                                    weeklyDayMask = null,
                                    repeatIntervalDays = n,
                                    repeatAnchorDateKey = anchor
                                )
                            )
                            mode = RepeatEditMode.List
                            onBack()
                        }
                    ) {
                        Text("Done")
                    }
                }
            }
        }

        if (showIntervalAnchorPicker) {
            key(intervalAnchorDate) {
                val initialMillis = localDateToMaterialDatePickerMillis(intervalAnchorDate)
                val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                DatePickerDialog(
                    onDismissRequest = { showIntervalAnchorPicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val millis = pickerState.selectedDateMillis ?: initialMillis
                                intervalAnchorDate = materialDatePickerMillisToLocalDate(millis)
                                showIntervalAnchorPicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showIntervalAnchorPicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = pickerState)
                }
            }
        }
    }
}

@Composable
private fun RepeatSelectableRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
