package my.edu.tarc.studicash_0703

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.GoalItem
import my.edu.tarc.studicash_0703.databinding.ActivityEditGoalBinding

class EditGoalActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityEditGoalBinding
    private var goalId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        goalId = intent.getStringExtra("goalId")

        goalId?.let {
            fetchGoalDetails(it)
        }

        binding.saveGoalButton.setOnClickListener {
            goalId?.let {
                saveGoalDetails(it)
            }
        }
    }

    private fun fetchGoalDetails(goalId: String) {
        firestore.collection("Goal").document(goalId).get().addOnSuccessListener { document ->
            val goal = document.toObject(GoalItem::class.java)
            goal?.let {
                binding.goalNameEditText.setText(it.name)
                binding.goalAmountEditText.setText(it.amount.toString())
                binding.goalStartDateEditText.setText(it.startDate)
                binding.goalEndDateEditText.setText(it.endDate)
            }
        }.addOnFailureListener { exception ->
            handleError("Error fetching goal details: ${exception.message}")
        }
    }

    private fun saveGoalDetails(goalId: String) {
        val name = binding.goalNameEditText.text.toString()
        val amount = binding.goalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val startDate = binding.goalStartDateEditText.text.toString()
        val endDate = binding.goalEndDateEditText.text.toString()

        val updatedGoal = mapOf(
            "name" to name,
            "amount" to amount,
            "startDate" to startDate,
            "endDate" to endDate
        )

        firestore.collection("Goal").document(goalId).update(updatedGoal).addOnSuccessListener {
            finish() // Close the activity and return to the previous screen
        }.addOnFailureListener { exception ->
            handleError("Error saving goal details: ${exception.message}")
        }
    }

    private fun handleError(message: String) {
        // Handle error (e.g., show a toast or log the error)
    }
}
