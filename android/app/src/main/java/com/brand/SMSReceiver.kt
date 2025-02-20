package com.brand

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.LocationManager
import android.provider.Settings  // Make sure to add this import

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as? Array<*>
                val messages = pdus?.mapNotNull {
                    SmsMessage.createFromPdu(it as ByteArray)
                }

                messages?.forEach { message ->
                    val sender = message.originatingAddress ?: "Unknown Sender"
                    val messageBody = message.messageBody?.trim() ?: ""

                    Log.d("SMSReceiver", "Message received: $messageBody from $sender")

                    if (messageBody.equals("AT-LOCATION", ignoreCase = true)) {
                        openReactNativeScreen(context)
                        sendLocationSMS(context, sender) // Location SMS function
                    }
                }
            }
        }
    }

    private fun openReactNativeScreen(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("screen", "Hello")
        }
        context.startActivity(intent)
    }

  private fun sendLocationSMS(context: Context, phoneNumber: String) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        // Redirect user to location settings
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
        sendSMS(context, phoneNumber, "Please enable GPS for location access.")
        return
    }

    // Check for ACCESS_FINE_LOCATION and ACCESS_BACKGROUND_LOCATION permissions
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // Request permissions if not granted
        ActivityCompat.requestPermissions(
            context as MainActivity, // Or your Activity context
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            1 // Request code
        )
        sendSMS(context, phoneNumber, "Location permissions not granted.")
        return
    }

    val intent = Intent(context, LocationService::class.java)
    intent.putExtra("phoneNumber", phoneNumber)

    // Starting the service based on Android version
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        context.startForegroundService(intent) // Foreground Service (Android O+)
    } else {
        context.startService(intent)
    }
}


    private fun sendSMS(context: Context, phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("SMSReceiver", "Reply sent to $phoneNumber: $message")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SMSReceiver", "Failed to send SMS: ${e.message}")
            }
        } else {
            Log.e("SMSReceiver", "SEND_SMS permission not granted.")
        }
    }
}
