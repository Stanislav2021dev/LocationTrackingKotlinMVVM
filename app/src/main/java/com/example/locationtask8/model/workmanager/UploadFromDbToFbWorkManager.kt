package com.example.locationtask8.model.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadFromDbToFbWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {

      return Result.success()
    }
}