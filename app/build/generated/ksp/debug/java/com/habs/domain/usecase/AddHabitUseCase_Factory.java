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
public final class AddHabitUseCase_Factory implements Factory<AddHabitUseCase> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  public AddHabitUseCase_Factory(Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
  }

  @Override
  public AddHabitUseCase get() {
    return newInstance(habitRepositoryProvider.get(), calendarRepositoryProvider.get());
  }

  public static AddHabitUseCase_Factory create(Provider<HabitRepository> habitRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    return new AddHabitUseCase_Factory(habitRepositoryProvider, calendarRepositoryProvider);
  }

  public static AddHabitUseCase newInstance(HabitRepository habitRepository,
      CalendarRepository calendarRepository) {
    return new AddHabitUseCase(habitRepository, calendarRepository);
  }
}
