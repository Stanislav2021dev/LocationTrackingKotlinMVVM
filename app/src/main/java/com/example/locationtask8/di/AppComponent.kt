package com.example.locationtask8.di

import com.example.locationtask8.database.CoordinatesDataBase
import com.example.locationtask8.model.*
import com.example.locationtask8.model.broadcast_receiver.FinishAppBroadcastReceiver
import com.example.locationtask8.model.broadcast_receiver.LocationSettingsChangeBroadcastReceiverForegroundWork
import com.example.locationtask8.model.broadcast_receiver.LocationSettingsChangerReceiverBackgroundWork
import com.example.locationtask8.model.workmanager.*
import com.example.locationtask8.view.LogInFragment
import com.example.locationtask8.view.MapsFragment
import com.example.locationtask8.view.NotificationView
import com.example.locationtask8.viewmodel.TrackViewModel
import dagger.Component
import javax.inject.Singleton

@Component(modules = [MapsModule::class])
@Singleton
interface AppComponent {

    fun inject(logInFragment: LogInFragment)

    fun inject(mapsFragment: MapsFragment)

    fun inject(trackViewModel: TrackViewModel)

    fun inject(getCoordinates: GetCoordinates)

    fun inject(initWorkManager: InitWorkManager)

    fun inject(cancelUploadWorkManager: CancelUploadWorkManager)

    fun inject(coordinatesDataBase: CoordinatesDataBase)

    fun inject(utils: Utils)

    fun inject(loadData: LoadData)

    fun inject(notificationView: NotificationView)

    fun inject(uploadToFbWorkManager: UploadToFbWorkManager)

    fun inject(uploadToDbWorkManager: UploadToDbWorkManager)

    fun inject(uploadFromDbToFbWorkManager: UploadFromDbToFbWorkManager)

    fun inject(backGroundService: BackGroundService)

    fun inject(finishAppBroadcastReceiver: FinishAppBroadcastReceiver)

    fun inject(locationSettingsChangeBroadcastReceiverForegroundWork: LocationSettingsChangeBroadcastReceiverForegroundWork)

    fun inject(locationSettingsChangerReceiverBackgroundWork: LocationSettingsChangerReceiverBackgroundWork)

    fun inject(logInModel: LogInModel)

    fun getUtils(): Utils?

}