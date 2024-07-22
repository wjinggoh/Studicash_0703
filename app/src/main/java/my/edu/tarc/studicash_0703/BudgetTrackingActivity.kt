package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
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
            val intent = Intent(this, AddBudgetActivity::class.java)
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
            val budgets = mutableListOf<BudgetItem>()
            val budgetTasks = mutableListOf<Task<Pair<String, Double>>>()

            for (document in result) {
                val category = document.getString("category") ?: ""
                val amount = document.getDouble("amount") ?: 0.0
                val task = fetchTotalSpentForBudget(category).continueWith { totalSpent ->
                    category to totalSpent.result
                }
                budgetTasks.add(task)
            }

            Tasks.whenAllComplete(budgetTasks).addOnCompleteListener { tasks ->
                val results = tasks.result
                results.forEach { task ->
                    val (name, spent) = task.result as Pair<String, Double>
                    val amount = result.documents.find { it.getString("category") == name }?.getDouble("amount") ?: 0.0
                    val progress = if (amount > 0) ((spent / amount) * 100).toInt() else 0
                    budgets.add(BudgetItem(name, amount, spent, progress))
                }

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
                taskCompletionSource.setResult(totalSpent)
            }
            .addOnFailureListener { exception ->
                Log.e("BudgetTrackingActivity", "Error fetching transactions: ${exception.message}")
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }

    private fun setupRecyclerView(budgets: List<BudgetItem>) {
        val adapter = BudgetAdapter(budgets)
        binding.budgetTrackingRecycleView.layoutManager = LinearLayoutManager(this)
        binding.budgetTrackingRecycleView.adapter = adapter
    }
}

