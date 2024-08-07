package my.edu.tarc.studicash_0703

import GoalRecordAdapter
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import my.edu.tarc.studicash_0703.Models.GoalRecord
import my.edu.tarc.studicash_0703.databinding.ActivityGoalRecordHistoryBinding

class GoalRecordHistoryActivity : AppCompatActivity(), GoalRecordAdapter.OnGoalRecordClickListener {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityGoalRecordHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalRecordHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.goalRecordHistoryBackBtn.setOnClickListener {
            finish()
        }

        fetchGoalNames()
    }

    private fun fetchGoalNames() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showError("User is not logged in!")
            return
        }

        lifecycleScope.launch {
            try {
                val goals = firestore.collection("Goal")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()
                    .documents
                    .map { it.getString("name") ?: "" }

                if (goals.isEmpty()) {
                    showError("No goals found.")
                } else {
                    setupSpinner(goals)
                }
            } catch (e: Exception) {
                showError("Error fetching goals: ${e.message}")
            }
        }
    }

    private fun setupSpinner(goalNames: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.goal_spinner_item, goalNames)
        adapter.setDropDownViewResource(R.layout.spinner_drop_down_item)
        binding.goalRecordSpinner.adapter = adapter

        binding.goalRecordSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGoal = goalNames[position]
                fetchGoalRecords(selectedGoal)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun fetchGoalRecords(goalName: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showError("User is not logged in!")
            return
        }

        lifecycleScope.launch {
            try {
                val goalRecords = firestore.collection("GoalRecord")
                    .whereEqualTo("goal", goalName)
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                    .toObjects(GoalRecord::class.java)

                setupRecyclerView(goalRecords)
            } catch (e: Exception) {
                showError("Error fetching goal records: ${e.message}")
            }
        }
    }

    private fun setupRecyclerView(goalRecords: List<GoalRecord>) {
        binding.goalRecordRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.goalRecordRecyclerView.adapter = GoalRecordAdapter(this, goalRecords.toMutableList(), this)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDelete(goalRecordId: String, goalName: String) {
        showDeleteConfirmationDialog(goalRecordId, goalName)
    }

    private fun showDeleteConfirmationDialog(goalRecordId: String, goalName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setMessage("Are you sure you want to delete this record from the goal '$goalName'?")

        builder.setPositiveButton("Delete") { _, _ ->
            deleteGoalRecord(goalRecordId)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun deleteGoalRecord(goalRecordId: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showError("User is not logged in!")
            return
        }

        lifecycleScope.launch {
            try {
                firestore.collection("GoalRecord")
                    .document(goalRecordId)
                    .delete()
                    .await()

                // Refresh the records
                val selectedGoal = binding.goalRecordSpinner.selectedItem as String
                fetchGoalRecords(selectedGoal)

                Toast.makeText(this@GoalRecordHistoryActivity, "Record deleted successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                showError("Error deleting record: ${e.message}")
            }
        }
    }
}
