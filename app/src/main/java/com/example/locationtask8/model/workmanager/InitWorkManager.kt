package com.example.locationtask8.model.workmanager

import android.app.Application
import android.content.Context
import androidx.work.*
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.ResultClass
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InitWorkManager @Inject constructor() {
    @Inject lateinit var context:Context
    val UPLOADTAG = "UPLOAD_WORK"
    val WORKTAG="CURRENT_WORK"
    init{
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

    fun initWork(result:ResultClass){

        val inputData = Data.Builder()
            .putString("DateTime", result.getCurrentTime())
            .putString("Coordinates", result.getCurrentLocation().toString())
            .build()

        val uploadToDbConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val uploadToFbConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadToFbRequest  = OneTimeWorkRequest.Builder(
            UploadToFbWorkManager::class.java)
            .setInputData(inputData)
            .addTag(UPLOADTAG)
            .setConstraints(uploadToFbConstraints).build()

        val uploadToDbRequest = OneTimeWorkRequest.Builder(
            UploadToDbWorkManager::class.java)
            .setInputData(inputData)
            .setConstraints(uploadToDbConstraints).build()

        val uploadFromDbToFbRequest = OneTimeWorkRequest.Builder(
            UploadFromDbToFbWorkManager::class.java
        )
            .setConstraints(uploadToFbConstraints)
            .build()

        val canceluploadToFb = OneTimeWorkRequest.Builder(
            CancelUploadWorkManager::class.java
        )
            .setInitialDelay(1000, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .beginWith(uploadToDbRequest)
            .then(uploadToFbRequest)
            .enqueue()

        WorkManager.getInstance(context)
            .beginWith(canceluploadToFb)
            .enqueue()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORKTAG, ExistingWorkPolicy.REPLACE, uploadFromDbToFbRequest)

    }

}