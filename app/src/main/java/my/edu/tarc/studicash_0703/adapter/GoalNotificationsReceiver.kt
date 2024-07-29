package my.edu.tarc.studicash_0703

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import my.edu.tarc.studicash_0703.Transaction.AddTransactionActivity

class GoalNotificationsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalName = intent.getStringExtra("goalName")
        val notificationId = intent.getIntExtra("notificationId", 0)

        val notificationIntent = Intent(context, AddTransactionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val builder = NotificationCompat.Builder(context, "goalChannelId")
            .setSmallIcon(R.drawable.goal)
            .setContentTitle("Goal Reminder")
            .setContentText("Don't forget to save for your goal: $goalName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
