package com.habs.di;

import com.habs.data.local.HabitDao;
import com.habs.data.local.HabsDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideHabitDaoFactory implements Factory<HabitDao> {
  private final Provider<HabsDatabase> dbProvider;

  public DatabaseModule_ProvideHabitDaoFactory(Provider<HabsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public HabitDao get() {
    return provideHabitDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideHabitDaoFactory create(Provider<HabsDatabase> dbProvider) {
    return new DatabaseModule_ProvideHabitDaoFactory(dbProvider);
  }

  public static HabitDao provideHabitDao(HabsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideHabitDao(db));
  }
}
