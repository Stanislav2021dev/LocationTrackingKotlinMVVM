package com.example.locationtask8.model

import android.Manifest
import android.app.Application
import android.app.TaskStackBuilder.create
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
//import android.location.LocationRequest
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.example.locationtask8.view.App
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import java.net.URI.create
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.launch
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class GetCoordinates(_app: Application) {
    private val app: Application
    private lateinit var locationRequest:LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var currentPoint: ResultClass? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val chanel = Channel<ResultClass>(CONFLATED)

    init {
        app=_app
    }

     fun startLocationGathering(timeInterval:Long){
        Log.v("TakeCoordinates","Start location gathering")
            buildLocationRequest(timeInterval)
        buildLocationSettingsRequest()
        buildLocationCallback()
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
    }

    fun buildLocationSettingsRequest(){
        val builder = LocationSettingsRequest.Builder()
        builder.addAllLocationRequests(setOf(locationRequest))
        locationSettingsRequest = builder.build()
    }

     fun buildLocationCallback(){
        locationCallback=object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation=locationResult.lastLocation
                currentPoint= updateLocation()
                CoroutineScope(Dispatchers.IO).launch {
                    addtoChanel()
                }
            }
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(app.applicationContext)
        val result = LocationServices.getSettingsClient(app.applicationContext)
            .checkLocationSettings(locationSettingsRequest).addOnCompleteListener {
               // it.getResult(ApiException)

                try {
                    if (ActivityCompat.checkSelfPermission(app.applicationContext,Manifest.permission.ACCESS_FINE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED) {
                        return@addOnCompleteListener
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,
                        Looper.myLooper())

                } catch (except:ApiException) {
                }


            }

    }
    fun getChan()=chanel
}

