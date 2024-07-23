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

        binding = ActivityBudgetTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupButtons()

        fetchAndDisplayBudgets()
    }

    private fun setupSpinner() {
        val items = arrayOf("Budget", "Goal")

        // Create an ArrayAdapter using the custom layout
        val adapter = ArrayAdapter(this, R.layout.spinner_drop_down_item, items)

        // Set the layout for the dropdown items
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
                val name=document.getString("name")?: ""
                val category = document.getString("category") ?: ""
                val amount = document.getDouble("amount") ?: 0.0
                val startDate = document.getString("startDate") ?: ""
                val endDate = document.getString("endDate") ?: ""
                val icon = document.getLong("icon")?.toInt() ?: 0

                // Creating BudgetItem object
                val budgetItem = BudgetItem(
                    id = id,
                    name=name,
                    category = category,
                    amount = amount,
                    spent = 0.0, // Default value
                    progress = 0, // Default value
                    startDate = startDate,
                    endDate = endDate,
                    icon = icon
                )

                val task = fetchTotalSpentForBudget(category).continueWith { totalSpent ->
                    val spent = totalSpent.result
                    val progress = if (budgetItem.amount > 0) ((spent / budgetItem.amount) * 100).toInt() else 0
                    budgetItem.spent = spent
                    budgetItem.progress = progress
                    budgetItem to spent
                }
                budgetTasks.add(task)
            }

            Tasks.whenAllComplete(budgetTasks).addOnCompleteListener { tasks ->
                val results = tasks.result
                val updatedBudgets = mutableListOf<BudgetItem>()
                results.forEach { task ->
                    val (budgetItem, spent) = task.result as Pair<BudgetItem, Double>
                    updatedBudgets.add(budgetItem)
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
            onEditClick = { budgetItem -> editBudget(budgetItem) },
            onDeleteClick = { budgetItem -> deleteBudget(budgetItem) }
        )
        binding.budgetTrackingRecycleView.adapter = adapter
        binding.budgetTrackingRecycleView.layoutManager = LinearLayoutManager(this)
    }

    private fun editBudget(budgetItem: BudgetItem) {
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
        AlertDialog.Builder(this)
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete this budget?")
            .setPositiveButton("Yes") { _, _ ->
                firestore.collection("Budget").document(budgetItem.id).delete()
                    .addOnSuccessListener {
                        fetchAndDisplayBudgets()
                    }
                    .addOnFailureListener { e ->
                        handleError("Error deleting budget: ${e.message}")
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        // Implement a loading indicator (e.g., ProgressBar) if needed
        // Example: binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleError(message: String) {
        binding.noBudgetsMessage.apply {
            visibility = View.VISIBLE
            text = message
        }
        binding.budgetTrackingRecycleView.visibility = View.GONE
    }
}