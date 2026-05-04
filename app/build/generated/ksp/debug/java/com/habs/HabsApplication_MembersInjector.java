package com.habs;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class HabsApplication_MembersInjector implements MembersInjector<HabsApplication> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public HabsApplication_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<HabsApplication> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new HabsApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(HabsApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.habs.HabsApplication.workerFactory")
  public static void injectWorkerFactory(HabsApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
