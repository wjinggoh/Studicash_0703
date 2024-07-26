package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.databinding.ActivityCreateGoalBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

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

        val frequencyOptions = arrayOf("Weekly", "Bi-weekly", "Monthly", "Quarterly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSavingFrequency.adapter = adapter

        binding.buttonSave.setOnClickListener {
            saveGoal()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.createGoalBackBtn.setOnClickListener {
            finish()
        }

        // Fetch and set the monthly income
        fetchMonthlyIncome()
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

    private fun fetchMonthlyIncome() {
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        firestore.collection("incomeTransactions")
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThanOrEqualTo("timestamp", endOfMonth)
            .get()
            .addOnSuccessListener { documents ->
                val totalIncome = calculateTotalIncome(documents)
                binding.textViewMonthlyIncome.text = totalIncome.toString()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching income: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun saveGoal() {
        val name = binding.GoalName.text.toString()
        val amount = binding.editGoalTextAmount.text.toString().toDoubleOrNull()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val monthlyIncome = binding.textViewMonthlyIncome.text.toString().toDoubleOrNull()
        val goalIconResId = R.drawable.goal
        val savingFrequency = binding.spinnerSavingFrequency.selectedItem.toString()

        if (amount != null && startDate.isNotEmpty() && endDate.isNotEmpty() && monthlyIncome != null) {
            val savingsNeeded = calculateSavings(amount, startDate, endDate, savingFrequency)

            if (savingsNeeded > 0 && savingsNeeded * getFrequencyMultiplier(savingFrequency) > monthlyIncome * 0.5) {
                Toast.makeText(this, "Savings amount exceeds 50% of your monthly income. Please adjust the goal amount or the period.", Toast.LENGTH_LONG).show()
                return
            }

            val goalData = mapOf(
                "name" to name,
                "amount" to amount,
                "startDate" to startDate,
                "endDate" to endDate,
                "monthlyIncome" to monthlyIncome,
                "savingFrequency" to savingFrequency
            )

            firestore.collection("Goal")
                .add(goalData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                    addExpenseCategory(name, goalIconResId)
                    createAutomatedTransactions(name, amount, startDate, endDate, savingFrequency, savingsNeeded)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving goal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTotalIncome(documents: QuerySnapshot): Double {
        var totalIncome = 0.0
        for (document in documents) {
            val amount = document.getDouble("amount") ?: 0.0
            Log.d("CalculateTotalIncome", "Amount: $amount")
            totalIncome += amount
        }
        return totalIncome
    }

    private fun calculateSavings(goalAmount: Double, startDate: String, endDate: String, frequency: String): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val start = sdf.parse(startDate)
        val end = sdf.parse(endDate)

        val weeks = ((end.time - start.time) / (1000 * 60 * 60 * 24 * 7)).toInt()
        val months = weeks / 4

        return when (frequency) {
            "Weekly" -> goalAmount / weeks
            "Bi-weekly" -> goalAmount / (weeks / 2)
            "Monthly" -> goalAmount / months
            "Quarterly" -> goalAmount / (months / 3)
            else -> 0.0
        }
    }

    private fun getFrequencyMultiplier(frequency: String): Int {
        return when (frequency) {
            "Weekly" -> 4
            "Bi-weekly" -> 2
            "Monthly" -> 1
            "Quarterly" -> 1 / 3
            else -> 1
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

    private fun createAutomatedTransactions(name: String, goalAmount: Double, startDate: String, endDate: String, frequency: String, savingsNeeded: Double) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val start = sdf.parse(startDate)
        val end = sdf.parse(endDate)
        val transactions = mutableListOf<Map<String, Any>>()

        var current = start
        while (current.before(end) || current.equals(end)) {
            val transaction = mapOf(
                "category" to name,
                "amount" to savingsNeeded,
                "date" to sdf.format(current),
                "type" to "Savings"
            )
            transactions.add(transaction)
            current = getNextDate(current, frequency)
        }

        for (transaction in transactions) {
            firestore.collection("expenseTransactions")
                .add(transaction)
                .addOnSuccessListener {
                    // Successfully added
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getNextDate(currentDate: java.util.Date, frequency: String): java.util.Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        when (frequency) {
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "Bi-weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            "Monthly" -> calendar.add(Calendar.MONTH, 1)
            "Quarterly" -> calendar.add(Calendar.MONTH, 3)
        }
        return calendar.time
    }
}
