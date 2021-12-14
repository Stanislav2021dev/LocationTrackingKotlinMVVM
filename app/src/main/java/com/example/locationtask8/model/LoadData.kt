package com.example.locationtask8.model

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.locationtask8.database.CoordinatesDataBase

import com.example.locationtask8.database.CoordinatesModel
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.HashMap
import javax.inject.Inject

class LoadData @Inject constructor() {
    private val coordinates = HashMap<String, Any>()
    private var uploadSuccess = false
    private lateinit var coordinatesDataBase:CoordinatesDataBase
    @Inject
    lateinit var context:Context

    @Inject
    lateinit var utils: Utils
  //  @Inject

    @Inject
    lateinit var db:FirebaseFirestore
    @Inject
    lateinit var firebaseAuth:FirebaseAuth

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
        coordinatesDataBase = CoordinatesDataBase.getInstance(context)!!
    }

    fun uploadToRoomDb(resultClass: ResultClass){
        if (!utils.isOnline()){
            coordinatesDataBase.getCoordinatesDAO()
                              ?.addCoordinates(CoordinatesModel(0,resultClass.getCurrentTime(),
                    resultClass.getCurrentLocation().toString()))
            Log.v("TakeCoordinates", "Upload coordinates to local DataBase")
            }
    }


    fun uploadToFirebase(resultClass: ResultClass) :Boolean {
        coordinates.put("time", resultClass.getCurrentTime())
        coordinates.put("location", resultClass.getCurrentLocation())
        db.collection(firebaseAuth.currentUser!!.uid)
            .document(resultClass.getCurrentTime())
            .set(coordinates, SetOptions.merge())
            .addOnSuccessListener {
                Log.v("TakeCoordinates", "Upload coordinates to firebase")
                uploadSuccess = true
            }
            .addOnFailureListener{
                Log.v("TakeCoordinates", "Error ->" +it)
                uploadSuccess = false
            }
        return uploadSuccess

    }

    fun uploadFromDbToFb(){

        val coordinatesModelList: List<CoordinatesModel>
        coordinatesModelList = coordinatesDataBase.getCoordinatesDAO()
            ?.getAllCoordinates() as List<CoordinatesModel>

        if (coordinatesModelList.size != 0) {
            Log.v("TakeCoordinates", "Upload from DB to Fb")
            for (coord in coordinatesModelList) {
                Log.v("DATABASE","Time " + coord.dateTime + " coordinates " + coord.coordinates
                         + "Size " + (coordinatesDataBase.getCoordinatesDAO()?.getAllCoordinates()
                        as List<CoordinatesModel>).size
                )

                uploadToFirebase(ResultClass(coord.dateTime,utils.toLatLng(coord.coordinates)))
                coordinatesDataBase.getCoordinatesDAO()?.delete(coord.getIdDb())
            }
        }
    }

}