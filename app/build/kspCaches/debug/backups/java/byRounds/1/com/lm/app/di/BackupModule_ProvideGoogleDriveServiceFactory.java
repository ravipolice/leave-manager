package com.lm.app.di;

import android.content.Context;
import com.lm.app.backup.GoogleDriveService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class BackupModule_ProvideGoogleDriveServiceFactory implements Factory<GoogleDriveService> {
  private final Provider<Context> contextProvider;

  public BackupModule_ProvideGoogleDriveServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GoogleDriveService get() {
    return provideGoogleDriveService(contextProvider.get());
  }

  public static BackupModule_ProvideGoogleDriveServiceFactory create(
      Provider<Context> contextProvider) {
    return new BackupModule_ProvideGoogleDriveServiceFactory(contextProvider);
  }

  public static GoogleDriveService provideGoogleDriveService(Context context) {
    return Preconditions.checkNotNullFromProvides(BackupModule.INSTANCE.provideGoogleDriveService(context));
  }
}
