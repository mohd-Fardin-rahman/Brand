package com.brand

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*

class LocationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun doWork(): Result {
        val phoneNumber = inputData.getString("phoneNumber") ?: return Result.failure()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sendSMS(phoneNumber, "Location permission not granted.")
            return Result.failure()
        }

        // Fetch location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latitude = location.latitude
                val longitude = location.longitude
                val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                sendSMS(phoneNumber, "My current location: $googleMapsLink")
            } ?: run {
                sendSMS(phoneNumber, "Failed to get location.")
            }
        }

        return Result.success()
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
