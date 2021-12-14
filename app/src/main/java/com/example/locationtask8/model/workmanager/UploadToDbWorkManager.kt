package com.example.locationtask8.model.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.LoadData
import com.example.locationtask8.model.ResultClass
import com.example.locationtask8.model.Utils
import javax.inject.Inject

class UploadToDbWorkManager(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams) {
    @Inject
    lateinit var loadData:LoadData
    @Inject
    lateinit var utils:Utils

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

    override fun doWork(): Result {
        Log.v("TakeCoordinates","Upload to Db")
        val dateTime = inputData.getString("DateTime")
        val coordinates = inputData.getString("Coordinates")
        loadData.uploadToRoomDb(ResultClass(dateTime!!, utils.toLatLng(coordinates!!)))
        return Result.success()
    }
}