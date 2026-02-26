package com.lm.app.ui.viewmodel;

import com.lm.app.data.LeaveRepository;
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
public final class LeaveViewModel_Factory implements Factory<LeaveViewModel> {
  private final Provider<LeaveRepository> leaveRepositoryProvider;

  public LeaveViewModel_Factory(Provider<LeaveRepository> leaveRepositoryProvider) {
    this.leaveRepositoryProvider = leaveRepositoryProvider;
  }

  @Override
  public LeaveViewModel get() {
    return newInstance(leaveRepositoryProvider.get());
  }

  public static LeaveViewModel_Factory create(Provider<LeaveRepository> leaveRepositoryProvider) {
    return new LeaveViewModel_Factory(leaveRepositoryProvider);
  }

  public static LeaveViewModel newInstance(LeaveRepository leaveRepository) {
    return new LeaveViewModel(leaveRepository);
  }
}
