package com.habs.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.habs.domain.model.Frequency
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitCompletion
import com.habs.domain.model.Task
import java.time.LocalTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val frequency: String,
    /** ISO weekdays Mon=bit0 … Sun=bit6; null = use [frequency] only. */
    val weeklyDayMask: Int? = null,
    val repeatIntervalDays: Int? = null,
    val repeatAnchorDateKey: String? = null,
    val executionTimeMinutes: Int?,  // minutes from midnight, null = no preferred time
    val calendarSynced: Boolean = false,
    val calendarEventId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Habit(
        id = id,
        name = name,
        icon = icon,
        colorHex = colorHex,
        frequency = Frequency.valueOf(frequency),
        weeklyDayMask = weeklyDayMask,
        repeatIntervalDays = repeatIntervalDays,
        repeatAnchorDateKey = repeatAnchorDateKey,
        executionTime = executionTimeMinutes?.let { LocalTime.ofSecondOfDay(it * 60L) },
        calendarSynced = calendarSynced,
        calendarEventId = calendarEventId,
        createdAt = createdAt
    )
}

fun Habit.toEntity() = HabitEntity(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex,
    frequency = frequency.name,
    weeklyDayMask = weeklyDayMask,
    repeatIntervalDays = repeatIntervalDays,
    repeatAnchorDateKey = repeatAnchorDateKey,
    executionTimeMinutes = executionTime?.let { it.hour * 60 + it.minute },
    calendarSynced = calendarSynced,
    calendarEventId = calendarEventId,
    createdAt = createdAt
)

@Entity(
    tableName = "completions",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("habitId"), Index("dateKey")]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val dateKey: String
) {
    fun toDomain() = HabitCompletion(id = id, habitId = habitId, completedAt = completedAt, dateKey = dateKey)
}

@Entity(
    tableName = "tasks",
    indices = [Index("dueDateKey"), Index("isCompleted")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDateKey: String,
    val dueTimeMinutes: Int? = null,
    val isCompleted: Boolean = false,
    val completedOnKey: String? = null,
    val calendarSynced: Boolean = false,
    val calendarEventId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Task(
        id = id,
        title = title,
        dueDateKey = dueDateKey,
        dueTime = dueTimeMinutes?.let { LocalTime.ofSecondOfDay(it * 60L) },
        isCompleted = isCompleted,
        completedOnKey = completedOnKey,
        calendarSynced = calendarSynced,
        calendarEventId = calendarEventId,
        createdAt = createdAt
    )
}

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    dueDateKey = dueDateKey,
    dueTimeMinutes = dueTime?.let { it.hour * 60 + it.minute },
    isCompleted = isCompleted,
    completedOnKey = completedOnKey,
    calendarSynced = calendarSynced,
    calendarEventId = calendarEventId,
    createdAt = createdAt
)
