package com.habs.data.repository;

import android.content.Context;
import com.habs.data.remote.GoogleCalendarApi;
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
public final class CalendarRepositoryImpl_Factory implements Factory<CalendarRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<GoogleCalendarApi> calendarApiProvider;

  public CalendarRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<GoogleCalendarApi> calendarApiProvider) {
    this.contextProvider = contextProvider;
    this.calendarApiProvider = calendarApiProvider;
  }

  @Override
  public CalendarRepositoryImpl get() {
    return newInstance(contextProvider.get(), calendarApiProvider.get());
  }

  public static CalendarRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<GoogleCalendarApi> calendarApiProvider) {
    return new CalendarRepositoryImpl_Factory(contextProvider, calendarApiProvider);
  }

  public static CalendarRepositoryImpl newInstance(Context context, GoogleCalendarApi calendarApi) {
    return new CalendarRepositoryImpl(context, calendarApi);
  }
}
