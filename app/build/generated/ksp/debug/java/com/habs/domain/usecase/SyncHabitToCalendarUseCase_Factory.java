package com.habs.domain.usecase;

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
public final class SyncHabitToCalendarUseCase_Factory implements Factory<SyncHabitToCalendarUseCase> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  public SyncHabitToCalendarUseCase_Factory(Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
  }

  @Override
  public SyncHabitToCalendarUseCase get() {
    return newInstance(habitRepositoryProvider.get(), calendarRepositoryProvider.get());
  }

  public static SyncHabitToCalendarUseCase_Factory create(
      Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    return new SyncHabitToCalendarUseCase_Factory(habitRepositoryProvider, calendarRepositoryProvider);
  }

  public static SyncHabitToCalendarUseCase newInstance(HabitRepository habitRepository,
      CalendarRepository calendarRepository) {
    return new SyncHabitToCalendarUseCase(habitRepository, calendarRepository);
  }
}
