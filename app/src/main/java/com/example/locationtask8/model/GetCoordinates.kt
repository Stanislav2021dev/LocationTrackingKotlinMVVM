package com.example.locationtask8.model

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
//import android.location.LocationRequest
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.view.App
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetCoordinates  @Inject constructor() :LocationCallback() {

    @Inject lateinit var context:Context

    private lateinit var locationRequest:LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private var currentLocation: Location? = null
    private var currentPoint: ResultClass? = null
    private var fusedLocationClient: FusedLocationProviderClient?=null
    private val chanel = Channel<ResultClass>(CONFLATED)

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

     fun startLocationGathering(timeInterval:Long){
        Log.v("TakeCoordinates","Start location gathering")
        buildLocationRequest(timeInterval)
        startLocationUpdates()
    }

    fun buildLocationRequest(timeInterval: Long) {
        Log.v("Order", "Build Location Request")
        locationRequest=LocationRequest.create().apply{
            interval = timeInterval
            fastestInterval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 6000
        }
      locationSettingsRequest = LocationSettingsRequest.Builder()
          .addAllLocationRequests(setOf(locationRequest))
          .build()
    }

    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        currentLocation=locationResult.lastLocation
        currentPoint= updateLocation()
        CoroutineScope(Dispatchers.IO).launch {
            addtoChanel()
        }
    }

    suspend fun addtoChanel(){
        if (currentPoint!=null){
            Log.v("TakeCoordinates", "Current Point " + currentPoint!!.getCurrentTime())
            chanel.send(currentPoint!!)
        }
    }

    fun updateLocation():ResultClass{
        Log.v("TakeCoordinates", "Taking coordinates. Current location --> " +
                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
        return ResultClass(SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(System.currentTimeMillis()),
                           LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
    }

    fun startLocationUpdates(){
        Log.v("TakeCoordinates","Start location updates")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        LocationServices.getSettingsClient(context)
            .checkLocationSettings(locationSettingsRequest).addOnCompleteListener {
               // it.getResult(ApiException)
                try {
                    if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED) {
                        return@addOnCompleteListener
                    }
                    else {
                        fusedLocationClient?.requestLocationUpdates(locationRequest, this, Looper.myLooper())
                    }
                } catch (except:ApiException) {
                    when (except.getStatusCode()) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.v("TakeCoordinates", "RESOLUTION_REQUIRED")
                            try {
                                val resolvableApiException = except as ResolvableApiException
                                Log.v("TakeCoordinates", "RESOLUTION_REQUIRED")
                                val intent = Intent("SHOW_SNACKBAR")
                                intent.putExtra("ApiException", resolvableApiException.resolution)
                                val pendingIntent =
                                    PendingIntent.getBroadcast(App.getContext(), 0, intent, 0)
                                pendingIntent.send(App.getContext(), 0, intent)
                            } catch (e: ClassCastException) {

                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                            Log.v("TakeCoordinates",  "SETTINGS_CHANGE_UNAVAILABLE")
                    }

                }
            }
    }
    fun getChan()=chanel
    fun stopLocationUpdates(){
        Log.v("MService","STOPLOCATIONUPDATES")
        fusedLocationClient?.removeLocationUpdates(this)
    }
}