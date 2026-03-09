package com.lm.app.backup;

import com.lm.app.data.LeaveRepository;
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
public final class BackupService_Factory implements Factory<BackupService> {
  private final Provider<LeaveRepository> repositoryProvider;

  private final Provider<GoogleDriveService> driveServiceProvider;

  public BackupService_Factory(Provider<LeaveRepository> repositoryProvider,
      Provider<GoogleDriveService> driveServiceProvider) {
    this.repositoryProvider = repositoryProvider;
    this.driveServiceProvider = driveServiceProvider;
  }

  @Override
  public BackupService get() {
    return newInstance(repositoryProvider.get(), driveServiceProvider.get());
  }

  public static BackupService_Factory create(Provider<LeaveRepository> repositoryProvider,
      Provider<GoogleDriveService> driveServiceProvider) {
    return new BackupService_Factory(repositoryProvider, driveServiceProvider);
  }

  public static BackupService newInstance(LeaveRepository repository,
      GoogleDriveService driveService) {
    return new BackupService(repository, driveService);
  }
}
