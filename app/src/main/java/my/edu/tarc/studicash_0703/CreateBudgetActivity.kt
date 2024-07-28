package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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

        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }

        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }

        binding.buttonSave.setOnClickListener {
            saveBudget()
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

    private fun setupCategorySpinner(categories: List<ExpenseCategory>) {
        val adapter = CategorySpinnerAdapter(this, categories)
        binding.spinnerCategory.adapter = adapter
    }

    private fun fetchCategories() {
        // First, fetch the goal collection documents to get their names
        firestore.collection("Goal")
            .get()
            .addOnSuccessListener { goalResult ->
                val goalNames = goalResult.documents.mapNotNull { it.getString("name") }

                Log.d("CreateBudgetActivity", "Goal Names: $goalNames") // Debugging line

                // Then, fetch the ExpenseCategories and filter out those matching goal names
                firestore.collection("ExpenseCategories")
                    .get()
                    .addOnSuccessListener { result ->
                        val fetchedCategories = result.mapNotNull { document ->
                            val iconField = document.get("icon")
                            val iconResId = when (iconField) {
                                is String -> {
                                    // Handle string type
                                    resources.getIdentifier(iconField, "drawable", packageName)
                                }
                                is Long -> {
                                    // Handle long type (assuming these are valid resource IDs)
                                    iconField.toInt()
                                }
                                else -> {
                                    // Log the unexpected type for debugging purposes
                                    Log.e("CreateBudgetActivity", "Unexpected type for 'icon': ${iconField?.javaClass?.name}")
                                    R.drawable.money // Use a default icon resource
                                }
                            }
                            val name = document.getString("name") ?: ""
                            if (goalNames.contains(name)) {
                                // Log the excluded category for debugging
                                Log.d("CreateBudgetActivity", "Excluding category: $name")
                                // Return null if the category name matches any goal name
                                null
                            } else {
                                ExpenseCategory(iconResId, name)
                            }
                        }.filterNotNull()

                        val predefinedCategories = getExpenseCategories()
                        expenseCategories = predefinedCategories + fetchedCategories

                        Log.d("CreateBudgetActivity", "Expense Categories: $expenseCategories") // Debugging line

                        setupCategorySpinner(expenseCategories)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching categories: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching goal names: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun getExpenseCategories(): List<ExpenseCategory> {
        // Return your predefined categories here
        return listOf(
            ExpenseCategory(R.drawable.food, "Food"),
            ExpenseCategory(R.drawable.fees, "Fees"),
            ExpenseCategory(R.drawable.shopping, "Shopping"),
            ExpenseCategory(R.drawable.transport, "Transportation"),
            ExpenseCategory(R.drawable.custom_category, "Other")
        )
    }

    private fun saveBudget() {
        val name = binding.budgetNameCreateBudget.text.toString()
        val selectedCategory = binding.spinnerCategory.selectedItem as ExpenseCategory
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()

        if (amount != null && startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val budgetData = mapOf(
                "name" to name,
                "category" to selectedCategory.name,
                "amount" to amount,
                "startDate" to startDate,
                "endDate" to endDate
            )

            firestore.collection("Budget")
                .add(budgetData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving budget: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }
}
