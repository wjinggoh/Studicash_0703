package my.edu.tarc.studicash_0703.Worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import my.edu.tarc.studicash_0703.Transaction.AddTransactionActivity

class GoalTransactionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CREATE_TRANSACTION") {
            val goalId = intent.getStringExtra("goalId")
            val goalName = intent.getStringExtra("goalName")
            val goalAmount = intent.getDoubleExtra("goalAmount", 0.0)
            val goalStartDate = intent.getStringExtra("goalStartDate")
            val goalEndDate = intent.getStringExtra("goalEndDate")
            val goalSaved = intent.getDoubleExtra("goalSaved", 0.0)
            val goalProgress = intent.getDoubleExtra("goalProgress", 0.0)

            val addTransactionIntent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("goalId", goalId)
                putExtra("goalName", goalName)
                putExtra("goalAmount", goalAmount)
                putExtra("goalStartDate", goalStartDate)
                putExtra("goalEndDate", goalEndDate)
                putExtra("goalSaved", goalSaved)
                putExtra("goalProgress", goalProgress)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(addTransactionIntent)
        }
    }
}
