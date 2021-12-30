package com.example.locationtask8.di

import android.app.Application
import android.content.Context
import com.example.locationtask8.database.CoordinatesDataBase
import com.example.locationtask8.model.GetCoordinates
import com.example.locationtask8.model.workmanager.InitWorkManager
import com.example.locationtask8.view.App
import com.example.locationtask8.viewmodel.TrackViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MapsModule {

    @Provides
    @Singleton
    fun provideContext(): Context = App.getContext()!!


    @Provides
    @Singleton
    fun provideApp(): App? = App.getInstance()

    @Provides
    @Singleton
    fun provideApplication(): Application = Application()

    @Provides
    fun provideTrackViewModel(): TrackViewModel = TrackViewModel(provideApplication())

    @Provides
    fun provideGetCoordinates(): GetCoordinates = GetCoordinates()

    @Provides
    fun provideInitWorkManager(): InitWorkManager = InitWorkManager()


    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()



}