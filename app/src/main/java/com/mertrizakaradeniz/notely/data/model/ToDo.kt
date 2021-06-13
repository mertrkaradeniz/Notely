package com.mertrizakaradeniz.notely.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "todo_table")
@Parcelize
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var title: String,
    var dateTime: String,
    var subtitle: String,
    var priority: Priority,
    var noteText: String,
    var imageUrl: String?,
    var color: String,
    var webLink: String?
): Parcelable
