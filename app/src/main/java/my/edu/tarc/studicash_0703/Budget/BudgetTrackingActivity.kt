package my.edu.tarc.studicash_0703.Budget

import my.edu.tarc.studicash_0703.adapter.BudgetAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.GoalTrackingActivity
import my.edu.tarc.studicash_0703.MainActivity
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.NotificationHelper
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityBudgetTrackingBinding

class BudgetTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetTrackingBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBudgetTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupButtons()

        fetchAndDisplayBudgets()
    }

    private fun setupSpinner() {
        val items = arrayOf("Budget", "Goal")

        val adapter = ArrayAdapter(this, R.layout.spinner_drop_down_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_drop_down_item)

        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> fetchAndDisplayBudgets()
                    1 -> {
                        startActivity(Intent(this@BudgetTrackingActivity, GoalTrackingActivity::class.java))
                        finish()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupButtons() {
        binding.addBudgetBtn.setOnClickListener {
            startActivity(Intent(this, CreateBudgetActivity::class.java))
        }

        binding.BudgetTrackingBackBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun fetchAndDisplayBudgets() {
        val uid = auth.currentUser?.uid ?: run {
            handleError("User not logged in")
            return
        }

        val budgetCollection = firestore.collection("Budget")
        budgetCollection.whereEqualTo("uid", uid) // Filter by UID
            .get()
            .addOnSuccessListener { result ->
                Log.d("BudgetTrackingActivity", "Fetched ${result.size()} budgets")

                val budgets = mutableListOf<BudgetItem>()
                val budgetTasks = mutableListOf<Task<Pair<BudgetItem, Double>>>()
                val reminderBudgets = mutableListOf<BudgetItem>()

                if (result.isEmpty) {
                    Log.d("BudgetTrackingActivity", "No budgets found.")
                }

                for (document in result) {
                    Log.d("BudgetTrackingActivity", "Processing document: ${document.id}")
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val startDate = document.getString("startDate") ?: ""
                    val endDate = document.getString("endDate") ?: ""
                    val icon = document.getLong("icon")?.toInt() ?: 0

                    val budgetItem = BudgetItem(
                        id = id,
                        name = name,
                        category = category,
                        amount = amount,
                        spent = 0.0,
                        progress = 0,
                        startDate = startDate,
                        endDate = endDate,
                        icon = icon
                    )

                    val task = fetchTotalSpentForBudget(category).continueWith { totalSpent ->
                        val progress = if (budgetItem.amount > 0) ((totalSpent.result / budgetItem.amount) * 100).toInt() else 0
                        budgetItem.spent = totalSpent.result
                        budgetItem.progress = progress
                        budgetItem to totalSpent.result
                    }
                    budgetTasks.add(task)
                }

                Tasks.whenAllComplete(budgetTasks).addOnCompleteListener { tasks ->
                    val results = tasks.result
                    val updatedBudgets = mutableListOf<BudgetItem>()

                    results.forEach { task ->
                        val (budgetItem, _) = task.result as Pair<BudgetItem, Double>
                        updatedBudgets.add(budgetItem)

                        // Check if usage exceeds 80%
                        if (budgetItem.progress > 80) {
                            reminderBudgets.add(budgetItem)
                        }
                    }

                    Log.d("BudgetTrackingActivity", "Budgets to display: ${updatedBudgets.size}")
                    if (updatedBudgets.isEmpty()) {
                        binding.budgetTrackingRecycleView.visibility = View.GONE
                        binding.noBudgetsMessage.visibility = View.VISIBLE
                    } else {
                        binding.budgetTrackingRecycleView.visibility = View.VISIBLE
                        binding.noBudgetsMessage.visibility = View.GONE
                        setupRecyclerView(updatedBudgets)
                    }

                    // Trigger notification for budgets that exceed 80% usage
                    if (reminderBudgets.isNotEmpty()) {
                        showReminderNotification(reminderBudgets)
                    }
                }.addOnFailureListener { exception ->
                    handleError("Error fetching budgets: ${exception.message}")
                }
            }.addOnFailureListener { exception ->
                handleError("Error fetching budgets: ${exception.message}")
            }
    }



    private fun fetchTotalSpentForBudget(budgetName: String): Task<Double> {
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

    private fun setupRecyclerView(budgets: List<BudgetItem>) {
        val adapter = BudgetAdapter(
            this,
            budgets,
            onEditClick = { budgetItem ->
                // Navigate to EditBudgetActivity
                val intent = Intent(this, EditBudgetActivity::class.java)
                intent.putExtra("budgetId", budgetItem.id)
                startActivity(intent)
            },
            onDeleteClick = { budgetItem ->
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(budgetItem.id, budgetItem.name)
            }
        )
        binding.budgetTrackingRecycleView.adapter = adapter
        binding.budgetTrackingRecycleView.layoutManager = LinearLayoutManager(this)
    }


    private fun editBudget(budgetItem: BudgetItem) {
        val intent = Intent(this, EditBudgetActivity::class.java)
        intent.putExtra("budgetItem", budgetItem)
        startActivity(intent)
    }




    private fun deleteBudget(budgetId: String) {
        firestore.collection("Budget").document(budgetId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Budget deleted successfully", Toast.LENGTH_SHORT).show()
                // Optionally refresh the RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting budget", Toast.LENGTH_SHORT).show()
                Log.w("TrackBudgetActivity", "Error deleting document", e)
            }
    }


    private fun showDeleteConfirmationDialog(budgetId: String, budgetName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete this budget?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteBudget(budgetId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showReminderNotification(budgets: List<BudgetItem>) {
        budgets.forEach { budgetItem ->
            NotificationHelper.showReminderNotification(this, budgetItem)
        }
    }

    private fun handleError(message: String) {
        binding.noBudgetsMessage.apply {
            visibility = View.VISIBLE
            text = message
        }
        binding.budgetTrackingRecycleView.visibility = View.GONE
    }
}
