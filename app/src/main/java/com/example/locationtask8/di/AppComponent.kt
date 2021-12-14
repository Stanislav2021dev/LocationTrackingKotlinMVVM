package com.example.locationtask8.di

import com.example.locationtask8.database.CoordinatesDataBase
import com.example.locationtask8.model.GetCoordinates
import com.example.locationtask8.model.LoadData
import com.example.locationtask8.model.Utils
import com.example.locationtask8.model.workmanager.*
import com.example.locationtask8.view.MapsFragment
import com.example.locationtask8.viewmodel.TrackViewModel
import dagger.Component
import javax.inject.Singleton

@Component(modules = [MapsModule::class])
@Singleton
public interface AppComponent {

    fun inject(mapsFragment: MapsFragment)

    fun inject(trackViewModel: TrackViewModel)

    fun inject(getCoordinates: GetCoordinates)

    fun inject(initWorkManager: InitWorkManager)

    fun inject(cancelUploadWorkManager: CancelUploadWorkManager)

    fun inject(coordinatesDataBase: CoordinatesDataBase)

    fun inject(utils: Utils)

    fun inject(loadData: LoadData)

    fun inject(uploadToFbWorkManager: UploadToFbWorkManager)

    fun inject(uploadToDbWorkManager: UploadToDbWorkManager)

    fun inject(uploadFromDbToFbWorkManager: UploadFromDbToFbWorkManager)

    fun getUtils(): Utils?

   // fun getDatabase():CoordinatesDataBase?

    //fun getLoadData(): LoadData?
}