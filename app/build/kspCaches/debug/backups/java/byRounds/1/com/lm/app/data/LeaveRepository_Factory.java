package com.lm.app.data;

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
public final class LeaveRepository_Factory implements Factory<LeaveRepository> {
  private final Provider<LeaveDao> leaveDaoProvider;

  public LeaveRepository_Factory(Provider<LeaveDao> leaveDaoProvider) {
    this.leaveDaoProvider = leaveDaoProvider;
  }

  @Override
  public LeaveRepository get() {
    return newInstance(leaveDaoProvider.get());
  }

  public static LeaveRepository_Factory create(Provider<LeaveDao> leaveDaoProvider) {
    return new LeaveRepository_Factory(leaveDaoProvider);
  }

  public static LeaveRepository newInstance(LeaveDao leaveDao) {
    return new LeaveRepository(leaveDao);
  }
}
