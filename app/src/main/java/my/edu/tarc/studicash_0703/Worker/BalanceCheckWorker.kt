import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.MainActivity
import my.edu.tarc.studicash_0703.R
import java.text.DecimalFormat

class BalanceCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            var totalIncome = 0.0
            var totalExpenses = 0.0

            // Fetch income transactions
            firestore.collection("incomeTransactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { incomeSnapshot ->
                    for (document in incomeSnapshot.documents) {
                        val amount = document.getDouble("amount") ?: 0.0
                        totalIncome += amount
                    }

                    // Fetch expense transactions
                    firestore.collection("expenseTransactions")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { expenseSnapshot ->
                            for (document in expenseSnapshot.documents) {
                                val amount = document.getDouble("amount") ?: 0.0
                                totalExpenses += amount
                            }

                            // Calculate balance
                            val balance = totalIncome - totalExpenses
                            val df = DecimalFormat("0.00")

                            // Send notification if balance is negative
                            if (balance < 0) {
                                sendNotification(balance)
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure
                        }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
        return Result.success()
    }

    private fun sendNotification(balance: Double) {
        val context = applicationContext
        val notificationManager = NotificationManagerCompat.from(context)

        // Create an intent to open the main activity with the profile fragment
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_fragment", "profile") // Optional: use extra to specify which fragment to open
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "balance_notification_channel")
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Balance Alert")
            .setContentText("Your balance is negative: $balance, please be alert!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
