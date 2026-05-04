package com.habs.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HabsDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao
}
