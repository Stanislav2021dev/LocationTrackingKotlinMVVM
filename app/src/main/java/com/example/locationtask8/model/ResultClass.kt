package com.example.locationtask8.model

import com.google.android.gms.maps.model.LatLng

class ResultClass(_currentTime:String,_currentLocation:LatLng) {
    private lateinit var currentTime:String
    private lateinit var currentLocation: LatLng

    init {
        currentTime = _currentTime
        currentLocation = _currentLocation
    }

    fun getCurrentTime()=currentTime
    fun getCurrentLocation()=currentLocation
}