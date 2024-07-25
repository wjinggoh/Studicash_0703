package my.edu.tarc.studicash_0703.sidebar

import NotificationItem
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.NotificationAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch notifications
        fetchBudgetExceededNotifications().addOnSuccessListener { notifications ->
            if (notifications.isNotEmpty()) {
                val adapter = NotificationAdapter(this, notifications)
                binding.notificationRecyclerView.adapter = adapter
                binding.notificationRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.noNotificationsMessage.visibility = View.GONE
                binding.notificationRecyclerView.visibility = View.VISIBLE
            } else {
                handleError("No notifications to display.")
            }
        }.addOnFailureListener { exception ->
            handleError("Error fetching notifications: ${exception.message}")
        }

        binding.notificationBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun fetchBudgetExceededNotifications(): Task<List<NotificationItem>> {
        val notificationList = mutableListOf<NotificationItem>()
        val firestore = FirebaseFirestore.getInstance()
        val budgetCollection = firestore.collection("Budget")

        return budgetCollection.get().continueWithTask { result ->
            val tasks = mutableListOf<Task<Triple<String, Double, Double>>>()
            for (document in result.result!!) {
                val amount = document.getDouble("amount") ?: 0.0
                val name = document.getString("name") ?: ""
                val category = document.getString("category") ?: ""

                val spentTask = fetchTotalSpentForBudget(category).continueWith { task ->
                    val spent = task.result ?: 0.0
                    Triple(name, amount, spent)
                }
                tasks.add(spentTask)
            }

            Tasks.whenAllSuccess<Triple<String, Double, Double>>(tasks).addOnSuccessListener { results ->
                for (result in results) {
                    val (name, amount, spent) = result
                    val progress = if (amount > 0) ((spent / amount) * 100).toInt() else 0

                    if (progress > 80) {
                        notificationList.add(
                            NotificationItem(
                                NotificationType.BUDGET_EXCEEDED,
                                "Budget Exceeded",
                                "Your budget '$name' has exceeded 80%. Amount: $amount. Spent: $spent."
                            )
                        )
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("NotificationsActivity", "Error processing budget data: ${exception.message}")
            }
            Tasks.forResult(notificationList)
        }
    }

    private fun fetchTotalSpentForBudget(budgetName: String): Task<Double> {
        val firestore = FirebaseFirestore.getInstance()
        val transactionsCollection = firestore.collection("expenseTransactions")
        val taskCompletionSource = TaskCompletionSource<Double>()

        transactionsCollection.whereEqualTo("category", budgetName).get()
            .addOnSuccessListener { result ->
                var totalSpent = 0.0
                for (document in result) {
                    totalSpent += document.getDouble("amount") ?: 0.0
                }
                taskCompletionSource.setResult(totalSpent)
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }

    private fun handleError(message: String) {
        binding.notificationRecyclerView.visibility = View.GONE
        binding.noNotificationsMessage.apply {
            visibility = View.VISIBLE
            text = message
        }
    }
}
