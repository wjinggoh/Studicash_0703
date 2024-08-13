package my.edu.tarc.studicash_0703

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.databinding.ActivityCreateGoalBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGoalBinding
    private val calendar = Calendar.getInstance()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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

        fetchMonthlyIncome()

        binding.editGoalTextAmount.addTextChangedListener(textWatcher)
        binding.editTextStartDate.addTextChangedListener(textWatcher)
        binding.editTextEndDate.addTextChangedListener(textWatcher)
        binding.spinnerSavingFrequency.onItemSelectedListener = frequencySelectedListener
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateAverageSavings()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val frequencySelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            updateAverageSavings()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun updateAverageSavings() {
        val amount = binding.editGoalTextAmount.text.toString().toDoubleOrNull()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val frequency = binding.spinnerSavingFrequency.selectedItem.toString()

        if (amount != null && startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val averageSavingsPerPeriod = calculateAverageSavingsPerFrequency(amount, startDate, endDate, frequency)
            binding.averageAmtperPeriodView.text = String.format("%.2f", averageSavingsPerPeriod)
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

        val uid = auth.currentUser?.uid

        if (uid != null) {
            firestore.collection("incomeTransactions")
                .whereEqualTo("userId", uid)
                .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
                .whereLessThanOrEqualTo("timestamp", endOfMonth)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // No income records found for the user
                        binding.textViewMonthlyIncome.text = "No income recorded for this month"
                    } else {
                        val totalIncome = calculateTotalIncome(documents)
                        binding.textViewMonthlyIncome.text = totalIncome.toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching income: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun scheduleGoalNotification(goalName: String, frequency: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, GoalNotificationsReceiver::class.java).apply {
            putExtra("goalName", goalName)
            putExtra("notificationId", goalName.hashCode()) // Unique ID for each goal
        }
        val pendingIntent = PendingIntent.getBroadcast(this, goalName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val interval = when (frequency) {
            "Weekly" -> AlarmManager.INTERVAL_DAY * 7
            "Bi-weekly" -> AlarmManager.INTERVAL_DAY * 14
            "Monthly" -> AlarmManager.INTERVAL_DAY * 30
            "Quarterly" -> AlarmManager.INTERVAL_DAY * 90
            else -> AlarmManager.INTERVAL_DAY * 30
        }

        val triggerAtMillis = System.currentTimeMillis() + interval

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            interval,
            pendingIntent
        )
    }

    private fun saveGoal() {
        val name = binding.GoalName.text.toString()
        val amount = binding.editGoalTextAmount.text.toString().toDoubleOrNull()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val monthlyIncome = binding.textViewMonthlyIncome.text.toString().toDoubleOrNull()
        val savingFrequency = binding.spinnerSavingFrequency.selectedItem.toString()
        val uid = auth.currentUser?.uid

        if (amount != null && startDate.isNotEmpty() && endDate.isNotEmpty() && monthlyIncome != null && uid != null) {
            val averageSavingsPerPeriod = calculateAverageSavingsPerFrequency(amount, startDate, endDate, savingFrequency)
            val formattedAverageSavingsPerPeriod = String.format("%.2f", averageSavingsPerPeriod).toDouble()
            binding.averageAmtperPeriodView.text = String.format("%.2f", averageSavingsPerPeriod)

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
                "savingFrequency" to savingFrequency,
                "amountToBeSavedPerPeriod" to formattedAverageSavingsPerPeriod,
                "savedAmount" to 0.0,
                "uid" to uid
            )

            firestore.collection("Goal")
                .add(goalData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                    scheduleGoalNotification(name, savingFrequency)
                    finish()
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
            totalIncome += amount
        }
        return totalIncome
    }

    private fun calculateSavings(amount: Double, startDate: String, endDate: String, frequency: String): Double {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val start = format.parse(startDate)
        val end = format.parse(endDate)

        val diffInMillis = end.time - start.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        return when (frequency) {
            "Weekly" -> amount / (diffInDays / 7.0)
            "Bi-weekly" -> amount / (diffInDays / 14.0)
            "Monthly" -> amount / (diffInDays / 30.0)
            "Quarterly" -> amount / (diffInDays / 90.0)
            else -> amount / (diffInDays / 30.0)
        }
    }

    private fun calculateAverageSavingsPerFrequency(amount: Double, startDate: String, endDate: String, frequency: String): Double {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val start = format.parse(startDate)
        val end = format.parse(endDate)

        val diffInMillis = end.time - start.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        return when (frequency) {
            "Weekly" -> amount / (diffInDays / 7.0)
            "Bi-weekly" -> amount / (diffInDays / 14.0)
            "Monthly" -> amount / (diffInDays / 30.0)
            "Quarterly" -> amount / (diffInDays / 90.0)
            else -> amount / (diffInDays / 30.0)
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


}