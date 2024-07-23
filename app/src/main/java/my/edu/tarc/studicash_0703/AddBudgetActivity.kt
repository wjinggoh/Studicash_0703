package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityAddBudgetBinding
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddBudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBudgetBinding
    private lateinit var expenseCategories: List<ExpenseCategory> // List to hold expense categories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.budgetBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.selectStartDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.startDateView.text = selectedDate
            }
        }

        binding.selectEndDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.endDateView.text = selectedDate
            }
        }

        fetchExpenseCategories() // Fetch expense categories from Firestore

        binding.saveBudgetButton.setOnClickListener {
            val budgetAmount = binding.budgetAmountEditText.text.toString()
            val selectedCategory = binding.budgetCategorySpinner.selectedItem.toString()
            val startDate = binding.startDateView.text.toString()
            val endDate = binding.endDateView.text.toString()

            if (validateInput(budgetAmount, startDate, endDate) &&
                isCategoryValid(selectedCategory)) {
                saveBudgetToFirestore(selectedCategory, budgetAmount.toDouble(), startDate, endDate)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time
            )
            onDateSelected(formattedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun fetchExpenseCategories() {
        val db = FirebaseFirestore.getInstance()

        db.collection("expense_categories").get()
            .addOnSuccessListener { documents ->
                expenseCategories = documents.mapNotNull { doc ->
                    ExpenseCategory(
                        name = doc.getString("name") ?: "",
                        icon = (doc.getLong("icon")?.toInt() ?: 0) // Retrieve and cast the icon field to Int
                    )
                }
                setupCategorySpinner()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading expense categories", Toast.LENGTH_SHORT).show()
                // Handle failure if needed
            }
    }

    private fun setupCategorySpinner() {
        val categories = expenseCategories.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.budgetCategorySpinner.adapter = adapter
    }

    private fun validateInput(amount: String, startDate: String, endDate: String): Boolean {
        if (amount.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isCategoryValid(selectedCategory: String): Boolean {
        return expenseCategories.any { it.name == selectedCategory }
    }

    private fun saveBudgetToFirestore(category: String, amount: Double, startDate: String, endDate: String) {
        val db = FirebaseFirestore.getInstance()

        val budget = hashMapOf(
            "category" to category,
            "amount" to amount,
            "startDate" to startDate,
            "endDate" to endDate
        )

        db.collection("budgets")
            .add(budget)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
