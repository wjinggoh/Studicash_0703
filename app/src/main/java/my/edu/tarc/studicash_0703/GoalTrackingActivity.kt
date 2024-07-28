package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.GoalItem
import my.edu.tarc.studicash_0703.databinding.ActivityGoalTrackingBinding
import java.util.concurrent.TimeUnit

class GoalTrackingActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityGoalTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = arrayOf("Goal", "Budget")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.goalTrackingSpinner.adapter = adapter

        // Set default selection to Goal
        binding.goalTrackingSpinner.setSelection(0)

        // Set up the OnItemSelectedListener
        binding.goalTrackingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Stay on the GoalTrackingActivity
                        // No action needed as we're already on this page
                    }
                    1 -> {
                        // Switch to BudgetTrackingActivity
                        val intent = Intent(this@GoalTrackingActivity, BudgetTrackingActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: Close this activity if you don't want to return to it
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when no item is selected
            }
        }

        binding.goalTrackingBackBtn.setOnClickListener {
            finish()
        }

        binding.addGoalTrackingBtn.setOnClickListener {
            val intent = Intent(this, CreateGoalActivity::class.java)
            startActivity(intent)
        }

        fetchAndDisplayGoals()

        // Schedule the worker to run daily
        val workRequest = PeriodicWorkRequestBuilder<GoalTransactionWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun fetchAndDisplayGoals() {
        val goalCollection = firestore.collection("Goal")
        goalCollection.get().addOnSuccessListener { result ->
            val goalTasks = mutableListOf<Task<Pair<GoalItem, Double>>>()

            for (document in result) {
                val id = document.id
                val name = document.getString("name") ?: ""
                val amount = document.getDouble("amount") ?: 0.0
                val startDate = document.getString("startDate") ?: ""
                val endDate = document.getString("endDate") ?: ""

                val goalItem = GoalItem(
                    id = id,
                    name = name,
                    amount = amount,
                    saved = 0.0, // Default value
                    progress = 0, // Default value
                    startDate = startDate,
                    endDate = endDate
                )

                val task = fetchTotalSavedForGoal(name).continueWith { saved ->
                    val savedAmount = saved.result
                    val progress = if (goalItem.amount > 0) {
                        ((savedAmount / goalItem.amount) * 100).toInt()
                    } else {
                        0
                    }
                    goalItem.saved = savedAmount
                    goalItem.progress = progress
                    goalItem to savedAmount
                }
                goalTasks.add(task)
            }

            Tasks.whenAllComplete(goalTasks).addOnCompleteListener { tasks ->
                val results = tasks.result
                val updatedGoals = mutableListOf<GoalItem>()
                results.forEach { task ->
                    val (goalItem, saved) = task.result as Pair<GoalItem, Double>
                    updatedGoals.add(goalItem)
                }

                if (updatedGoals.isEmpty()) {
                    binding.goalTrackingRecycleView.visibility = View.GONE
                    binding.noGoalsMessage.visibility = View.VISIBLE
                } else {
                    binding.goalTrackingRecycleView.visibility = View.VISIBLE
                    binding.noGoalsMessage.visibility = View.GONE
                    setupRecyclerView(updatedGoals)
                }
            }.addOnFailureListener { exception ->
                handleError("Error fetching goals: ${exception.message}")
            }
        }.addOnFailureListener { exception ->
            handleError("Error fetching goals: ${exception.message}")
        }
    }

    private fun fetchTotalSavedForGoal(goalName: String): Task<Double> {
        val transactionsCollection = firestore.collection("expenseTransactions") // Adjust if needed
        val query = transactionsCollection.whereEqualTo("category", goalName)

        return query.get().continueWith { task ->
            if (task.isSuccessful) {
                val documents = task.result
                var totalSaved = 0.0
                for (document in documents) {
                    val amount = document.getDouble("amount") ?: 0.0
                    totalSaved += amount
                }
                totalSaved
            } else {
                0.0
            }
        }
    }

    private fun setupRecyclerView(goals: List<GoalItem>) {
        binding.goalTrackingRecycleView.layoutManager = LinearLayoutManager(this)
        binding.goalTrackingRecycleView.adapter = GoalAdapter(
            goals,
            onEditClick = { goal ->
                // Navigate to EditGoalActivity
                val intent = Intent(this, EditGoalActivity::class.java)
                intent.putExtra("goalId", goal.id)
                startActivity(intent)
            },
            onDeleteClick = { goal ->
                showDeleteConfirmationDialog(goal.id, goal.name)
            }
        )
    }

    private fun showDeleteConfirmationDialog(goalId: String, goalName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteGoal(goalId, goalName)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteGoal(goalId: String, goalName: String) {
        // Delete the goal from the Goal collection
        firestore.collection("Goal").document(goalId).delete().addOnSuccessListener {
            // Delete the corresponding expense category
            firestore.collection("ExpenseCategories")
                .whereEqualTo("name", goalName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                    }
                    // Refresh the goal list after deletion
                    fetchAndDisplayGoals()
                }.addOnFailureListener { exception ->
                    handleError("Error deleting expense category: ${exception.message}")
                }
        }.addOnFailureListener { exception ->
            handleError("Error deleting goal: ${exception.message}")
        }
    }

    private fun handleError(message: String) {
        // Handle error (e.g., show a toast or log the error)
    }
}
