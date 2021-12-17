package com.example.locationtask8.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.example.locationtask8.R

import com.example.locationtask8.databinding.FragmentMapsBinding
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.BackGroundService
import com.example.locationtask8.model.Utils
import com.example.locationtask8.model.broadcast_receiver.LocationSettingsChangeBraodcastReceiver
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
    private lateinit var permissions: Array<String>
    private lateinit var mMap: GoogleMap
    private var firstCreate: Boolean = true
    private lateinit var apiExeptionReceiver: BroadcastReceiver
    private var errorSnackBar: Snackbar? = null
    private var backgroundSnackBar: Snackbar? = null
    private lateinit var mIntent: Intent
    private lateinit var mReceiver: LocationSettingsChangeBraodcastReceiver
    private var flag_action_mapsFragment_to_logInFragment: Boolean = false


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
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        })
        mIntent = Intent(requireActivity(), BackGroundService::class.java)
        setHasOptionsMenu (true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v("TakeCoordinates", "OnCreateView")
      //  inflater.inflate(R.layout.fragment_maps,container,false)
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Log.v("TakeCoordinates", "OnStart, Stop Service")
        context?.stopService(mIntent)
        getLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        Log.v("TakeCoordinates", "On Resume")
        hideSnackBar()
        if (ActivityCompat.checkSelfPermission(requireContext(), BACKGROUND_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){
            backgroundSnackBar?.dismiss()
        }
    }



    override fun onStop() {
        super.onStop()
        Log.v("TakeCoordinates", "OnSTOP!!!")
        if (flag_action_mapsFragment_to_logInFragment==false) {
            context?.startService(mIntent)
        }
        flag_action_mapsFragment_to_logInFragment=false
        trackViewModel.getCoordinates.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("TakeCoordinates", "OnDestroy")
        trackViewModel.getCoordinates.stopLocationUpdates()
        context?.unregisterReceiver(mReceiver)
        context?.unregisterReceiver(apiExeptionReceiver)
        context?.stopService(mIntent)
    }




    fun getLocationPermissions() {
        if (Build.VERSION.SDK_INT == 29) {
              arrayOf(FINE_LOCATION, BACKGROUND_LOCATION)
        }
        else if (Build.VERSION.SDK_INT >29) {
            permissions =  arrayOf(FINE_LOCATION)
        }
        checkBackgroundPermission()
        if (context?.checkSelfPermission(FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            registerBroadcastReceivers()
            trackViewModel.start()
        } else {
            Log.v("TakeCoordinates", "REQUEST_PERMISSION")
            requestPermissions(requireActivity(),permissions,LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    private fun checkBackgroundPermission() {
        if (checkSelfPermission(requireContext(), BACKGROUND_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context?.packageName))
            backgroundSnackBar = snackbar.createSnackBar(requireContext(),
                "To enable background work, " +
                        "please turn on 'Allow all the time' for this application",
                "Allow", intent)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("TakeCoordinates", "PERMISSION_GRANTED")
                mMap.isMyLocationEnabled = true
                checkBackgroundPermission()
                registerBroadcastReceivers()
                trackViewModel.start()
            } else {
                Log.v("TakeCoordinates", "NEW SNACKBAR")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + (context?.packageName)))

                snackbar.createSnackBar(requireContext(), "Location permission needed", "Allow permission",
                    intent)
            }
        }
    }

    fun registerBroadcastReceivers() {
        if (firstCreate) {
            Log.v("TakeCoordinates", "registerBroadcast")
            val filter = IntentFilter(PROVIDERS_CHANGED_ACTION)
            mReceiver = LocationSettingsChangeBraodcastReceiver()
            context?.registerReceiver(mReceiver,filter)
            createApiExeptionReceiver()
            context?.registerReceiver(apiExeptionReceiver, IntentFilter("SHOW_SNACKBAR"))
            firstCreate = false
        }
    }

    fun createApiExeptionReceiver() {
        apiExeptionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v("TakeCoordinates", "ApiExeprion Receiver")
                hideSnackBar()
                if (utils.isAppOnForeground() && !utils.isGpsEnabled()) {
                    Log.v("TakeCoordinates", "ERROR SNACKBAR")
                    val turnOnLocationIntent =
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    errorSnackBar = snackbar.createSnackBar(
                        context, "Turn On Location", "Ok", turnOnLocationIntent)
                }
            }
        }
    }

    fun hideSnackBar() {
        if (utils.isGpsEnabled() && errorSnackBar?.isShown == true) {
            errorSnackBar?.dismiss()
        }
    }

}