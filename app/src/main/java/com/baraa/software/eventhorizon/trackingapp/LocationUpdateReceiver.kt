package com.baraa.software.eventhorizon.trackingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class LocationUpdateReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var lat = intent?.getDoubleExtra("lat",0.0)
        var lng = intent?.getDoubleExtra("lng",0.0)

        Toast.makeText(context,"your Current location $lat , $lng", Toast.LENGTH_LONG).show()
    }
}