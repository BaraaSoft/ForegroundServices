package com.baraa.software.eventhorizon.trackingapp

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class MainActivity : AppCompatActivity() {

    private val UPDATE_INTERVAL = (10 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    val REQUEST_CHECK_SETTINGS:Int = 11707

    val PERMISSIONS_REQUEST_LOCATION:Int = 10177
    var isLocationPermissionGranted:Boolean = false
    lateinit var mLocationRequest: LocationRequest


//    var locationBroadcastReceiver: BroadcastReceiver = object :BroadcastReceiver(){
//        override fun onReceive(p0: Context?, intent: Intent?) {
//            var lat = intent?.getDoubleExtra("lat",0.0)
//            var lng = intent?.getDoubleExtra("lng",0.0)
//            onLocationUpdate(lat,lng)
//        }
//    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!checkLocationPermissions())requestLocationPermission()
        else isLocationPermissionGranted = true


        Intent(this, LocationService::class.java).also { intent ->
            intent.action= LocationService.ACTION_START
            startService(intent)
        }

//        LocalBroadcastManager.getInstance(this)
//                .registerReceiver(locationBroadcastReceiver, IntentFilter(LocationService.LOCATION_RECEIVER))
    }

    override fun onResume() {
        super.onResume()

        // ask the user for permission to change the location settings if its not available
        changeLocationSetting()
    }

    override fun onStop() {
        super.onStop()
//        LocalBroadcastManager
//                .getInstance(this)
//                .unregisterReceiver(locationBroadcastReceiver)
    }


    fun onLocationUpdate(lat:Double?,lng:Double?){
        Toast.makeText(this,"your Current location $lat , $lng",Toast.LENGTH_LONG).show()
    }


    protected fun changeLocationSetting() {

        mLocationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }


        // Create LocationSettingsRequest object using location request
        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .apply { addLocationRequest(mLocationRequest) }
                .build()


        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnFailureListener{
            if (it is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    it.startResolutionForResult(this@MainActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                isLocationPermissionGranted = (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    fun checkLocationPermissions():Boolean{
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    fun requestLocationPermission(){
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION)
    }
}
