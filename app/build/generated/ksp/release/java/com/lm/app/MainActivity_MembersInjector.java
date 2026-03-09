package com.lm.app;

import com.lm.app.backup.BackupService;
import com.lm.app.data.repository.AuthRepository;
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

  private final Provider<AuthRepository> authRepositoryProvider;

  public MainActivity_MembersInjector(Provider<BackupService> backupServiceProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.backupServiceProvider = backupServiceProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<BackupService> backupServiceProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new MainActivity_MembersInjector(backupServiceProvider, authRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectBackupService(instance, backupServiceProvider.get());
    injectAuthRepository(instance, authRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.lm.app.MainActivity.backupService")
  public static void injectBackupService(MainActivity instance, BackupService backupService) {
    instance.backupService = backupService;
  }

  @InjectedFieldSignature("com.lm.app.MainActivity.authRepository")
  public static void injectAuthRepository(MainActivity instance, AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }
}
