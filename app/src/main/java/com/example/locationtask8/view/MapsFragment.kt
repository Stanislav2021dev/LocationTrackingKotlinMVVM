package com.example.locationtask8.view

import  android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.locationtask8.R
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.viewmodel.LogInViewModel
import com.example.locationtask8.viewmodel.TrackViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import javax.inject.Inject

class MapsFragment : Fragment(),OnMapReadyCallback {
    private val LOCATION_PERMISSION_REQUEST_CODE = 222
    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private lateinit var permissions: Array<String>
    private lateinit var mMap:GoogleMap

    @Inject lateinit var trackViewModel : TrackViewModel


    override fun onMapReady(map: GoogleMap) {
        val sydney = LatLng(-34.0, 151.0)
        mMap=map
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)

        trackViewModel =
            ViewModelProvider(this).get(TrackViewModel::class.java)

        getLocationPermissions()
    }

     fun getLocationPermissions(){
        permissions = arrayOf(FINE_LOCATION)
        if  (requireContext().checkSelfPermission(FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            trackViewModel.start()

        }
        else {
            ActivityCompat.requestPermissions(requireActivity(),permissions,LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults.size!=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 mMap.setMyLocationEnabled(true)
            }
        }

    }


}