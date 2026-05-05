package com.habs.presentation.today

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Material3 [androidx.compose.material3.DatePicker] encodes [androidx.compose.material3.DatePickerState.selectedDateMillis]
 * as the UTC instant at start of the selected **calendar** day. Converting that instant with the system default zone
 * shifts the calendar date behind for zones ahead of UTC (classic “pick Friday, get Thursday”).
 */
fun materialDatePickerMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

fun localDateToMaterialDatePickerMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
