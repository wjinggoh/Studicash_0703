import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun scheduleTransactions(
    context: Context,
    goalName: String,
    goalAmount: Double,
    startDate: String,
    endDate: String,
    frequency: String,
    savingsNeeded: Double
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val start = sdf.parse(startDate)
    val end = sdf.parse(endDate)
    val calendar = Calendar.getInstance()
    calendar.time = start

    val firestore = FirebaseFirestore.getInstance()

    while (calendar.time.before(end) || calendar.time.equals(end)) {
        val transactionData = mapOf(
            "goalName" to goalName,
            "savingsNeeded" to savingsNeeded,
            "date" to sdf.format(calendar.time)
        )

        firestore.collection("scheduledTransactions")
            .add(transactionData)
            .addOnSuccessListener {
                // Scheduled transaction added successfully
            }
            .addOnFailureListener { e ->
                // Handle the error
            }

        when (frequency) {
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "Bi-weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            "Monthly" -> calendar.add(Calendar.MONTH, 1)
            "Quarterly" -> calendar.add(Calendar.MONTH, 3)
        }
    }

    // Schedule the periodic work request
    val workRequest = PeriodicWorkRequestBuilder<CheckSavingsWorker>(1, TimeUnit.DAYS).build()
    WorkManager.getInstance(context).enqueue(workRequest)
}
