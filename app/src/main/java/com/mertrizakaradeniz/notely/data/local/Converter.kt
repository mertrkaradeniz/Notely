package com.mertrizakaradeniz.notely.data.local

import androidx.room.TypeConverter
import com.mertrizakaradeniz.notely.data.model.Priority

class Converter {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }
}