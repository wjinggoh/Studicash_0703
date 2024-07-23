package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
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
        firestore.collection("ExpenseCategories")
            .get()
            .addOnSuccessListener { result ->
                val fetchedCategories = result.map { document ->
                    val iconName = document.getString("icon") ?: "default_icon"
                    val iconResId = resources.getIdentifier(iconName, "drawable", packageName)
                    ExpenseCategory(iconResId, document.getString("name") ?: "")
                }
                val predefinedCategories = getExpenseCategories()
                expenseCategories = fetchedCategories + predefinedCategories

                setupCategorySpinner(expenseCategories)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching categories: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val selectedCategory = binding.spinnerCategory.selectedItem as ExpenseCategory
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()

        if (amount != null && startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val budget = hashMapOf(
                "category" to selectedCategory.name,
                "amount" to amount,
                "startDate" to startDate,
                "endDate" to endDate,
                "icon" to selectedCategory.icon
            )

            firestore.collection("Budget")
                .add(budget)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity after saving
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving budget: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

}
