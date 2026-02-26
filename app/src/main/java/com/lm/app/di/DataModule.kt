package com.lm.app.di

import android.content.Context
import androidx.room.Room
import com.lm.app.data.AppDatabase
import com.lm.app.data.LeaveDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "leave_manager.db"
        ).build()
    }

    @Provides
    fun provideLeaveDao(database: AppDatabase): LeaveDao {
        return database.leaveDao()
    }
}
