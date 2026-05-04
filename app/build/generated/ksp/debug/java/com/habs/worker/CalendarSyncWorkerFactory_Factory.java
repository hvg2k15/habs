package com.habs.worker;

import com.habs.domain.repository.CalendarRepository;
import com.habs.domain.repository.HabitRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class CalendarSyncWorkerFactory_Factory implements Factory<CalendarSyncWorkerFactory> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  public CalendarSyncWorkerFactory_Factory(Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
  }

  @Override
  public CalendarSyncWorkerFactory get() {
    return newInstance(habitRepositoryProvider.get(), calendarRepositoryProvider.get());
  }

  public static CalendarSyncWorkerFactory_Factory create(
      Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    return new CalendarSyncWorkerFactory_Factory(habitRepositoryProvider, calendarRepositoryProvider);
  }

  public static CalendarSyncWorkerFactory newInstance(HabitRepository habitRepository,
      CalendarRepository calendarRepository) {
    return new CalendarSyncWorkerFactory(habitRepository, calendarRepository);
  }
}
