package com.example.locationtask8.model.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadToFbWorkManager(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        Log.v("TakeCoordinates","Upload to Fb")
       return Result.success()
    }
}