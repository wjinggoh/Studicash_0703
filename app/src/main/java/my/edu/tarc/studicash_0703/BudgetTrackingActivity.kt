package my.edu.tarc.studicash_0703

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
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.adapter.BudgetAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityBudgetTrackingBinding

class BudgetTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetTrackingBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityBudgetTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Spinner with options
        val items = arrayOf("Budget", "Goal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        // Set default selection to Budget
        binding.spinner.setSelection(0)

        // Set up the OnItemSelectedListener for Spinner
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Stay on BudgetTrackingActivity
                        fetchAndDisplayBudgets()
                    }
                    1 -> {
                        // Switch to GoalTrackingActivity
                        val intent = Intent(this@BudgetTrackingActivity, GoalTrackingActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: Close this activity if you don't want to return to it
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when no item is selected
            }
        }

        binding.addBudgetBtn.setOnClickListener {
            val intent = Intent(this, CreateBudgetActivity::class.java)
            startActivity(intent)
        }

        binding.BudgetTrackingBackBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Fetch and display budgets initially
        fetchAndDisplayBudgets()
    }

    private fun fetchAndDisplayBudgets() {
        val budgetCollection = firestore.collection("Budget")
        budgetCollection.get().addOnSuccessListener { result ->
            Log.d("BudgetTrackingActivity", "Fetched ${result.size()} budgets")

            val budgets = mutableListOf<BudgetItem>()
            val budgetTasks = mutableListOf<Task<Pair<BudgetItem, Double>>>()

            if (result.isEmpty) {
                Log.d("BudgetTrackingActivity", "No budgets found.")
            }

            for (document in result) {
                val id = document.id
                val category = document.getString("category") ?: ""
                val amount = document.getDouble("amount") ?: 0.0
                val startDate = document.getString("startDate") ?: ""
                val endDate = document.getString("endDate") ?: ""
                val icon = document.getLong("icon")?.toInt() ?: 0
                Log.d("BudgetTrackingActivity", "Document data: category=$category, amount=$amount, startDate=$startDate, endDate=$endDate, icon=$icon")
                val budgetItem = BudgetItem(id, category, amount, 0.0, 0, startDate, endDate, icon)

                val task = fetchTotalSpentForBudget(category).continueWith { totalSpent ->
                    budgetItem to totalSpent.result
                }
                budgetTasks.add(task)
            }

            Tasks.whenAllComplete(budgetTasks).addOnCompleteListener { tasks ->
                val results = tasks.result
                results.forEach { task ->
                    val (budgetItem, spent) = task.result as Pair<BudgetItem, Double>
                    val progress = if (budgetItem.amount > 0) ((spent / budgetItem.amount) * 100).toInt() else 0
                    budgetItem.spent = spent
                    budgetItem.progress = progress
                    budgets.add(budgetItem)
                }

                Log.d("BudgetTrackingActivity", "Budgets to display: ${budgets.size}")
                if (budgets.isEmpty()) {
                    binding.budgetTrackingRecycleView.visibility = View.GONE
                    binding.noBudgetsMessage.visibility = View.VISIBLE
                } else {
                    binding.budgetTrackingRecycleView.visibility = View.VISIBLE
                    binding.noBudgetsMessage.visibility = View.GONE
                    setupRecyclerView(budgets)
                }
            }.addOnFailureListener { exception ->
                binding.budgetTrackingRecycleView.visibility = View.GONE
                binding.noBudgetsMessage.visibility = View.VISIBLE
                binding.noBudgetsMessage.text = "Error fetching budgets: ${exception.message}"
            }
        }.addOnFailureListener { exception ->
            binding.budgetTrackingRecycleView.visibility = View.GONE
            binding.noBudgetsMessage.visibility = View.VISIBLE
            binding.noBudgetsMessage.text = "Error fetching budgets: ${exception.message}"
        }
    }

    private fun fetchTotalSpentForBudget(budgetName: String): Task<Double> {
        val transactionsCollection = firestore.collection("expenseTransactions")
        val taskCompletionSource = TaskCompletionSource<Double>()

        transactionsCollection
            .whereEqualTo("category", budgetName)
            .get()
            .addOnSuccessListener { result ->
                var totalSpent = 0.0
                for (document in result) {
                    val amount = document.getDouble("amount") ?: 0.0
                    totalSpent += amount
                }
                Log.d("BudgetTrackingActivity", "Total spent for $budgetName: $totalSpent")
                taskCompletionSource.setResult(totalSpent)
            }
            .addOnFailureListener { exception ->
                Log.e("BudgetTrackingActivity", "Error fetching transactions for $budgetName: ${exception.message}")
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }

    private fun setupRecyclerView(budgets: List<BudgetItem>) {
        val adapter = BudgetAdapter(
            this, // Pass the context
            budgets,
            onEditClick = { budgetItem ->
                // Handle edit click
                editBudget(budgetItem)
            },
            onDeleteClick = { budgetItem ->
                // Handle delete click
                deleteBudget(budgetItem)
            }
        )
        binding.budgetTrackingRecycleView.adapter = adapter
        binding.budgetTrackingRecycleView.layoutManager = LinearLayoutManager(this)
    }


    private fun editBudget(budgetItem: BudgetItem) {
        // Navigate to EditBudgetActivity with the selected budgetItem details
        val intent = Intent(this, EditBudgetActivity::class.java).apply {
            putExtra("id", budgetItem.id)
            putExtra("category", budgetItem.category)
            putExtra("amount", budgetItem.amount)
            putExtra("startDate", budgetItem.startDate)
            putExtra("endDate", budgetItem.endDate)
            putExtra("icon", budgetItem.icon)
        }
        startActivity(intent)
    }

    private fun deleteBudget(budgetItem: BudgetItem) {
        // Show a confirmation dialog before deleting
        AlertDialog.Builder(this)
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete this budget?")
            .setPositiveButton("Yes") { _, _ ->
                firestore.collection("Budget").document(budgetItem.id).delete()
                    .addOnSuccessListener {
                        // Refresh the budget list after deletion
                        fetchAndDisplayBudgets()
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Toast.makeText(this, "Error deleting budget: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
