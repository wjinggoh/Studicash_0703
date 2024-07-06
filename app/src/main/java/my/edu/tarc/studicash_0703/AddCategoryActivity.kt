package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityAddCategoryBinding

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backCategory.setOnClickListener {
            startActivity(Intent(this@AddCategoryActivity, AddExpenseActivity::class.java))
        }

        binding.saveCategoryButton.setOnClickListener {
            val categoryName = binding.categoryNameEditText.text.toString()
            if (categoryName.isNotEmpty()) {
                // Save the custom category (implement the save logic here)
                saveCategory(categoryName)
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCategory(categoryName: String) {
        val db = FirebaseFirestore.getInstance()
        val categoryData = hashMapOf("name" to categoryName)
        db.collection("Categories")
            .add(categoryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding category: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
