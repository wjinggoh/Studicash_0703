package my.edu.tarc.studicash_0703

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import my.edu.tarc.studicash_0703.Transaction.AddTransactionActivity

class GoalNotificationsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalName = intent.getStringExtra("goalName") ?: "Your Goal"
        val estimatedAmount = intent.getDoubleExtra("estimatedAmount", 0.0)
        val notificationId = goalName.hashCode()

        showNotification(context, goalName, estimatedAmount, notificationId)
    }

    private fun showNotification(context: Context, goalName: String, estimatedAmount: Double, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Intent for opening AddTransactionActivity
        val transactionIntent = Intent(context, AddTransactionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("amount", estimatedAmount) // Pass estimated amount
            putExtra("goalName", goalName)
        }
        val pendingTransactionIntent = PendingIntent.getActivity(
            context, notificationId, transactionIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create notification channel if needed
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "goalChannelId"
            val channelName = "Goal Reminder Channel"
            val channelDescription = "Channel for goal reminders"

            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = channelDescription
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification
        val builder = NotificationCompat.Builder(context, "goalChannelId")
            .setSmallIcon(R.drawable.notification) // Use your notification icon
            .setContentTitle("Goal Reminder")
            .setContentText("Don't forget to save for your goal: $goalName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingTransactionIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.add__1_, // Use your action icon
                "Save Now", // Action button text
                pendingTransactionIntent
            )

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handle the exception if needed
            e.printStackTrace()
        }
    }
}
