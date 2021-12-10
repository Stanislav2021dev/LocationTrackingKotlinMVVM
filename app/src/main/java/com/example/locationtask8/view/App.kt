package com.example.locationtask8.view

import android.app.Application
import android.content.Context

class App: Application() {
    private var instance: App? = null

    fun getInstance(): App? {
        return instance
    }

    fun getContext(): Context? {
        return instance!!.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance=this
    }
}