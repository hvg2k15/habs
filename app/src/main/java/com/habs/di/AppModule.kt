package com.habs.di

import android.content.Context
import androidx.room.Room
import com.habs.data.local.CompletionDao
import com.habs.data.local.HabitDao
import com.habs.data.local.HabsDatabase
import com.habs.data.repository.CalendarRepositoryImpl
import com.habs.data.repository.HabitRepositoryImpl
import com.habs.domain.repository.CalendarRepository
import com.habs.domain.repository.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabsDatabase =
        Room.databaseBuilder(context, HabsDatabase::class.java, "habs.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHabitDao(db: HabsDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideCompletionDao(db: HabsDatabase): CompletionDao = db.completionDao()
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
}
