package com.mertrizakaradeniz.notely.data.repository

import androidx.lifecycle.LiveData
import com.mertrizakaradeniz.notely.data.local.ToDoDao
import com.mertrizakaradeniz.notely.data.model.ToDo
import javax.inject.Inject

class ToDoRepository @Inject constructor(
    private val toDoDao: ToDoDao
) {
    val getAllData: LiveData<List<ToDo>> = toDoDao.getAllData()
    val sortByHighPriority: LiveData<List<ToDo>> = toDoDao.sortByHighPriority()
    val sortByLowPriority: LiveData<List<ToDo>> = toDoDao.sortByLowPriority()

    suspend fun upsertNote(toDoData: ToDo) {
        toDoDao.upsertNote(toDoData)
    }

    suspend fun deleteItem(toDoData: ToDo) {
        toDoDao.deleteItem(toDoData)
    }

    suspend fun deleteAll() {
        toDoDao.deleteAll()
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ToDo>> {
        return toDoDao.searchDatabase(searchQuery)
    }
}