package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.databinding.ActivityCreateGoalBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGoalBinding
    private val calendar = Calendar.getInstance()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }

        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }

        binding.buttonSave.setOnClickListener {
            saveGoal()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.createBudgetBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun showDatePickerDialog(dateTextView: TextView) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateTextView(dateTextView)
        }

        DatePickerDialog(
            this, dateSetListener,
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTextView(dateTextView: TextView) {
        val format = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale.US)
        dateTextView.text = sdf.format(calendar.time)
    }

    private fun saveGoal() {
        val name = binding.GoalName.text.toString()
        val amount = binding.editGoalTextAmount.text.toString().toDoubleOrNull()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val goalIconResId = R.drawable.goal

        if (amount != null && startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val goalData = mapOf(
                "name" to name,
                "amount" to amount,
                "startDate" to startDate,
                "endDate" to endDate
            )

            firestore.collection("Goal")
                .add(goalData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                    addExpenseCategory(name, goalIconResId)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving goal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExpenseCategory(name: String, iconResId: Int) {
        val expenseCategory = ExpenseCategory(
            name = name,
            icon = iconResId // Storing resource ID as integer
        )

        firestore.collection("ExpenseCategories")
            .add(expenseCategory.toMap())
            .addOnSuccessListener {
                Toast.makeText(this, "Expense Category saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving Expense Category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
