package com.mertrizakaradeniz.notely.di

import android.content.Context
import androidx.room.Room
import com.mertrizakaradeniz.notely.data.local.ToDoDatabase
import com.mertrizakaradeniz.notely.util.Constant.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideToDoDatabase(@ApplicationContext context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context,
            ToDoDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideToDoDao(toDoDatabase: ToDoDatabase) = toDoDatabase.getToDoDao()
}