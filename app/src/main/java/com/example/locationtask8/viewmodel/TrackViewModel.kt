package com.example.locationtask8.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.GetCoordinates
import com.example.locationtask8.model.workmanager.InitWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackViewModel @Inject constructor(application: Application) : AndroidViewModel(application)  {


    @Inject lateinit var getCoordinates:GetCoordinates
    @Inject lateinit var workManager:InitWorkManager

    init {
    //    getCoordinates= GetCoordinates(application)
     //   workManager= InitWorkManager(application)
      //  app=application
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
        resultWork2()
    }

     fun start(){
             getCoordinates.startLocationGathering(10*1000)
    }

    fun resultWork2() = viewModelScope.launch(IO){
                getCoordinates.getChan().consumeEach {
                    Log.v("TakeCoordinates ", "Consumer " +it.getCurrentTime())
                workManager.initWork(it)
            }
    }

   // fun resultWork() = coroutineScope{
    //   val observer =  launch {
     //      getCoordinates.getChan().consumeEach {
     //         workManager.initWork(it)
      //     }
     //  }
   //}
}