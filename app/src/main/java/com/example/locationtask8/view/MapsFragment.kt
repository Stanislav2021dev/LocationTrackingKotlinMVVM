package com.example.locationtask8.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import android.content.Intent
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.maps.*
import java.util.*


class MapsFragment : Fragment(), OnMapReadyCallback,ServiceConnection {


    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private val LOCATION_PERMISSION_REQUEST_CODE = 222
    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private var mMap: GoogleMap?=null
    private var firstCreate: Boolean = true
    private lateinit var apiExeptionReceiver: BroadcastReceiver
    private var permissionErrorSnackBar: Snackbar?=null
    private var backgroundSnackBar: Snackbar?=null
    private var locationErrorSnackBar: Snackbar?=null
    private lateinit var mIntent: Intent
    private var mReceiverForegroundWork: LocationSettingsChangeBroadcastReceiverForegroundWork?= null
    private var flag_action_mapsFragment_to_logInFragment: Boolean = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var mIsBound = false
    private var mapFragment: SupportMapFragment?=null

    @Inject
    lateinit var trackViewModel: TrackViewModel

    @Inject
    lateinit var utils: Utils

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
    }

    override fun onMapReady(map: GoogleMap) {
        val kyiv = LatLng(50.43, 30.57)
        mMap = map
        map.addMarker(MarkerOptions().position(kyiv).title(getString(R.string.marker_home)))
        map.moveCamera(CameraUpdateFactory.newLatLng(kyiv))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("MService", "ON CREATE!!")

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
        mapFragment= (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment!!.getMapAsync(this)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            Log.v("TakeCoordinates", "onOptionsItemSelected")
            FirebaseAuth.getInstance().signOut()
            val manager: FragmentManager = requireActivity().supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            transaction.addToBackStack("mm")
            transaction.replace(R.id.activity_main_navHostFragment, LogInFragment())
            transaction.commit()


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
              showPermissionSnackBar()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v("MService","ON START!!")
        requestPermissionLauncher.launch(FINE_LOCATION)
    }

    override fun onResume() {
        super.onResume()
        Log.v("MService","ON RESUME!!")
        Log.v("TakeCoordinates", "On Resume")
        if (checkSelfPermission(requireContext(), BACKGROUND_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){
            backgroundSnackBar?.dismiss()
        }

        doUnbindService()
        hideLocationErrorSnackBar()
        showLocationErrorSnackBar()
    }

    override fun onPause() {
        super.onPause()
        doBindService()
    }

    override fun onStop() {
        super.onStop()
        Log.v("TakeCoordinates", "OnSTOP!!!")

        if (flag_action_mapsFragment_to_logInFragment==false ) {
            Log.v("MService","Service start")
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
        Log.v("MService","ON DESTROYYY!!")
        Log.v("TakeCoordinates", "OnDestroy")
        trackViewModel.getCoordinates.stopLocationUpdates()
        context?.stopService(mIntent)

    }


    override fun onDestroyView() {
        _binding=null
        mMap?.clear()
        mapFragment?.onDestroy()
        mapFragment=null
        super.onDestroyView()
    }

    private fun checkBackgroundPermission() {
        if (checkSelfPermission(requireContext(), BACKGROUND_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            showBackgroundPermissionSnackBar()
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
                showLocationErrorSnackBar()
            }
        }
    }

    fun showLocationErrorSnackBar(){
        if (utils.isAppOnForeground() && !utils.isGpsEnabled()) {
        if (locationErrorSnackBar == null) {
            val parentActivity : FragmentActivity? = this.activity
            if (parentActivity != null) {
                Log.v("TakeCoordinates", "ERROR SNACKBAR")
                val turnOnLocationIntent =
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                locationErrorSnackBar=createSnackBar(requireContext(),
                    getString(R.string.location_error_snackBar),getString(R.string.action_OK),
                    turnOnLocationIntent,parentActivity)
                locationErrorSnackBar!!.show()
            }
        }
            else
                locationErrorSnackBar!!.show()
        }
    }

    fun hideLocationErrorSnackBar() {
        if (utils.isGpsEnabled() && locationErrorSnackBar?.isShown == true) {
            locationErrorSnackBar?.dismiss()
        }
    }

    fun showBackgroundPermissionSnackBar(){
        if (backgroundSnackBar == null) {
            val parentActivity : FragmentActivity? = this.activity
            if (parentActivity != null) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + (context?.packageName)))
                backgroundSnackBar = createSnackBar(requireContext(),
                    getString(R.string.background_snackBar_1) +
                            getString(R.string.background_snackBar_2),
                    getString(R.string.action_allow), intent,parentActivity)
                backgroundSnackBar!!.show()
            }
       }
        else
            backgroundSnackBar!!.show()

    }

    fun showPermissionSnackBar(){
        Log.v("TakeCoordinates", "NEW SNACKBAR")
        if (permissionErrorSnackBar == null) {
             val parentActivity : FragmentActivity? = this.activity
            if (parentActivity != null) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + (context?.packageName)))
                permissionErrorSnackBar=createSnackBar(requireContext(),
                    getString(R.string.permission_snackBar), getString(R.string.action_allow_perm), intent,parentActivity)
                permissionErrorSnackBar!!.show()
            }
        }
        else
            permissionErrorSnackBar!!.show()
    }


    override fun onServiceConnected(componentname: ComponentName?, binder: IBinder?) {
        Log.v("MService","onServiceConnected")
        mIsBound = true

    }

    override fun onServiceDisconnected(componentname: ComponentName?) {
        Log.v("MService","onServiceDisconnected")
        mIsBound = false
    }
    fun doBindService() {
        Log.v("MService","DO BIND SERVICE!!")
       context?.bindService(Intent(requireActivity(),BackGroundService::class.java),
           this,Context.BIND_AUTO_CREATE)
    }
    fun doUnbindService() {
        if (mIsBound) {
            Log.v("MService","DO UNBIND SERVICE!!")
            context?.unbindService(this)
        }
    }

    fun createSnackBar(context: Context, maitText:String, action:String, intent: Intent,fragmentActivity: FragmentActivity) :Snackbar{
        val snackbar=Snackbar.make(fragmentActivity.findViewById(android.R.id.content),
            maitText, Snackbar.LENGTH_INDEFINITE).setAction(action) {
            requireActivity().finish()
            context.startActivity(intent)
        }
        return snackbar
    }

}