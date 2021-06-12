package com.mertrizakaradeniz.notely.ui

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.data.repository.ToDoRepository
import com.mertrizakaradeniz.notely.receiver.AlarmReceiver
import com.mertrizakaradeniz.notely.util.Constant.BUNDLE
import com.mertrizakaradeniz.notely.util.Constant.NOTIFICATION_BUNDLE
import com.mertrizakaradeniz.notely.util.Constant.REQUEST_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ToDoViewModel @Inject constructor(
    private val repository: ToDoRepository,
    application: Application
) : AndroidViewModel(application) {

    private val app = application
    private val alarmManager: AlarmManager =
        application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setupNotification(calender: Calendar, toDo: ToDo) {
        val notificationIntent = Intent(app, AlarmReceiver::class.java)
//        notificationIntent.putExtra(NOTIFICATION_TITLE, title)
//        notificationIntent.putExtra(NOTIFICATION_MESSAGE, message)
        val bundle = Bundle().apply {
            putParcelable(NOTIFICATION_BUNDLE, toDo)
        }
        notificationIntent.putExtra(BUNDLE, bundle)
        val notifyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            REQUEST_CODE,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calender.timeInMillis,
            notifyPendingIntent
        )

//        alarmManager.setInexactRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + 1000 * 30,
//            1000 * 30,
//            notifyPendingIntent
//        )

//        alarmManager.setInexactRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY,
//            AlarmManager.INTERVAL_HALF_DAY,
//            notifyPendingIntent
//        )

//        val calendar: Calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, 14)
//        }
//        alarmManager.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.RTC_WAKEUP,
//            notifyPendingIntent
//        )
    }

    fun cancelNotification() {
        //alarmManager.cancel(notifyPendingIntent)
    }

    val gelAllData: LiveData<List<ToDo>> = repository.getAllData
    val sortByHighPriority: LiveData<List<ToDo>> = repository.sortByHighPriority
    val sortByLowPriority: LiveData<List<ToDo>> = repository.sortByLowPriority

    fun insertData(toDo: ToDo) = viewModelScope.launch {
        repository.insertData(toDo)
    }

    fun updateData(toDo: ToDo) = viewModelScope.launch {
        repository.updateData(toDo)
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