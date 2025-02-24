package com.brand

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

    override suspend fun doWork(): Result {
        val phoneNumber = inputData.getString("phoneNumber") ?: return Result.failure()

        if (!hasLocationPermission()) {
            sendSMS(phoneNumber, "Location permission not granted.")
            return Result.failure()
        }

        val location = getCurrentLocation()
        return if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
            sendSMS(phoneNumber, "My current location: $googleMapsLink")
            Result.success()
        } else {
            sendSMS(phoneNumber, "Failed to get location.")
            Result.retry()
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(2000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(locationResult.lastLocation)
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (!locationAvailability.isLocationAvailable) {
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(null)
                    }
                }
            }

            if (hasLocationPermission()) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } else {
                continuation.resume(null)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("LocationWorker", "Reply sent to $phoneNumber: $message")
            } catch (e: Exception) {
                Log.e("LocationWorker", "Failed to send SMS: ${e.message}")
            }
        } else {
            Log.e("LocationWorker", "SEND_SMS permission not granted.")
        }
    }
}
