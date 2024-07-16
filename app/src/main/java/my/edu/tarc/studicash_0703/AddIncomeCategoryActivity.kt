package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityAddIncomeCategoryBinding

class AddIncomeCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backCategory.setOnClickListener {
            onBackPressed()
        }

        binding.saveIncomeCategoryBtn.setOnClickListener {
            val categoryName = binding.incomeCategoryNameEditText.text.toString()
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
        db.collection("IncomeCategories")
            .add(categoryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Income category added successfully", Toast.LENGTH_SHORT).show()
                fetchUpdatedCategories()  // Fetch updated categories
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding income category: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUpdatedCategories() {
        val db = FirebaseFirestore.getInstance()
        db.collection("IncomeCategories")
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

