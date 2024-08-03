package my.edu.tarc.studicash_0703.Worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.R
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

class ExpenseTrackingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        val userId = "USER_ID" // Retrieve the actual user ID from context or preferences

        val lastTrackedDate = firestore.collection("users").document(userId).get().result?.getDate("lastTrackedDate")
        val today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())

        if (lastTrackedDate == null || !isSameDay(lastTrackedDate, today)) {
            sendNotification("You haven't tracked your expenses for today. Please update your records.")
        }

        return Result.success()
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    private fun sendNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "expense_tracking_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Expense Tracking", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, notificationChannelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Expense Tracker")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, notificationBuilder.build())
    }
}
