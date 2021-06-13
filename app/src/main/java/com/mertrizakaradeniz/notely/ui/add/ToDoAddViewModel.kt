package com.mertrizakaradeniz.notely.ui.add

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.Priority
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.data.repository.ToDoRepository
import com.mertrizakaradeniz.notely.receiver.AlarmReceiver
import com.mertrizakaradeniz.notely.util.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ToDoAddViewModel @Inject constructor(
    private val repository: ToDoRepository,
    application: Application
) : AndroidViewModel(application) {

    private val app = application
    private val alarmManager: AlarmManager =
        application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var notifyPendingIntent: PendingIntent? = null

    fun setupNotification(calender: Calendar, toDo: ToDo) {
        val notificationIntent = Intent(app, AlarmReceiver::class.java)
        val bundle = Bundle().apply {
            putParcelable(Constant.NOTIFICATION_BUNDLE, toDo)
        }
        notificationIntent.putExtra(Constant.BUNDLE, bundle)
        notifyPendingIntent = PendingIntent.getBroadcast(
            app,
            Constant.REQUEST_CODE,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            notifyPendingIntent
        )
    }

    fun cancelNotification() {
        if (notifyPendingIntent != null) {
            alarmManager.cancel(notifyPendingIntent)
        }
    }

    fun upsertNote(toDo: ToDo) = viewModelScope.launch {
        repository.upsertNote(toDo)
    }

    fun deleteItem(toDo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteItem(toDo)
    }

    val listener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when (position) {
                0 -> {
                    (parent!!.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(
                            application,
                            R.color.dark_red
                        )
                    )
                }
                1 -> {
                    (parent!!.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(
                            application,
                            R.color.dark_yellow
                        )
                    )
                }
                2 -> {
                    (parent!!.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(
                            application,
                            R.color.dark_green
                        )
                    )
                }
            }
        }
    }

    fun verifyDataFromUser(title: String, subtitle: String, description: String): Boolean {
        return !(title.isEmpty() || subtitle.isEmpty() || description.isEmpty())
    }

    fun parsePriority(priority: String): Priority {
        return when (priority) {
            "High Priority" -> {
                Priority.HIGH
            }
            "Medium Priority" -> {
                Priority.MEDIUM
            }
            "Low Priority" -> {
                Priority.LOW
            }
            else -> Priority.LOW
        }
    }
}