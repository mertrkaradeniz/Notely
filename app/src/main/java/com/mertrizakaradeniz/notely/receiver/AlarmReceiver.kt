package com.mertrizakaradeniz.notely.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.mertrizakaradeniz.notely.util.Constant.BUNDLE
import com.mertrizakaradeniz.notely.util.createChannel
import com.mertrizakaradeniz.notely.util.sendNotification

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SAAS", "broadcast")
        val bundle = intent?.getBundleExtra(BUNDLE)

        val notificationManager = context?.let {
            ContextCompat.getSystemService(
                it,
                NotificationManager::class.java,
            )
        } as NotificationManager

        notificationManager.createChannel(context, "note_channel", "note")

        if (bundle != null) {
            notificationManager.sendNotification(
                context,
                bundle
            )
        }
    }
}