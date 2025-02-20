package com.brand

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo

class CallService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    // Incoming call, start the service with the number
                    Log.d("CallService", "Incoming call from: $phoneNumber")
                    if (phoneNumber != null) {
                        startForegroundService(phoneNumber)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    // Call has been answered
                    Log.d("CallService", "Call answered.")
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    // Call ended
                    Log.d("CallService", "Call ended.")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Get the TelephonyManager system service
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create notification and start service in foreground
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "CALL_SERVICE_CHANNEL")
            .setContentTitle("Processing Call")
            .setContentText("Listening for incoming calls...")
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(1, notification)
        }

        return START_STICKY
    }

    // Create notification channel (required for Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CALL_SERVICE_CHANNEL", "Call Service", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    // Handle incoming call action and start the call process
    private fun startForegroundService(phoneNumber: String) {
        val intent = Intent(this, CallService::class.java)
        intent.putExtra("PHONE_NUMBER", phoneNumber)
        startService(intent)  // Call the service with the phone number
    }

    // Return null as this is a started service and not bound
    override fun onBind(intent: Intent?): IBinder? = null
}
