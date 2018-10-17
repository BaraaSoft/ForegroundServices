package com.baraa.software.eventhorizon.trackingapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannel.DEFAULT_CHANNEL_ID
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.google.android.gms.location.*

class LocationService:Service() {

    companion object {
        const val ACTION_START = "LocationService.start"
        const val ACTION_STOP = "LocationService.stop"
        const val LOCATION_RECEIVER = "trackingapp.action.location.change"

        const val LOCATION_NOTIFICATON_CHANNEL = "location_notification_channel"
    }

    val REQUEST_CHECK_SETTINGS:Int = 1234
    private val UPDATE_INTERVAL = (10 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    lateinit var mLocationRequest: LocationRequest
    var serviceId:Int? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var action = intent?.action
        when(action){
            ACTION_START -> {
                stopService(serviceId)
                serviceId = startId
                startLocationUpdates()
                buildNotification()
            }
            ACTION_STOP ->{
                stopService(serviceId)
            }
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    fun stopService(startId: Int?){
        startId?.let { stopSelf(it) }
    }


    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(mLocationRequest)
        }.build()

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this)


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                // do work here
                onLocationChanged(locationResult?.lastLocation)
            }
        }, Looper.myLooper())

    }


    private fun onLocationChanged(lastLocation: Location?) {
//        Log.e("LocationService","ID ::$serviceId >>${lastLocation?.latitude}, ${lastLocation?.longitude}")
//        LocalBroadcastManager.getInstance(this)
//                .also {
//                    Intent(LOCATION_RECEIVER).apply {
//                        putExtra("lat",lastLocation?.latitude)
//                        putExtra("lng",lastLocation?.longitude)
//                    }.also {intent ->
//                        it.sendBroadcast(intent)
//                    }
//                }
        Intent().apply {
            action = LOCATION_RECEIVER
            putExtra("lat",lastLocation?.latitude)
            putExtra("lng",lastLocation?.longitude)
            sendBroadcast(this)
        }
        val textContent = "Your current location is ${lastLocation?.latitude}, ${lastLocation?.longitude}"
        updateNotification(serviceId ?:0,textContent)
    }


    private fun dissmissNotification(notificationId:Int){
        with(NotificationManagerCompat.from(this)) {
            // dismiss notification based on it's Id
            serviceId?.run {
                cancel(this)
            }

        }
    }


    private fun updateNotification(notificationId:Int,txt:String){
        val notification =  NotificationCompat.Builder(this, LOCATION_NOTIFICATON_CHANNEL)
                .setSmallIcon(R.drawable.map_ok)
                .setOnlyAlertOnce(true).setContentText(txt)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.map_normal))
                .build()
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            serviceId?.run {
                notify(this, notification)
            }

        }
    }


    private fun buildNotification(){
        val notification = NotificationCompat.Builder(this, LOCATION_NOTIFICATON_CHANNEL)
                .setSmallIcon(R.drawable.map_ok)
                .setContentTitle("Your current location")
                .setContentText("Check out your current location")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.map_normal))
                .build()
        startForeground(serviceId!!, notification)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText ="location update notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(DEFAULT_CHANNEL_ID, LOCATION_NOTIFICATON_CHANNEL, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



}