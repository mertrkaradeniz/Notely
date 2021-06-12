package com.mertrizakaradeniz.notely.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.ui.main.MainActivity
import com.mertrizakaradeniz.notely.util.Constant.NOTIFICATION_BUNDLE
import com.mertrizakaradeniz.notely.util.Constant.NOTIFICATION_ID

fun NotificationManager.sendNotification(
    applicationContext: Context,
    bundle: Bundle
) {
    val toDo = bundle.getParcelable<ToDo>(NOTIFICATION_BUNDLE)
    val contentPendingIntent = NavDeepLinkBuilder(applicationContext)
        .setComponentName(MainActivity::class.java)
        .setArguments(bundle)
        .setGraph(R.navigation.nav_graph)
        .setDestination(R.id.updateFragment)
        .createPendingIntent()

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.note_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(toDo?.title)
        .setContentText(toDo?.description)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.createChannel(
    applicationContext: Context,
    channelId: String,
    channelName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(false)
        }
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = "Word Reminder"

        val notificationManager = applicationContext.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}