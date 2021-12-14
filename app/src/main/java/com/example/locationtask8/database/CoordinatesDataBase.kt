package com.example.locationtask8.database

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import javax.inject.Inject


@Database(entities = [CoordinatesModel::class], version = 1)

abstract class CoordinatesDataBase : RoomDatabase()   {
    abstract fun getCoordinatesDAO(): CoordinatesDAO

    companion object{
        @Volatile
        private var INSTANCE: CoordinatesDataBase? = null
        fun getInstance(context:Context): CoordinatesDataBase? {
            if (INSTANCE == null) {
                synchronized(CoordinatesDataBase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context,
                            CoordinatesDataBase::class.java, "CoordinatesData").build()
                    }
                }
            }
            return INSTANCE
        }

    }


}