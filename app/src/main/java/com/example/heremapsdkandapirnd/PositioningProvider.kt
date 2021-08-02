package com.example.heremapsdkandapirnd

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat


class PositioningProvider(val context: Context) : LocationListener {
    private var locationManager: LocationManager? = null

    private var platformLocationListener: PlatformLocationListener? = null

    interface PlatformLocationListener {
        fun onLocationUpdated(location: Location?)
    }

    override fun onLocationChanged(location: Location) {
        if (platformLocationListener != null) {
            platformLocationListener!!.onLocationUpdated(location)
        }
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        when (status) {
            LocationProvider.AVAILABLE -> Log.d(
                LOG_TAG,
                "PlatformPositioningProvider status: AVAILABLE"
            )
            LocationProvider.OUT_OF_SERVICE -> Log.d(
                LOG_TAG,
                "PlatformPositioningProvider status: OUT_OF_SERVICE"
            )
            LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d(
                LOG_TAG,
                "PlatformPositioningProvider status: TEMPORARILY_UNAVAILABLE"
            )
            else -> Log.d(LOG_TAG, "PlatformPositioningProvider status: UNKNOWN")
        }
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(LOG_TAG, "PlatformPositioningProvider enabled.")
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(LOG_TAG, "PlatformPositioningProvider disabled.")
        super.onProviderDisabled(provider)
    }


    fun startLocating(locationCallback: PlatformLocationListener?) {
        if (platformLocationListener != null) {
            throw RuntimeException("Please stop locating before starting again.")
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(LOG_TAG, "Positioning permissions denied.")
            return
        }
        platformLocationListener = locationCallback
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager?
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        ) {
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_INTERVAL_IN_MS.toLong(),
                1f,
                this
            )
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                LOCATION_UPDATE_INTERVAL_IN_MS.toLong(),
                1f,
                this
            )
        } else {
            Log.d(LOG_TAG, "Positioning not possible.")
            stopLocating()
        }
    }

    fun stopLocating() {
        if (locationManager == null) {
            return
        }
        locationManager!!.removeUpdates(this)
        platformLocationListener = null
    }

    companion object {
        val LOG_TAG = "PositioningProvide"
        const val LOCATION_UPDATE_INTERVAL_IN_MS = 1000
    }
}