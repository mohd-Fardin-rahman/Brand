package com.brand

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.net.Uri
import androidx.core.app.ActivityCompat
import android.app.Activity

class CallReceiver : BroadcastReceiver() {

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

                    Log.d("CallReceiver", "Message received: $messageBody from $sender")

                    // Check if message matches the keyword
                    if (messageBody.equals("AT-CALL", ignoreCase = true)) {
                        initiateCall(context, sender)
                    }
                }
            }
        }
    }

    private fun initiateCall(context: Context, phoneNumber: String) {
        // Check if CALL_PHONE permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // This flag ensures proper activity launch from receiver
            context.startActivity(callIntent)
            Log.d("CallReceiver", "Initiating call to $phoneNumber")
        } else {
            // If permission is not granted, request permission through Activity
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), 1)
            } else {
                Log.e("CallReceiver", "CALL_PHONE permission not granted.")
            }
        }
    }
}
