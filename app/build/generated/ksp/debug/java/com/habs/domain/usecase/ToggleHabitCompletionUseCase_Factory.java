package com.habs.domain.usecase;

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
public final class ToggleHabitCompletionUseCase_Factory implements Factory<ToggleHabitCompletionUseCase> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  public ToggleHabitCompletionUseCase_Factory(Provider<HabitRepository> habitRepositoryProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
  }

  @Override
  public ToggleHabitCompletionUseCase get() {
    return newInstance(habitRepositoryProvider.get());
  }

  public static ToggleHabitCompletionUseCase_Factory create(
      Provider<HabitRepository> habitRepositoryProvider) {
    return new ToggleHabitCompletionUseCase_Factory(habitRepositoryProvider);
  }

  public static ToggleHabitCompletionUseCase newInstance(HabitRepository habitRepository) {
    return new ToggleHabitCompletionUseCase(habitRepository);
  }
}
