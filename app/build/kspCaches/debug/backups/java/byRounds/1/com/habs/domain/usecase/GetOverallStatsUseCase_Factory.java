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
public final class GetOverallStatsUseCase_Factory implements Factory<GetOverallStatsUseCase> {
  private final Provider<HabitRepository> habitRepositoryProvider;

  public GetOverallStatsUseCase_Factory(Provider<HabitRepository> habitRepositoryProvider) {
    this.habitRepositoryProvider = habitRepositoryProvider;
  }

  @Override
  public GetOverallStatsUseCase get() {
    return newInstance(habitRepositoryProvider.get());
  }

  public static GetOverallStatsUseCase_Factory create(
      Provider<HabitRepository> habitRepositoryProvider) {
    return new GetOverallStatsUseCase_Factory(habitRepositoryProvider);
  }

  public static GetOverallStatsUseCase newInstance(HabitRepository habitRepository) {
    return new GetOverallStatsUseCase(habitRepository);
  }
}
