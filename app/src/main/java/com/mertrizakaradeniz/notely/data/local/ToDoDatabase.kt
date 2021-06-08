package com.mertrizakaradeniz.notely.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mertrizakaradeniz.notely.data.model.ToDo

@Database(entities = [ToDo::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ToDoDatabase: RoomDatabase() {

    abstract fun getToDoDao(): ToDoDao
}