package com.brand
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled

class MainActivity : ReactActivity() {

    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 123
        private const val CALL_PERMISSION_REQUEST_CODE = 124
    }

    override fun getMainComponentName(): String = "Project01"

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions
        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            val callPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)

            // Request SMS permission if not granted
            if (smsPermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                    Toast.makeText(this, "SMS permission is required to receive messages.", Toast.LENGTH_LONG).show()
                }
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), SMS_PERMISSION_REQUEST_CODE)
            }

            // Request CALL permission if not granted
            if (callPermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(this, "Call permission is required to make phone calls.", Toast.LENGTH_LONG).show()
                }
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), CALL_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val message = when (requestCode) {
                SMS_PERMISSION_REQUEST_CODE -> "SMS permission granted"
                CALL_PERMISSION_REQUEST_CODE -> "Call permission granted"
                else -> ""
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            val message = when (requestCode) {
                SMS_PERMISSION_REQUEST_CODE -> "SMS permission denied"
                CALL_PERMISSION_REQUEST_CODE -> "Call permission denied"
                else -> ""
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
