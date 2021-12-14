package com.example.locationtask8.view

import android.app.Application
import android.content.Context



class App : Application() {

    override fun onCreate() {
        super.onCreate()

        instance=this
    }

    companion object{
       private var instance:App?=null

        fun getInstance(): App? = instance

        fun getContext(): Context? = instance?.applicationContext
    }




}


