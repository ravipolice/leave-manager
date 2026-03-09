package com.lm.app.ui.viewmodel;

import com.lm.app.backup.BackupService;
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
public final class BackupViewModel_Factory implements Factory<BackupViewModel> {
  private final Provider<BackupService> backupServiceProvider;

  public BackupViewModel_Factory(Provider<BackupService> backupServiceProvider) {
    this.backupServiceProvider = backupServiceProvider;
  }

  @Override
  public BackupViewModel get() {
    return newInstance(backupServiceProvider.get());
  }

  public static BackupViewModel_Factory create(Provider<BackupService> backupServiceProvider) {
    return new BackupViewModel_Factory(backupServiceProvider);
  }

  public static BackupViewModel newInstance(BackupService backupService) {
    return new BackupViewModel(backupService);
  }
}
