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
public final class GetHabitStatsUseCase_Factory implements Factory<GetHabitStatsUseCase> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  public GetHabitStatsUseCase_Factory(Provider<HabitRepository> habitRepositoryProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
  }

  @Override
  public GetHabitStatsUseCase get() {
    return newInstance(habitRepositoryProvider.get());
  }

  public static GetHabitStatsUseCase_Factory create(
      Provider<HabitRepository> habitRepositoryProvider) {
    return new GetHabitStatsUseCase_Factory(habitRepositoryProvider);
  }

  public static GetHabitStatsUseCase newInstance(HabitRepository habitRepository) {
    return new GetHabitStatsUseCase(habitRepository);
  }
}
