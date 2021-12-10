package com.example.locationtask8.model.workmanager

import android.app.Application
import androidx.work.*
import com.example.locationtask8.model.ResultClass
import java.util.concurrent.TimeUnit

class InitWorkManager(application: Application) {
    private val app:Application
    init {
        app=application
    }
    fun initWork(result:ResultClass){

        val inputData = Data.Builder()
//            .putString("DateTime", result.getCurrentTime())
           // .putString("Coordinates", result.getCurrentLocation().toString())
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
            .addTag("fb")
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

        WorkManager.getInstance(app.applicationContext)
            .beginWith(uploadToDbRequest)
            .then(uploadToFbRequest)
            .enqueue()

        WorkManager.getInstance(app.applicationContext)
            .beginWith(canceluploadToFb)
            .enqueue()

        WorkManager.getInstance(app.applicationContext)
            .enqueueUniqueWork("work", ExistingWorkPolicy.REPLACE, uploadFromDbToFbRequest)

    }

}