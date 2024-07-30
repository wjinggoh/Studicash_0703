package my.edu.tarc.studicash_0703.Budget

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.CategorySpinnerAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityEditBudgetBinding
import java.text.SimpleDateFormat
import java.util.*

class EditBudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBudgetBinding
    private val calendar = Calendar.getInstance()
    private var expenseCategories = listOf<ExpenseCategory>()
    private lateinit var firestore: FirebaseFirestore
    private var budgetId: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get the budget ID passed from the previous activity
        budgetId = intent.getStringExtra("budgetId")

        if (budgetId != null) {
            fetchBudgetData(budgetId!!)
        } else {
            Log.e("EditBudgetActivity", "Budget ID is null")
        }

        setupDatePicker(binding.editTextStartDate)
        setupDatePicker(binding.editTextEndDate)

        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }

        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }

        binding.buttonSave.setOnClickListener {
            budgetId?.let {
                saveBudget(it)
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.editBudgetBackBtn.setOnClickListener {
            finish()
        }

        fetchExpenseCategories()
    }

    private fun fetchBudgetData(budgetId: String) {
        val docRef = firestore.collection("Budget").document(budgetId)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                binding.budgetNameEditBudget.setText(document.getString("name"))
                binding.editTextAmount.setText(document.getDouble("amount")?.toString())
                binding.editTextStartDate.text = document.getString("startDate")
                binding.editTextEndDate.text = document.getString("endDate")
                // Fetch the category and set it to the spinner
                val categoryName = document.getString("category")
                if (!categoryName.isNullOrEmpty()) {
                    val categoryIndex = expenseCategories.indexOfFirst { it.name == categoryName }
                    if (categoryIndex != -1) {
                        binding.spinnerCategory.setSelection(categoryIndex)
                    }
                }
            } else {
                Log.d("EditBudgetActivity", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("EditBudgetActivity", "get failed with ", exception)
        }
    }

    private fun setupDatePicker(dateTextView: TextView) {
        dateTextView.setOnClickListener {
            showDatePickerDialog(dateTextView)
        }
    }

    private fun showDatePickerDialog(dateTextView: TextView) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateText(dateTextView)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateText(dateTextView: TextView) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateTextView.text = sdf.format(calendar.time)
    }

    private fun fetchExpenseCategories() {
        val uid = auth.currentUser?.uid ?: return // Exit if UID is null
        // Fetch Expense Categories
        firestore.collection("ExpenseCategories")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                val fetchedExpenseCategories = result.map { document ->
                    ExpenseCategory(
                        icon = R.drawable.custom_category, // Fallback icon resource
                        name = document.getString("name") ?: "",
                        iconUri = document.getString("iconUri") // Fetch icon URL
                    )
                }

                // Combine fetched categories with predefined categories (if any)
                expenseCategories = fetchedExpenseCategories + getExpenseCategories()
                setupCategorySpinner()
            }
            .addOnFailureListener { exception ->
                Log.d("EditBudgetActivity", "fetchExpenseCategories failed with ", exception)
            }
    }

    private fun setupCategorySpinner() {
        val adapter = CategorySpinnerAdapter(this, expenseCategories)
        binding.spinnerCategory.adapter = adapter

        // Optionally, set a default selection or perform other initializations here
    }

    private fun saveBudget(budgetId: String) {
        val name = binding.budgetNameEditBudget.text.toString()
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val selectedCategory = binding.spinnerCategory.selectedItem as? ExpenseCategory

        val updatedBudget = mapOf(
            "name" to name,
            "amount" to amount,
            "startDate" to startDate,
            "endDate" to endDate,
            "category" to (selectedCategory?.name ?: ""), // Ensure category name is used
            "uid" to (auth.currentUser?.uid ?: "")
        )

        if (name.isNotEmpty() && amount != null && startDate.isNotEmpty() && endDate.isNotEmpty() && selectedCategory != null) {
            firestore.collection("Budget").document(budgetId)
                .update(updatedBudget)
                .addOnSuccessListener {
                    Toast.makeText(this, "Budget updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating budget", Toast.LENGTH_SHORT).show()
                    Log.w("EditBudgetActivity", "Error updating document", e)
                }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
}
