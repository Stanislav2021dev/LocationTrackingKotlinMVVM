package com.example.locationtask8.model

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.broadcast_receiver.LocationSettingsChangerReceiverBackgroundWork
import com.example.locationtask8.model.workmanager.InitWorkManager
import com.example.locationtask8.view.NotificationView
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.GlobalScope

class BackGroundService :Service(),LocationListener {


    private val NOTIFICATION_ID = 123
    private lateinit var mReceiver:BroadcastReceiver

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var getCoordinates: GetCoordinates

    @Inject
    lateinit var initWorkManager: InitWorkManager

    @Inject
    lateinit var notifications: NotificationView

    init {
            val appComponent: AppComponent = DaggerAppComponent.create()
            appComponent.inject(this)
    }


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.v("TakeCoordinates","OnCreate Service")
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        mReceiver=LocationSettingsChangerReceiverBackgroundWork()
        registerReceiver(mReceiver,filter)
        resultWork()
    }

    override fun onDestroy() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        Log.v("TakeCoordinates","OnDestroyService")
        getCoordinates.stopLocationUpdates()
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("TakeCoordinates", "OnStart comand")
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==  PackageManager.PERMISSION_GRANTED) {
            getCoordinates.startLocationGathering(20 * 1000)

        }
        return START_STICKY
    }

    fun resultWork(){
        GlobalScope.launch {
             getCoordinates.getChan().consumeEach {
                initWorkManager.initWork(it)
                 startForeground(NOTIFICATION_ID, notifications
                     .locationInfoNotification(it.getCurrentLocation().toString()))
            }
        }
    }

    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")
    }


    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
        Log.v("TakeCoordinates","Listner LocationChanged")
    }

}