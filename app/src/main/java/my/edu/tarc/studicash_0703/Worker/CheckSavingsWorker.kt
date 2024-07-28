import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CheckSavingsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val firestore = FirebaseFirestore.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())

        // Retrieve scheduled transactions
        firestore.collection("scheduledTransactions")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val goalName = document.getString("goalName")
                    val savingsNeeded = document.getDouble("savingsNeeded")

                    // Notify user
                    if (goalName != null && savingsNeeded != null) {
                        notifyUser(goalName, savingsNeeded)
                    }
                }
            }

        return Result.success()
    }

    private fun notifyUser(goalName: String, savingsNeeded: Double) {
        // Implement your notification logic here
        // This can include creating a notification that, when clicked, opens an Activity to confirm the transaction
    }
}