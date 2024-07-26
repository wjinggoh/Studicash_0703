package my.edu.tarc.studicash_0703

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.GoalItem
import java.util.Calendar

class GoalTransactionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getStringExtra("goalId")
        val goalName = intent.getStringExtra("goalName")
        val goalAmount = intent.getDoubleExtra("goalAmount", 0.0)
        val goalStartDate = intent.getStringExtra("goalStartDate")
        val goalEndDate = intent.getStringExtra("goalEndDate")
        val goalSaved = intent.getDoubleExtra("goalSaved", 0.0)
        val goalProgress = intent.getIntExtra("goalProgress", 0)

        val goal = GoalItem(
            id = goalId ?: "",
            name = goalName ?: "",
            amount = goalAmount,
            saved = goalSaved,
            progress = goalProgress,
            startDate = goalStartDate ?: "",
            endDate = goalEndDate ?: ""
        )

        when (intent.action) {
            "CREATE_TRANSACTION" -> createTransactionForGoal(context, goal)
        }
    }

    private fun createTransactionForGoal(context: Context, goal: GoalItem) {
        val firestore = FirebaseFirestore.getInstance()

        val transaction = mapOf(
            "category" to goal.name,
            "amount" to goal.amount, // or some other logic to determine amount
            "date" to getCurrentDate()
        )

        firestore.collection("expenseTransactions").add(transaction).addOnSuccessListener {
            updateGoalProgress(context, goal)
        }.addOnFailureListener { exception ->
            // Handle error
        }
    }

    private fun updateGoalProgress(context: Context, goal: GoalItem) {
        val firestore = FirebaseFirestore.getInstance()
        goal.saved += goal.amount // or some other logic to update saved amount
        goal.progress = ((goal.saved / goal.amount) * 100).toInt()

        firestore.collection("Goal").document(goal.id).set(goal).addOnSuccessListener {
            // Goal progress updated
        }.addOnFailureListener { exception ->
            // Handle error
        }
    }

    private fun getCurrentDate(): String {
        // Implement a method to get the current date as a string
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}
