package com.lm.app.ui.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
  @Override
  public UserViewModel get() {
    return newInstance();
  }

  public static UserViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static UserViewModel newInstance() {
    return new UserViewModel();
  }

  private static final class InstanceHolder {
    private static final UserViewModel_Factory INSTANCE = new UserViewModel_Factory();
  }
}
