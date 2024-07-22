package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.adapter.CategorySpinnerAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityCreateBudgetBinding
import java.text.SimpleDateFormat
import java.util.*

class CreateBudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBudgetBinding
    private val calendar = Calendar.getInstance()
    private var expenseCategories = listOf<ExpenseCategory>()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Fetch categories from Firestore
        fetchCategories()

        // Set click listeners for date buttons
        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }

        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }

        // Set click listeners for save and cancel buttons
        binding.buttonSave.setOnClickListener {
            saveBudget()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        // Back button click listener
        binding.imageView10.setOnClickListener {
            finish()
        }
    }

    private fun showDatePickerDialog(editText: TextView) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateDateInView(editText)
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView(editText: TextView) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        editText.text = sdf.format(calendar.time)
    }

    private fun saveBudget() {
        val category = binding.spinnerCategory.selectedItem.toString()
        val amount = binding.editTextAmount.text.toString()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()

        if (category.isEmpty() || amount.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Implement save budget logic here (e.g., save to Firestore)
        val budget = hashMapOf(
            "category" to category,
            "amount" to amount.toDouble(),
            "startDate" to startDate,
            "endDate" to endDate
        )

        firestore.collection("budgets")
            .add(budget)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving the budget
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCategories() {
        firestore.collection("ExpenseCategories")
            .get()
            .addOnSuccessListener { result ->
                expenseCategories = result.map { document ->
                    ExpenseCategory(R.drawable.custom_category, document.getString("name") ?: "")
                } + getExpenseCategories() // Combine with predefined categories

                setupCategorySpinner(expenseCategories)
            }
    }

    private fun getExpenseCategories(): List<ExpenseCategory> {
        return listOf(
            ExpenseCategory(R.drawable.food, "Food"),
            ExpenseCategory(R.drawable.fees, "Fees"),
            ExpenseCategory(R.drawable.shopping, "Shopping"),
            ExpenseCategory(R.drawable.transport, "Transportation"),
            ExpenseCategory(R.drawable.custom_category, "Other")
        )
    }

    private fun setupCategorySpinner(categories: List<ExpenseCategory>) {
        val adapter = CategorySpinnerAdapter(this, categories)
        binding.spinnerCategory.adapter = adapter
    }
}
