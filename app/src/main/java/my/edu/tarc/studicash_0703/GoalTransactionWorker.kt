package my.edu.tarc.studicash_0703

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.GoalItem
import java.util.*

class GoalTransactionWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        val goalCollection = firestore.collection("Goal")

        goalCollection.get().addOnSuccessListener { result ->
            for (document in result) {
                val goal = document.toObject(GoalItem::class.java)
                if (shouldPromptForTransaction(goal)) {
                    showNotification(applicationContext, goal)
                }
            }
        }.addOnFailureListener { exception ->
            // Handle error
        }

        return Result.success()
    }

    private fun shouldPromptForTransaction(goal: GoalItem): Boolean {
        // Example logic: check if it's the end of the month
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        return today == calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun showNotification(context: Context, goal: GoalItem) {
        val channelId = "goal_channel"
        val channelName = "Goal Channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val yesIntent = Intent(context, GoalTransactionReceiver::class.java).apply {
            action = "CREATE_TRANSACTION"
            putExtra("goalId", goal.id)
            putExtra("goalName", goal.name)
            putExtra("goalAmount", goal.amount)
            putExtra("goalStartDate", goal.startDate)
            putExtra("goalEndDate", goal.endDate)
            putExtra("goalSaved", goal.saved)
            putExtra("goalProgress", goal.progress)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(context, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Create Transaction for Goal")
            .setContentText("Do you want to create a transaction for your goal: ${goal.name}?")
            .setSmallIcon(R.drawable.goal)
            .setAutoCancel(true)
            .addAction(R.drawable.goal, "Yes", yesPendingIntent)
            .addAction(R.drawable.baseline_cancel_24, "No", null)
            .build()

        notificationManager.notify(goal.id.hashCode(), notification)
    }
}
