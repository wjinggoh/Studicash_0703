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
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.GoalItem
import my.edu.tarc.studicash_0703.databinding.ActivityGoalTrackingBinding
import java.util.concurrent.TimeUnit
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import my.edu.tarc.studicash_0703.Budget.BudgetTrackingActivity
import my.edu.tarc.studicash_0703.Worker.GoalTransactionWorker

class GoalTrackingActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityGoalTrackingBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = arrayOf("Goal", "Budget")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_drop_down_item)
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

        binding.goalRecordHistory.setOnClickListener {
            val intent = Intent(this, GoalRecordHistoryActivity::class.java)
            startActivity(intent)
        }

        fetchAndDisplayGoals()

        // Schedule the worker to run daily
        val workRequest = PeriodicWorkRequestBuilder<GoalTransactionWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun fetchAndDisplayGoals() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            handleError("User is not logged in!")
            return
        }
        lifecycleScope.launch {
            try {
                val goalCollection = firestore.collection("Goal")
                val goalDocuments = goalCollection.whereEqualTo("uid", uid).get().await()

                val goalItems = goalDocuments.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val startDate = document.getString("startDate") ?: ""
                    val endDate = document.getString("endDate") ?: ""

                    val savedAmount = fetchTotalSavedForGoal(name)
                    val progress = if (amount > 0) ((savedAmount / amount) * 100).toInt() else 0

                    GoalItem(
                        id = id,
                        name = name,
                        amount = amount,
                        saved = savedAmount,
                        progress = progress,
                        startDate = startDate,
                        endDate = endDate
                    )
                }

                if (goalItems.isEmpty()) {
                    binding.goalTrackingRecycleView.visibility = View.GONE
                    binding.noGoalsMessage.visibility = View.VISIBLE
                } else {
                    binding.goalTrackingRecycleView.visibility = View.VISIBLE
                    binding.noGoalsMessage.visibility = View.GONE
                    setupRecyclerView(goalItems)
                }
            } catch (exception: Exception) {
                handleError("Error fetching goals: ${exception.message}")
            }
        }
    }

    private suspend fun fetchTotalSavedForGoal(goalName: String): Double {
        return try {
            val transactionsCollection = firestore.collection("GoalRecord") // Adjust if needed
            val querySnapshot = transactionsCollection.whereEqualTo("goal", goalName).get().await()

            querySnapshot.documents.sumOf { document ->
                document.getDouble("saveAmount") ?: 0.0
            }
        } catch (exception: Exception) {
            handleError("Error fetching total saved for goal: ${exception.message}")
            0.0
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
