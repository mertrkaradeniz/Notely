package com.mertrizakaradeniz.notely.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.data.repository.ToDoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoViewModel @Inject constructor(
    private val repository: ToDoRepository,
    application: Application
) : AndroidViewModel(application) {

    fun checkIfDatabaseEmpty(toDoData: List<ToDo>) {
        emptyDatabase.value = toDoData.isEmpty()
    }
    val emptyDatabase: MutableLiveData<Boolean> = MutableLiveData(false)

    val gelAllData: LiveData<List<ToDo>> = repository.getAllData
    val sortByHighPriority: LiveData<List<ToDo>> = repository.sortByHighPriority
    val sortByLowPriority: LiveData<List<ToDo>> = repository.sortByLowPriority

    fun upsertNote(toDo: ToDo) = viewModelScope.launch {
        repository.upsertNote(toDo)
    }

    fun deleteItem(toDo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteItem(toDo)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ToDo>> {
        return repository.searchDatabase(searchQuery)
    }
}