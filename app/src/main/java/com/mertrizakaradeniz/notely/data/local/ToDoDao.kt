package com.mertrizakaradeniz.notely.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mertrizakaradeniz.notely.data.model.ToDo

@Dao
interface ToDoDao {

    @Query("SELECT * FROM todo_table ORDER BY id DESC")
    fun getAllData(): LiveData<List<ToDo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(toDoData: ToDo)

    @Update
    suspend fun updateData(toDoData: ToDo)

    @Delete
    suspend fun deleteItem(toDoData: ToDo)

    @Query("DELETE FROM todo_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM todo_table WHERE title LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): LiveData<List<ToDo>>

    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortByHighPriority(): LiveData<List<ToDo>>

    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'L%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'H%' THEN 3 END")
    fun sortByLowPriority(): LiveData<List<ToDo>>
}