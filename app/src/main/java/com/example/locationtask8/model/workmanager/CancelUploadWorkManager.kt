package com.example.locationtask8.model.workmanager

import android.content.Context
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import javax.inject.Inject

class CancelUploadWorkManager(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams) {
    val TAG = "UPLOAD_WORK"
    @Inject
    lateinit var context:Context

    init{
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }
    override fun doWork(): Result {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
       return Result.success()
    }
}