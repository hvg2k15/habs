package com.habs.presentation.calendar;

import com.habs.domain.repository.CalendarRepository;
import com.habs.domain.repository.HabitRepository;
import com.habs.domain.usecase.SyncHabitToCalendarUseCase;
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
public final class CalendarViewModel_Factory implements Factory<CalendarViewModel> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  private final Provider<SyncHabitToCalendarUseCase> syncHabitProvider;

  public CalendarViewModel_Factory(Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider,
      Provider<SyncHabitToCalendarUseCase> syncHabitProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
    this.syncHabitProvider = syncHabitProvider;
  }

  @Override
  public CalendarViewModel get() {
    return newInstance(habitRepositoryProvider.get(), calendarRepositoryProvider.get(), syncHabitProvider.get());
  }

  public static CalendarViewModel_Factory create(Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider,
      Provider<SyncHabitToCalendarUseCase> syncHabitProvider) {
    return new CalendarViewModel_Factory(habitRepositoryProvider, calendarRepositoryProvider, syncHabitProvider);
  }

  public static CalendarViewModel newInstance(HabitRepository habitRepository,
      CalendarRepository calendarRepository, SyncHabitToCalendarUseCase syncHabit) {
    return new CalendarViewModel(habitRepository, calendarRepository, syncHabit);
  }
}
