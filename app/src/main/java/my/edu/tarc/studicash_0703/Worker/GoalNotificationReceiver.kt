import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val goalName = intent.getStringExtra("goalName") ?: return
        val goalAmount = intent.getDoubleExtra("goalAmount", 0.0)
        val goalId = intent.getStringExtra("goalId") ?: return

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Goal").document(goalId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val periodicSavings = document.get("periodicSavings") as? MutableMap<String, Double> ?: mutableMapOf()
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                    val updatedSavings = periodicSavings.toMutableMap()
                    val currentAmount = updatedSavings[currentDate] ?: 0.0
                    updatedSavings[currentDate] = currentAmount + (goalAmount / getFrequencyMultiplier(document.getString("savingFrequency") ?: "Monthly"))

                    val updates = mapOf("periodicSavings" to updatedSavings)
                    document.reference.update(updates)
                        .addOnSuccessListener {
                            // Notify user or update UI
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                        }
                }
            }
    }

    private fun getFrequencyMultiplier(frequency: String): Double {
        return when (frequency) {
            "Weekly" -> 4.0
            "Monthly" -> 1.0
            "Quarterly" -> 1 / 3.0
            "Yearly" -> 1 / 12.0
            else -> 1.0 // Default to monthly if frequency is unknown
        }
    }
}
