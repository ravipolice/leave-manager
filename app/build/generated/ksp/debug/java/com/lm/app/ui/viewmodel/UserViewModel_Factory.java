package com.lm.app.ui.viewmodel;

import com.lm.app.backup.GoogleDriveService;
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
public final class UserViewModel_Factory implements Factory<UserViewModel> {
  private final Provider<GoogleDriveService> driveServiceProvider;

  public UserViewModel_Factory(Provider<GoogleDriveService> driveServiceProvider) {
    this.driveServiceProvider = driveServiceProvider;
  }

  @Override
  public UserViewModel get() {
    return newInstance(driveServiceProvider.get());
  }

  public static UserViewModel_Factory create(Provider<GoogleDriveService> driveServiceProvider) {
    return new UserViewModel_Factory(driveServiceProvider);
  }

  public static UserViewModel newInstance(GoogleDriveService driveService) {
    return new UserViewModel(driveService);
  }
}
