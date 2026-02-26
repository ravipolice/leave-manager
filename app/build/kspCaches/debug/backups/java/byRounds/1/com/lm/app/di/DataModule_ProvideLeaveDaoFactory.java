package com.lm.app.di;

import com.lm.app.data.AppDatabase;
import com.lm.app.data.LeaveDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_ProvideLeaveDaoFactory implements Factory<LeaveDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideLeaveDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LeaveDao get() {
    return provideLeaveDao(databaseProvider.get());
  }

  public static DataModule_ProvideLeaveDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideLeaveDaoFactory(databaseProvider);
  }

  public static LeaveDao provideLeaveDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideLeaveDao(database));
  }
}
