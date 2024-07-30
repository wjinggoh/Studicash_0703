package my.edu.tarc.studicash_0703.sidebar

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.NotificationAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch budget exceeded notifications
        fetchBudgetExceededNotifications().addOnSuccessListener { notifications ->
            if (notifications.isNotEmpty()) {
                val adapter = NotificationAdapter(this, notifications)
                binding.notificationRecyclerView.adapter = adapter
                binding.noNotificationsMessage.visibility = View.GONE
                binding.notificationRecyclerView.visibility = View.VISIBLE
            } else {
                handleError("No budget notifications to display.")
            }
        }.addOnFailureListener { exception ->
            handleError("Error fetching budget notifications: ${exception.message}")
        }

        // Fetch transaction notifications
        fetchTransactionNotifications().addOnSuccessListener { notifications ->
            if (notifications.isNotEmpty()) {
                val adapter = NotificationAdapter(this, notifications)
                binding.notificationRecyclerView.adapter = adapter
                binding.noNotificationsMessage.visibility = View.GONE
                binding.notificationRecyclerView.visibility = View.VISIBLE
            } else {
                handleError("No transaction notifications to display.")
            }
        }.addOnFailureListener { exception ->
            handleError("Error fetching transaction notifications: ${exception.message}")
        }

        binding.notificationBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun fetchTransactionNotifications(): Task<List<NotificationItem>> {
        val notificationList = mutableListOf<NotificationItem>()
        val firestore = FirebaseFirestore.getInstance()
        val transactionsCollection = firestore.collection("expenseTransactions")

        val currentUserUid = getCurrentUserUid()
        Log.d("NotificationsActivity", "Fetching transactions for UID: $currentUserUid")

        val taskCompletionSource = TaskCompletionSource<List<NotificationItem>>()

        transactionsCollection.whereEqualTo("uid", currentUserUid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10) // Limit the number of transactions fetched, if needed
            .get()
            .addOnSuccessListener { result ->
                Log.d("NotificationsActivity", "Firestore result: ${result.documents.size} documents")
                if (result.isEmpty) {
                    Log.d("NotificationsActivity", "No transactions found for UID: $currentUserUid")
                } else {
                    for (document in result) {
                        val amount = document.getDouble("amount") ?: 0.0
                        val title = "New Transaction Alert"
                        val message = "A transaction of amount $amount has been made."

                        Log.d("NotificationsActivity", "Processing transaction: Amount: $amount")

                        if (amount > 100.0) { // Example criteria: Notify if transaction amount > 100
                            val notification = NotificationItem(
                                NotificationType.TRANSACTIONS_DONE,
                                title,
                                message
                            )
                            notificationList.add(notification)
                            Log.d("NotificationsActivity", "Added Notification: $notification | Date: ${getCurrentFormattedDate()}")
                        }
                    }
                    taskCompletionSource.setResult(notificationList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationsActivity", "Error fetching transactions: ${exception.message}")
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }

    private fun fetchBudgetExceededNotifications(): Task<List<NotificationItem>> {
        val notificationList = mutableListOf<NotificationItem>()
        val firestore = FirebaseFirestore.getInstance()
        val budgetCollection = firestore.collection("Budget")

        val currentUserUid = getCurrentUserUid()
        Log.d("NotificationsActivity", "Fetching budgets for UID: $currentUserUid")

        val taskCompletionSource = TaskCompletionSource<List<NotificationItem>>()

        budgetCollection.whereEqualTo("uid", currentUserUid).get().addOnSuccessListener { result ->
            Log.d("NotificationsActivity", "Firestore result: ${result.documents.size} documents")
            if (result.isEmpty) {
                Log.d("NotificationsActivity", "No documents found for UID: $currentUserUid")
            } else {
                for (document in result) {
                    Log.d("NotificationsActivity", "Document data: ${document.data}")
                }

                val tasks = mutableListOf<Task<Triple<String, Double, Double>>>()
                for (document in result) {
                    val amount = document.getDouble("amount") ?: 0.0
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""

                    Log.d("NotificationsActivity", "Processing budget: $name, Amount: $amount, Category: $category")

                    val spentTask = fetchTotalSpentForBudget(category).continueWith { task ->
                        val spent = task.result ?: 0.0
                        Log.d("NotificationsActivity", "Total spent for $name: $spent")
                        Triple(name, amount, spent)
                    }
                    tasks.add(spentTask)
                }

                Tasks.whenAllSuccess<Triple<String, Double, Double>>(tasks).addOnSuccessListener { results ->
                    for (result in results) {
                        val (name, amount, spent) = result
                        val progress = if (amount > 0) ((spent / amount) * 100).toInt() else 0

                        if (progress > 80) {
                            val notification = NotificationItem(
                                NotificationType.BUDGET_EXCEEDED,
                                "Budget Exceeded",
                                "Your budget '$name' has exceeded 80%. Amount: $amount. Spent: $spent."
                            )
                            notificationList.add(notification)
                            Log.d("NotificationsActivity", "Added Notification: $notification | Date: ${getCurrentFormattedDate()}")
                        }
                    }
                    taskCompletionSource.setResult(notificationList)
                }.addOnFailureListener { exception ->
                    Log.e("NotificationsActivity", "Error processing budget data: ${exception.message}")
                    taskCompletionSource.setException(exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("NotificationsActivity", "Error fetching budgets: ${exception.message}")
            taskCompletionSource.setException(exception)
        }

        return taskCompletionSource.task
    }




    // Dummy method to get the current user's UID
    private fun getCurrentUserUid(): String? {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        return currentUser?.uid
    }

    // Format date as string
    private fun getCurrentFormattedDate(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val date = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
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
