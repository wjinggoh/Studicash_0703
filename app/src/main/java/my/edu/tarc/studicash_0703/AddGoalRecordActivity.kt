package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import my.edu.tarc.studicash_0703.databinding.ActivityAddGoalRecordBinding
import java.util.Calendar

class AddGoalRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddGoalRecordBinding
    private val calendar = Calendar.getInstance()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var goalList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set up back button click listener
        binding.goalRecordBackBtn.setOnClickListener {
            finish() // Go back to the previous screen
        }

        fetchGoals()

        binding.saveGoalRecordBtn.setOnClickListener {
            saveGoalRecord()
        }

        binding.goalRecordDateSelectBtn.setOnClickListener {
            showDatePicker()
        }
    }

    private fun fetchGoals() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("Goal")
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener { documents ->
                    goalList = mutableListOf()
                    for (document in documents) {
                        val goalName = document.getString("name")
                        goalName?.let { goalList.add(it) }
                    }
                    setupSpinner(goalList)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(this, "Error fetching goals", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Initialize DatePickerDialog with current date
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDayOfMonth"
                binding.saveDateGoalRecord.text = selectedDate
                // Update the calendar with the selected date
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setupSpinner(goalNames: List<String>) {
        // Ensure R.layout.spinner_items contains a TextView with the ID textView
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.goalSpinner.adapter = adapter

        binding.goalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGoal = goalNames[position]
                fetchGoalDetails(selectedGoal)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun fetchGoalDetails(goalName: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("Goal")
                .whereEqualTo("name", goalName)
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val goalDocument = documents.firstOrNull()
                        val amountToBeSavedPerPeriod = goalDocument?.getDouble("amountToBeSavedPerPeriod")

                        if (amountToBeSavedPerPeriod != null) {
                            binding.amtToSavePerFrequency.setText(amountToBeSavedPerPeriod.toString())
                        } else {
                            Toast.makeText(this, "Error fetching amount to save per period", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Selected goal not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddGoalRecordActivity", "Error fetching goal data", e)
                    Toast.makeText(this, "Error fetching goal data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGoalRecord() {
        val userId = auth.currentUser?.uid
        val selectedGoal = binding.goalSpinner.selectedItem.toString()
        val saveDate = Timestamp(calendar.time)  // Use the updated calendar instance

        Log.d("AddGoalRecordActivity", "User ID: $userId")
        Log.d("AddGoalRecordActivity", "Selected Goal: $selectedGoal")
        Log.d("AddGoalRecordActivity", "Save Date: $saveDate")

        if (userId != null) {
            firestore.collection("Goal")
                .whereEqualTo("name", selectedGoal)
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d("AddGoalRecordActivity", "Documents fetched: ${documents.size()}")

                    if (documents.isEmpty) {
                        Log.d("AddGoalRecordActivity", "No documents found for selected goal")
                        Toast.makeText(this, "Selected goal not found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val goalDocument = documents.firstOrNull()
                    val amountToBeSavedPerPeriod = goalDocument?.getDouble("amountToBeSavedPerPeriod")

                    Log.d("AddGoalRecordActivity", "Fetched amount to be saved per period: $amountToBeSavedPerPeriod")

                    if (amountToBeSavedPerPeriod != null) {
                        binding.amtToSavePerFrequency.setText(amountToBeSavedPerPeriod.toString())
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val formattedDate = dateFormat.format(calendar.time)
                        binding.saveDateGoalRecord.text = formattedDate

                        // Create the GoalRecord with the provided data
                        val goalRecord = hashMapOf(
                            "userId" to userId,
                            "goal" to selectedGoal,
                            "saveAmount" to amountToBeSavedPerPeriod,
                            "saveDate" to saveDate
                        )

                        // Add the GoalRecord to Firestore
                        firestore.collection("GoalRecord")
                            .add(goalRecord)
                            .addOnSuccessListener { documentRef ->
                                // Update the document to include the goalRecordId
                                firestore.collection("GoalRecord")
                                    .document(documentRef.id)
                                    .update("goalRecordId", documentRef.id)
                                    .addOnSuccessListener {
                                        Log.d("AddGoalRecordActivity", "Goal record saved successfully with ID: ${documentRef.id}")
                                        Toast.makeText(this, "Goal record saved successfully", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("AddGoalRecordActivity", "Error updating goal record with ID", e)
                                        Toast.makeText(this, "Error updating goal record with ID", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("AddGoalRecordActivity", "Error saving goal record", e)
                                Toast.makeText(this, "Error saving goal record", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.d("AddGoalRecordActivity", "Amount to save per period is null")
                        Toast.makeText(this, "Error fetching amount to save per period", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddGoalRecordActivity", "Error fetching goal data", e)
                    Toast.makeText(this, "Error fetching goal data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("AddGoalRecordActivity", "User not authenticated")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

}
