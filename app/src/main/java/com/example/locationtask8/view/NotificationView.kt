package com.example.locationtask8.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.app.NotificationCompat


import com.example.locationtask8.R
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.broadcast_receiver.FinishAppBroadcastReceiver
import javax.inject.Inject


class NotificationView @Inject constructor() {
    @Inject
    lateinit var context: Context
    private val NOTIFICATION_CHANNEL_ID = "com.example.locationtask8.viewmodel"
    private lateinit var notificationManager: NotificationManager
    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

    fun locationInfoNotification(msg:String) : Notification {
        createNotificationChanel()
        val finishAppIntent= Intent("ACTION_FINISH")
        val startActivityIntent: Intent = Intent(context,MainActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        context.registerReceiver(FinishAppBroadcastReceiver(), IntentFilter("ACTION_FINISH") , 0)
        val startActivityPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, 0)
        val closeAppPendingIntent:PendingIntent=PendingIntent.getBroadcast(context,0,finishAppIntent,0)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

            .addAction(R.mipmap.ic_launcher_round, context.getString(R.string.notification_btn_text) , startActivityPendingIntent)
            .addAction(R.mipmap.ic_launcher_round,"Close App",closeAppPendingIntent)
            .setSmallIcon(R.drawable.ic_baseline_launch_24)
            .setContentText(context.getString(R.string.notification_msg)+" "+msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(startActivityPendingIntent)
            .setAutoCancel(true)
            .build()
    }


    fun errorNotification() {
        createNotificationChanel()

        val turnOnLocationSettingsPendingIntent = PendingIntent.getActivity(context,0,
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),0)

        val notification =  NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_launch_24)
            .addAction(R.mipmap.ic_launcher_round,"Turn On Location Settings", turnOnLocationSettingsPendingIntent)
            .setContentTitle("Error")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(turnOnLocationSettingsPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(111,notification)
   }

    fun createNotificationChanel() {
        val name = context.getString(R.string.chanel_name)
        val descriptionText = context.getString(R.string.chanel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}