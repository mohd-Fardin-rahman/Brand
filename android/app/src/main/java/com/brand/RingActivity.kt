import android.media.RingtoneManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the ringtone
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone.play()

        // Close the activity once the ringtone starts
        finish()
    }
}
