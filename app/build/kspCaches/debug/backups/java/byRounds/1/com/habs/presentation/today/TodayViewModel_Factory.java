package com.habs.presentation.today;

import com.habs.domain.usecase.AddHabitUseCase;
import com.habs.domain.usecase.DeleteHabitUseCase;
import com.habs.domain.usecase.GetTodayHabitsUseCase;
import com.habs.domain.usecase.SyncHabitToCalendarUseCase;
import com.habs.domain.usecase.ToggleHabitCompletionUseCase;
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
public final class TodayViewModel_Factory implements Factory<TodayViewModel> {
  private final Provider<GetTodayHabitsUseCase> getTodayHabitsProvider;

  private final Provider<ToggleHabitCompletionUseCase> toggleCompletionProvider;

  private final Provider<AddHabitUseCase> addHabitProvider;

  private final Provider<DeleteHabitUseCase> deleteHabitProvider;

  private final Provider<SyncHabitToCalendarUseCase> syncToCalendarProvider;

  public TodayViewModel_Factory(Provider<GetTodayHabitsUseCase> getTodayHabitsProvider,
      Provider<ToggleHabitCompletionUseCase> toggleCompletionProvider,
      Provider<AddHabitUseCase> addHabitProvider, Provider<DeleteHabitUseCase> deleteHabitProvider,
      Provider<SyncHabitToCalendarUseCase> syncToCalendarProvider) {
    this.getTodayHabitsProvider = getTodayHabitsProvider;
    this.toggleCompletionProvider = toggleCompletionProvider;
    this.addHabitProvider = addHabitProvider;
    this.deleteHabitProvider = deleteHabitProvider;
    this.syncToCalendarProvider = syncToCalendarProvider;
  }

  @Override
  public TodayViewModel get() {
    return newInstance(getTodayHabitsProvider.get(), toggleCompletionProvider.get(), addHabitProvider.get(), deleteHabitProvider.get(), syncToCalendarProvider.get());
  }

  public static TodayViewModel_Factory create(
      Provider<GetTodayHabitsUseCase> getTodayHabitsProvider,
      Provider<ToggleHabitCompletionUseCase> toggleCompletionProvider,
      Provider<AddHabitUseCase> addHabitProvider, Provider<DeleteHabitUseCase> deleteHabitProvider,
      Provider<SyncHabitToCalendarUseCase> syncToCalendarProvider) {
    return new TodayViewModel_Factory(getTodayHabitsProvider, toggleCompletionProvider, addHabitProvider, deleteHabitProvider, syncToCalendarProvider);
  }

  public static TodayViewModel newInstance(GetTodayHabitsUseCase getTodayHabits,
      ToggleHabitCompletionUseCase toggleCompletion, AddHabitUseCase addHabit,
      DeleteHabitUseCase deleteHabit, SyncHabitToCalendarUseCase syncToCalendar) {
    return new TodayViewModel(getTodayHabits, toggleCompletion, addHabit, deleteHabit, syncToCalendar);
  }
}
