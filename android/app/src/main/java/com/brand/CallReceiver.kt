package com.brand

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra("state")
            when (state) {
                "RINGING" -> {
                    val incomingNumber = intent.getStringExtra("incoming_number")
                    Log.d("CallReceiver", "Incoming call from: $incomingNumber")
                }
                "OFFHOOK" -> {
                    Log.d("CallReceiver", "Call is active (OFFHOOK)")
                }
                "IDLE" -> {
                    Log.d("CallReceiver", "Call is idle")
                }
            }
        }
    }
}
