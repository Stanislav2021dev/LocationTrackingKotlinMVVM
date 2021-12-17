package com.example.locationtask8.model.broadcast_receiver

import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import javax.inject.Inject

class FinishAppBroadcastReceiver :BroadcastReceiver() {
    @Inject
    lateinit var context:Context

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.unregisterReceiver(this)
        val am = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val taskInfo: List<AppTask> = am.appTasks
        if (taskInfo.size!=0){
            for (task in taskInfo) {
                task.finishAndRemoveTask()
            }
        }
    }
}