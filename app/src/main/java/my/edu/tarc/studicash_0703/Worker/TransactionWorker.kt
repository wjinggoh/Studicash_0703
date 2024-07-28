package my.edu.tarc.studicash_0703.Worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class TransactionWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val firestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        val goalName = inputData.getString("goalName") ?: return Result.failure()
        val savingsNeeded = inputData.getDouble("savingsNeeded", 0.0)
        val timestamp = inputData.getLong("timestamp", 0L)

        val transactionData = mapOf(
            "category" to goalName,
            "amount" to savingsNeeded,
            "timestamp" to Date(timestamp)
        )

        firestore.collection("expenseTransactions")
            .add(transactionData)
            .addOnSuccessListener {
                // Automated transaction added successfully
            }
            .addOnFailureListener { e ->
                // Handle failure
            }

        return Result.success()
    }
}
