package com.habs.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.habs.domain.model.Frequency
import com.habs.domain.model.Habit
import com.habs.domain.model.HabitCompletion
import java.time.LocalTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val frequency: String,
    val reminderTimeMinutes: Int?,  // minutes from midnight, null = no reminder
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
        reminderTime = reminderTimeMinutes?.let { LocalTime.ofSecondOfDay(it * 60L) },
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
    reminderTimeMinutes = reminderTime?.let { it.hour * 60 + it.minute },
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
