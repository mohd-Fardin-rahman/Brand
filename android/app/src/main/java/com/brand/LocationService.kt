
package com.brand

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phoneNumber") ?: return START_NOT_STICKY
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if fine location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Handle background location permission if needed (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request background location permission
                requestBackgroundLocationPermission()
                return START_NOT_STICKY
            }

            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                sendSMS(phoneNumber, "Please enable GPS for location access.")
                stopSelf()
                return START_NOT_STICKY
            }

            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 5000 // 5 seconds
                fastestInterval = 2000
                numUpdates = 1 // Only one location update
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        sendLocationMessage(phoneNumber, location)
                    } ?: run {
                        sendSMS(phoneNumber, "Failed to get location.")
                    }
                    stopSelf()
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            sendSMS(phoneNumber, "Location permission not granted.")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun sendLocationMessage(phoneNumber: String, location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        sendSMS(phoneNumber, "My current location: $googleMapsLink")
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = android.telephony.SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("LocationService", "Reply sent to $phoneNumber: $message")
            } catch (e: Exception) {
                Log.e("LocationService", "Failed to send SMS: ${e.message}")
            }
        } else {
            Log.e("LocationService", "SEND_SMS permission not granted.")
        }
    }

    private fun startForegroundService() {
        val channelId = "location_service"
        val channelName = "Location Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText("Getting your location...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification) // Start foreground service
    }

    private fun requestBackgroundLocationPermission() {
        // Request background location permission (may need to show UI to user)
        Log.d("LocationService", "Requesting background location permission.")
        // You can use an Activity or a UI component to request permission here
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
