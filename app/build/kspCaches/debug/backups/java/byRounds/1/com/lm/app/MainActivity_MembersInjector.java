package com.lm.app;

import com.lm.app.backup.BackupService;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<BackupService> backupServiceProvider;

  public MainActivity_MembersInjector(Provider<BackupService> backupServiceProvider) {
    this.backupServiceProvider = backupServiceProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<BackupService> backupServiceProvider) {
    return new MainActivity_MembersInjector(backupServiceProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectBackupService(instance, backupServiceProvider.get());
  }

  @InjectedFieldSignature("com.lm.app.MainActivity.backupService")
  public static void injectBackupService(MainActivity instance, BackupService backupService) {
    instance.backupService = backupService;
  }
}
