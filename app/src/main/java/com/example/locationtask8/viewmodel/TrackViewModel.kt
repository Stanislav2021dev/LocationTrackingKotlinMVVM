package com.example.locationtask8.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.GetCoordinates
import com.example.locationtask8.model.workmanager.InitWorkManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class TrackViewModel @Inject constructor(application: Application) : AndroidViewModel(application)  {

    @Inject lateinit var getCoordinates:GetCoordinates
    @Inject lateinit var workManager:InitWorkManager
    private var currentLocationLiveData: MutableLiveData<LatLng>?=null

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
        resultWork()
    }

     fun start(){
             getCoordinates.startLocationGathering(10*1000)
    }

    fun resultWork() = viewModelScope.launch(IO){
                getCoordinates.getChan().consumeEach {
                workManager.initWork(it)
                currentLocationLiveData?.postValue(it.getCurrentLocation())
            }
    }
    fun getCurrentLocationLiveData()=currentLocationLiveData
}