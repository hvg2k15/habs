package com.habs.di;

import com.habs.data.local.CompletionDao;
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
public final class DatabaseModule_ProvideCompletionDaoFactory implements Factory<CompletionDao> {
  private final Provider<HabsDatabase> dbProvider;

  public DatabaseModule_ProvideCompletionDaoFactory(Provider<HabsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CompletionDao get() {
    return provideCompletionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCompletionDaoFactory create(
      Provider<HabsDatabase> dbProvider) {
    return new DatabaseModule_ProvideCompletionDaoFactory(dbProvider);
  }

  public static CompletionDao provideCompletionDao(HabsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCompletionDao(db));
  }
}
