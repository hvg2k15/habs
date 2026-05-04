package com.habs.presentation.stats;

import com.habs.domain.repository.HabitRepository;
import com.habs.domain.usecase.GetHabitStatsUseCase;
import com.habs.domain.usecase.GetOverallStatsUseCase;
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
public final class StatsViewModel_Factory implements Factory<StatsViewModel> {
  private final Provider<GetOverallStatsUseCase> getOverallStatsProvider;

  private final Provider<GetHabitStatsUseCase> getHabitStatsProvider;

  private final Provider<HabitRepository> habitRepositoryProvider;

  public StatsViewModel_Factory(Provider<GetOverallStatsUseCase> getOverallStatsProvider,
      Provider<GetHabitStatsUseCase> getHabitStatsProvider,
      Provider<HabitRepository> habitRepositoryProvider) {
    this.getOverallStatsProvider = getOverallStatsProvider;
    this.getHabitStatsProvider = getHabitStatsProvider;
    this.habitRepositoryProvider = habitRepositoryProvider;
  }

  @Override
  public StatsViewModel get() {
    return newInstance(getOverallStatsProvider.get(), getHabitStatsProvider.get(), habitRepositoryProvider.get());
  }

  public static StatsViewModel_Factory create(
      Provider<GetOverallStatsUseCase> getOverallStatsProvider,
      Provider<GetHabitStatsUseCase> getHabitStatsProvider,
      Provider<HabitRepository> habitRepositoryProvider) {
    return new StatsViewModel_Factory(getOverallStatsProvider, getHabitStatsProvider, habitRepositoryProvider);
  }

  public static StatsViewModel newInstance(GetOverallStatsUseCase getOverallStats,
      GetHabitStatsUseCase getHabitStats, HabitRepository habitRepository) {
    return new StatsViewModel(getOverallStats, getHabitStats, habitRepository);
  }
}
