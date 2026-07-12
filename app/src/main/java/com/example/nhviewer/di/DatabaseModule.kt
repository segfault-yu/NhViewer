package com.example.nhviewer.di

import android.content.Context
import androidx.room.Room
import com.example.nhviewer.data.local.NhViewerDatabase
import com.example.nhviewer.data.local.dao.ReadingHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NhViewerDatabase {
        return Room.databaseBuilder(
            context,
            NhViewerDatabase::class.java,
            "nhviewer_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideReadingHistoryDao(db: NhViewerDatabase): ReadingHistoryDao {
        return db.readingHistoryDao
    }
}
