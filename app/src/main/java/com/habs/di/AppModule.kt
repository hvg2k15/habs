package com.habs.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habs.data.local.CompletionDao
import com.habs.data.local.HabitDao
import com.habs.data.local.HabsDatabase
import com.habs.data.local.TaskDao
import com.habs.data.repository.CalendarRepositoryImpl
import com.habs.data.repository.HabitRepositoryImpl
import com.habs.data.repository.TaskRepositoryImpl
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import com.habs.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN calendarSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN calendarEventId TEXT")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabsDatabase =
        Room.databaseBuilder(context, HabsDatabase::class.java, "habs.db")
            .addMigrations(MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHabitDao(db: HabsDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideCompletionDao(db: HabsDatabase): CompletionDao = db.completionDao()

    @Provides
    fun provideTaskDao(db: HabsDatabase): TaskDao = db.taskDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
