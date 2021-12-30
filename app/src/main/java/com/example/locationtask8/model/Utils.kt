package com.example.locationtask8.model

import android.app.ActivityManager
import android.app.ActivityManager.RunningTaskInfo
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import androidx.core.content.ContextCompat.getSystemService




class Utils @Inject constructor() {

    @Inject lateinit var context:Context
    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }


    fun getCurrentTime():String{
        return SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(System.currentTimeMillis())
    }

    fun isOnline(): Boolean {
        val connectivityManager: ConnectivityManager = context.getSystemService(
            ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        return currentNetwork != null && caps!!.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isGpsEnabled(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isAppOnForeground():Boolean{
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if(!tasks.isEmpty()){
            val topActivity=tasks.get(0).topActivity
            if (!topActivity!!.packageName.equals(context.packageName)){
                return false
            }
        }
        return true
    }

    fun toLatLng(coordinates: String): LatLng {
        val latlong = coordinates.split(",").toTypedArray()
        val latitude = latlong[0].replace("lat/lng: (", "").toDouble()
        val longitude = latlong[1].replace(")", "").toDouble()
        return LatLng(latitude, longitude)
    }

     fun isMyServiceRunning(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager
            .getRunningServices(Int.MAX_VALUE)) {
            if (BackGroundService::class.java.getName() ==
                service.service.className) {
                return true
            }
        }
        return false
    }


}