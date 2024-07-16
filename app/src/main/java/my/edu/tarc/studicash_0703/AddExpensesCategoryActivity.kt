package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Fragment.HomeFragment.Companion.TAG
import my.edu.tarc.studicash_0703.databinding.ActivityAddExpensesCategoryBinding

class AddExpensesCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpensesCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpensesCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backCategory.setOnClickListener {
            onBackPressed()
        }

        binding.saveExpenseCategoryBtn.setOnClickListener {
            val categoryName = binding.ExpenseCategoryNameEditText.text.toString()
            if (categoryName.isNotEmpty()) {
                saveCategory(categoryName)
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCategory(categoryName: String) {
        val db = FirebaseFirestore.getInstance()
        val categoryData = hashMapOf("name" to categoryName)
        db.collection("ExpenseCategories")
            .add(categoryData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Expense category added successfully", Toast.LENGTH_SHORT).show()
                fetchUpdatedCategories()  // Fetch updated categories
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding expense category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUpdatedCategories() {
        val db = FirebaseFirestore.getInstance()
        db.collection("ExpenseCategories")
            .get()
            .addOnSuccessListener { result ->
                val updatedCategories = result.map { document ->
                    document.getString("name") ?: ""
                }
                // Update spinner adapter here if applicable
                // e.g., myActivity.setupCategorySpinner(updatedCategories)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

