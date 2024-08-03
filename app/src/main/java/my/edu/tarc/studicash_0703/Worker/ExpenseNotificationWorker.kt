package my.edu.tarc.studicash_0703.Worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import my.edu.tarc.studicash_0703.MainActivity
import my.edu.tarc.studicash_0703.R

class ExpenseNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Logic to determine if notification should be sent
        sendNotification("You haven't tracked your expenses for today. Please update your records.")
        return Result.success()
    }

    private fun sendNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "expense_tracking_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Expense Tracking", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity and navigate to AddFragment
        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("open_fragment", "add")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, notificationChannelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Expense Tracker")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1, notificationBuilder.build())
    }
}
