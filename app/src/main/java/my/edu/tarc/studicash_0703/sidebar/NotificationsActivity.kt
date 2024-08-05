package my.edu.tarc.studicash_0703.sidebar

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.NotificationItem
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.NotificationAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityNotificationsBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize RecyclerView
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchAllNotifications().addOnSuccessListener { notifications ->
            if (notifications.isNotEmpty()) {
                val adapter = NotificationAdapter(this, notifications.toMutableList())
                binding.notificationRecyclerView.adapter = adapter
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

    private fun fetchAllNotifications(): Task<List<NotificationItem>> {
        val notifications = mutableListOf<NotificationItem>()
        val budgetTask = fetchBudgetExceededNotifications()
        val transactionTask = fetchTransactionNotifications()
        val createdBudgetTask = fetchCreatedBudgetNotifications()

        return Tasks.whenAllSuccess<List<NotificationItem>>(budgetTask, transactionTask, createdBudgetTask)
            .continueWith { task ->
                val results = task.result ?: emptyList()
                results.forEach { resultList ->
                    notifications.addAll(resultList)
                }
                Log.d("NotificationsActivity", "Fetched Notifications: $notifications")
                notifications
            }
    }



    private fun fetchTransactionNotifications(): Task<List<NotificationItem>> {
        val notificationList = mutableListOf<NotificationItem>()
        val firestore = FirebaseFirestore.getInstance()
        val transactionsCollection = firestore.collection("expenseTransactions")
        val currentUserUid = getCurrentUserUid()

        Log.d("NotificationsActivity", "Fetching transactions for UID: $currentUserUid")

        val taskCompletionSource = TaskCompletionSource<List<NotificationItem>>()

        transactionsCollection.whereEqualTo("userId", currentUserUid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)  // Adjust as needed
            .get()
            .addOnSuccessListener { result ->
                Log.d("NotificationsActivity", "Firestore result: ${result.size()} documents")
                if (result.isEmpty) {
                    Log.d("NotificationsActivity", "No transactions found.")
                } else {
                    for (document in result) {
                        val amount = document.getDouble("amount") ?: 0.0
                        val title = "New Transaction Alert"
                        val message = "A transaction of amount $amount has been made."
                        val date = getCurrentFormattedDate()

                        Log.d("NotificationsActivity", "Processing transaction: Amount: $amount")

                        if (amount > 0.0) {  // Adjust threshold as needed
                            val notification = NotificationItem(
                                NotificationType.TRANSACTIONS_DONE,
                                title,
                                message,
                                date
                            )
                            notificationList.add(notification)
                            Log.d("NotificationsActivity", "Added Notification: $notification | Date: $date")
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
                                type = NotificationType.BUDGET_EXCEEDED,
                                title = "Budget Exceeded",
                                message = "Your budget '$name' has exceeded 80%. Amount: $amount. Spent: $spent.",
                                date = getCurrentFormattedDate()
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun fetchTotalSpentForBudget(budgetCategory: String): Task<Double> {
        val firestore = FirebaseFirestore.getInstance()
        val transactionsCollection = firestore.collection("expenseTransactions")
        val taskCompletionSource = TaskCompletionSource<Double>()

        val currentUid = getCurrentUserUid()
        Log.d("NotificationsActivity", "Fetching transactions for category: $budgetCategory and UID: $currentUid")

        transactionsCollection.whereEqualTo("category", budgetCategory)
            .whereEqualTo("userId", currentUid)
            .get()
            .addOnSuccessListener { result ->
                var totalSpent = 0.0
                Log.d("NotificationsActivity", "Transactions found: ${result.documents.size}")
                for (document in result) {
                    val amount = document.getDouble("amount") ?: 0.0
                    Log.d("NotificationsActivity", "Transaction amount: $amount")
                    totalSpent += amount
                }
                Log.d("NotificationsActivity", "Total spent for category '$budgetCategory': $totalSpent")
                taskCompletionSource.setResult(totalSpent)
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationsActivity", "Error fetching transactions: ${exception.message}")
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }


    private fun fetchCreatedBudgetNotifications(): Task<List<NotificationItem>> {
        val notificationList = mutableListOf<NotificationItem>()
        val firestore = FirebaseFirestore.getInstance()
        val budgetCollection = firestore.collection("Budget")

        val currentUserUid = getCurrentUserUid()
        Log.d("NotificationsActivity", "Fetching created budgets for UID: $currentUserUid")

        val taskCompletionSource = TaskCompletionSource<List<NotificationItem>>()

        // Fetch budgets created within the last 24 hours
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        budgetCollection
            .whereEqualTo("uid", currentUserUid)
            .get()
            .addOnSuccessListener { result ->
                Log.d("NotificationsActivity", "Firestore result: ${result.documents.size} documents")
                if (result.isEmpty) {
                    Log.d("NotificationsActivity", "No newly created budgets found for UID: $currentUserUid")
                } else {
                    for (document in result) {
                        val name = document.getString("name") ?: "Unnamed Budget"
                        val amount = document.getDouble("amount") ?: 0.0
                        val category = document.getString("category") ?: "Uncategorized"
                        val date = getCurrentFormattedDate()

                        Log.d("NotificationsActivity", "Processing created budget: Name: $name, Amount: $amount, Category: $category")

                        val notification = NotificationItem(
                            type = NotificationType.BUDGET_CREATED,
                            title = "New Budget Created",
                            message = "A new budget '$name' has been created with an amount of $amount in category '$category'.",
                            date = date
                        )
                        notificationList.add(notification)
                        Log.d("NotificationsActivity", "Added Notification: $notification | Date: $date")
                    }
                    taskCompletionSource.setResult(notificationList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationsActivity", "Error fetching created budgets: ${exception.message}")
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
