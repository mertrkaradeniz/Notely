package com.mertrizakaradeniz.notely.di

import com.mertrizakaradeniz.notely.data.local.ToDoDao
import com.mertrizakaradeniz.notely.data.repository.ToDoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideToDoRepository(
        toDoDao: ToDoDao
    ): ToDoRepository = ToDoRepository(toDoDao)
}