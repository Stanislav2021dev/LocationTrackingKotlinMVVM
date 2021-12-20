package com.example.locationtask8.view

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.locationtask8.R

import com.example.locationtask8.databinding.FragmentMapsBinding
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.BackGroundService
import com.example.locationtask8.model.Utils
import com.example.locationtask8.model.broadcast_receiver.LocationSettingsChangeBroadcastReceiverForegroundWork
import com.example.locationtask8.viewmodel.TrackViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private val LOCATION_PERMISSION_REQUEST_CODE = 222
    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private var mMap: GoogleMap?=null
    private var firstCreate: Boolean = true
    private lateinit var apiExeptionReceiver: BroadcastReceiver
    private var permissionErrorSnackBar: Snackbar? = null
    private var backgroundSnackBar: Snackbar? = null
    private var locationErrorSnackBar: Snackbar? = null
    private lateinit var mIntent: Intent
    private var mReceiverForegroundWork: LocationSettingsChangeBroadcastReceiverForegroundWork?= null
    private var flag_action_mapsFragment_to_logInFragment: Boolean = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    @Inject
    lateinit var trackViewModel: TrackViewModel

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var snackbar: SnackBarView

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)

    }

    override fun onMapReady(map: GoogleMap) {
        val sydney = LatLng(-34.0, 151.0)
        mMap = map
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("TakeCoordinates", "OnCreate")

        trackViewModel =
            ViewModelProvider(this).get(TrackViewModel::class.java)
        trackViewModel.getCurrentLocationLiveData()?.observe(this, {
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(it))
        })
        mIntent = Intent(requireActivity(), BackGroundService::class.java)
        setHasOptionsMenu (true)
        requestPermissions()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v("TakeCoordinates", "OnCreateView")
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v("TakeCoordinates", "OnViewCreated")
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            Log.v("TakeCoordinates", "onOptionsItemSelected")
            FirebaseAuth.getInstance().signOut()
            Navigation.findNavController(requireView()).navigate(R.id.action_mapsFragment_to_logInFragment)
            flag_action_mapsFragment_to_logInFragment=true

            if (backgroundSnackBar?.isShown == true) {
                backgroundSnackBar?.dismiss()
            }
            if (locationErrorSnackBar?.isShown==true){
                locationErrorSnackBar?.dismiss()
            }
            if (permissionErrorSnackBar?.isShown==true){
                permissionErrorSnackBar?.dismiss()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun requestPermissions(){
        requestPermissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGrandted:Boolean ->

            if (isGrandted){
                Log.v("TakeCoordinates", "PERMISSION_GRANTED")
                permissionErrorSnackBar?.dismiss()
                if (checkSelfPermission(requireContext(), FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                    mMap?.isMyLocationEnabled = true
                }
                checkBackgroundPermission()
                registerBroadcastReceivers()
                trackViewModel.start()
            }
            else {
                Log.v("TakeCoordinates", "NEW SNACKBAR")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + (context?.packageName)))
                permissionErrorSnackBar=snackbar.createSnackBar(requireContext(), "Location permission needed", "Allow permission", intent)
            }
        }
    }




    override fun onStart() {
        super.onStart()
        Log.v("TakeCoordinates", "OnStart, Stop Service")
        context?.stopService(mIntent)
        requestPermissionLauncher.launch(FINE_LOCATION)
    }

    override fun onResume() {
        super.onResume()
        Log.v("TakeCoordinates", "On Resume")
        if (checkSelfPermission(requireContext(), BACKGROUND_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){
            backgroundSnackBar?.dismiss()
        }
        hideLocationErrorSnackBar()
        createLocationErrorSnackBar()
    }



    override fun onStop() {
        super.onStop()
        Log.v("TakeCoordinates", "OnSTOP!!!")
        if (flag_action_mapsFragment_to_logInFragment==false) {
            context?.startService(mIntent)
        }
        if (!firstCreate){
            context?.unregisterReceiver(mReceiverForegroundWork)
            context?.unregisterReceiver(apiExeptionReceiver)
        }
        flag_action_mapsFragment_to_logInFragment=false
        trackViewModel.getCoordinates.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("TakeCoordinates", "OnDestroy")
        trackViewModel.getCoordinates.stopLocationUpdates()
        context?.stopService(mIntent)
    }


    private fun checkBackgroundPermission() {
        if (checkSelfPermission(requireContext(), BACKGROUND_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + (context?.packageName)))
            backgroundSnackBar = snackbar.createSnackBar(requireContext(),
                "To enable background work, " +
                        "please turn on 'Allow all the time' for this application",
                "Allow", intent)
        }
    }

    fun registerBroadcastReceivers() {
            Log.v("TakeCoordinates", "registerBroadcast")
            val filter = IntentFilter(PROVIDERS_CHANGED_ACTION)
            mReceiverForegroundWork = LocationSettingsChangeBroadcastReceiverForegroundWork()
            context?.registerReceiver(mReceiverForegroundWork,filter)
            createApiExeptionReceiver()
            context?.registerReceiver(apiExeptionReceiver, IntentFilter("SHOW_SNACKBAR"))
            firstCreate = false
    }

    fun createApiExeptionReceiver() {
        apiExeptionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v("TakeCoordinates", "ApiExeprion Receiver")
                hideLocationErrorSnackBar()
                createLocationErrorSnackBar()
            }
        }
    }

    fun createLocationErrorSnackBar(){
        if (utils.isAppOnForeground() && !utils.isGpsEnabled()) {
            Log.v("TakeCoordinates", "ERROR SNACKBAR")
            val turnOnLocationIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            locationErrorSnackBar=snackbar.createSnackBar(requireContext(),
                "Turn On Location Settings","OK",turnOnLocationIntent)
        }
    }

    fun hideLocationErrorSnackBar() {
        if (utils.isGpsEnabled() && locationErrorSnackBar?.isShown == true) {
            locationErrorSnackBar?.dismiss()
        }
    }
}