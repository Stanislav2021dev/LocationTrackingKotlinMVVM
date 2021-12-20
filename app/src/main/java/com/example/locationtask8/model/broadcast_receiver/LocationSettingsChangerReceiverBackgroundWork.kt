package com.example.locationtask8.model.broadcast_receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.BackGroundService
import com.example.locationtask8.model.Utils
import com.example.locationtask8.view.NotificationView
import javax.inject.Inject

class LocationSettingsChangerReceiverBackgroundWork:BroadcastReceiver() {
    @Inject
    lateinit var notificationView: NotificationView
    @Inject
    lateinit var utils: Utils

    private lateinit var serviceIntent: Intent

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (LocationManager.GPS_PROVIDER == intent.getStringExtra(LocationManager.EXTRA_PROVIDER_NAME)){
            if (!utils.isGpsEnabled()) {
                Log.v("TakeCoordinates", "NotificationError")
                notificationView.errorNotification()
            }
            else{
                serviceIntent = Intent(context, BackGroundService::class.java)
                val notificationManager =context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
            }
        }
    }
}