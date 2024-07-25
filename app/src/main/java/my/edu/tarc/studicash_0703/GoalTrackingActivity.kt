package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.Models.GoalItem
import my.edu.tarc.studicash_0703.databinding.ActivityGoalTrackingBinding

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

        binding.goalTrackingBackBtn.setOnClickListener{
            finish()
        }

        binding.addGoalTrackingBtn.setOnClickListener{
            val intent = Intent(this, CreateGoalActivity::class.java)
            startActivity(intent)
        }

        fetchAndDisplayGoals()
    }

    private fun fetchAndDisplayGoals() {
        val goalCollection = firestore.collection("Goal")
        goalCollection.get().addOnSuccessListener { result ->
            Log.d("GoalTrackingActivity", "Fetched ${result.size()} goals")

            val goalTasks = mutableListOf<Task<Pair<GoalItem, Double>>>()

            if (result.isEmpty) {
                Log.d("GoalTrackingActivity", "No goals found.")
            }

            for (document in result) {
                val id = document.id
                val name = document.getString("name") ?: ""
                val amount = document.getDouble("amount") ?: 0.0
                val startDate = document.getString("startDate") ?: ""
                val endDate = document.getString("endDate") ?: ""

                // Creating GoalItem object
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
                    val saved = saved.result
                    val progress = if (goalItem.amount > 0) ((saved / goalItem.amount) * 100).toInt() else 0
                    goalItem.saved = saved
                    goalItem.progress = progress
                    goalItem to saved
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

                Log.d("GoalTrackingActivity", "Goals to display: ${updatedGoals.size}")
                if (updatedGoals.isEmpty()) {
                    binding.goalTrackingRecycleView.visibility = View.GONE
                    binding.noGoalsMessage.visibility = View.VISIBLE
                } else {
                    binding.goalTrackingRecycleView.visibility = View.VISIBLE
                    binding.noGoalsMessage.visibility = View.GONE
                    setupRecyclerView(updatedGoals)
                }
            }.addOnFailureListener { exception ->
                binding.goalTrackingRecycleView.visibility = View.GONE
                binding.noGoalsMessage.visibility = View.VISIBLE
                binding.noGoalsMessage.text = "Error fetching goals: ${exception.message}"
            }
        }.addOnFailureListener { exception ->
            binding.goalTrackingRecycleView.visibility = View.GONE
            binding.noGoalsMessage.visibility = View.VISIBLE
            binding.noGoalsMessage.text = "Error fetching goals: ${exception.message}"
        }
    }

    private fun fetchTotalSavedForGoal(goalName: String): Task<Double> {
        val transactionsCollection = firestore.collection("expenseTransactions") // Adjust if your collection name is different
        val query = transactionsCollection.whereEqualTo("category", goalName) // Adjust field name as necessary

        return query.get().continueWith { task ->
            if (task.isSuccessful) {
                val documents = task.result
                var totalSaved = 0.0
                for (document in documents) {
                    val amount = document.getDouble("amount") ?: 0.0 // Adjust field name as necessary
                    totalSaved += amount
                }
                totalSaved
            } else {
                throw task.exception ?: Exception("Error fetching total saved amount")
            }
        }
    }


    private fun setupRecyclerView(goals: List<GoalItem>) {
        val adapter = GoalAdapter(goals,
            onEditClick = { goal ->
                // Handle edit goal
                val intent = Intent(this, EditGoalActivity::class.java)
                intent.putExtra("goalId", goal.id)
                startActivity(intent)
            },
            onDeleteClick = { goal ->
                // Handle delete goal
                deleteGoal(goal)
            }
        )
        binding.goalTrackingRecycleView.adapter = adapter
        binding.goalTrackingRecycleView.layoutManager = LinearLayoutManager(this)
    }

    private fun deleteGoal(goalItem: GoalItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Yes") { _, _ ->
                firestore.collection("Goal").document(goalItem.id).delete()
                    .addOnSuccessListener {
                        fetchAndDisplayGoals()
                    }
                    .addOnFailureListener { e ->
                        handleError("Error deleting goal: ${e.message}")
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }



    private fun handleError(message: String) {
        binding.noGoalsMessage.apply {
            visibility = View.VISIBLE
            text = message
        }
        binding.goalTrackingRecycleView.visibility = View.GONE
    }

}
