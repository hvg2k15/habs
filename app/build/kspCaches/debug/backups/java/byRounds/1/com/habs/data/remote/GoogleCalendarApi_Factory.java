package com.habs.data.remote;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class GoogleCalendarApi_Factory implements Factory<GoogleCalendarApi> {
  private final Provider<Context> contextProvider;

  public GoogleCalendarApi_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GoogleCalendarApi get() {
    return newInstance(contextProvider.get());
  }

  public static GoogleCalendarApi_Factory create(Provider<Context> contextProvider) {
    return new GoogleCalendarApi_Factory(contextProvider);
  }

  public static GoogleCalendarApi newInstance(Context context) {
    return new GoogleCalendarApi(context);
  }
}
