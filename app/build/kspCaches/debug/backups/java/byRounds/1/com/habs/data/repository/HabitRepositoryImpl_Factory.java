package com.habs.data.repository;

import com.habs.data.local.CompletionDao;
import com.habs.data.local.HabitDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class HabitRepositoryImpl_Factory implements Factory<HabitRepositoryImpl> {
  private final Provider<HabitDao> habitDaoProvider;

  private final Provider<CompletionDao> completionDaoProvider;

  public HabitRepositoryImpl_Factory(Provider<HabitDao> habitDaoProvider,
      Provider<CompletionDao> completionDaoProvider) {
    this.habitDaoProvider = habitDaoProvider;
    this.completionDaoProvider = completionDaoProvider;
  }

  @Override
  public HabitRepositoryImpl get() {
    return newInstance(habitDaoProvider.get(), completionDaoProvider.get());
  }

  public static HabitRepositoryImpl_Factory create(Provider<HabitDao> habitDaoProvider,
      Provider<CompletionDao> completionDaoProvider) {
    return new HabitRepositoryImpl_Factory(habitDaoProvider, completionDaoProvider);
  }

  public static HabitRepositoryImpl newInstance(HabitDao habitDao, CompletionDao completionDao) {
    return new HabitRepositoryImpl(habitDao, completionDao);
  }
}
