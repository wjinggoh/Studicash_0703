package my.edu.tarc.studicash_0703.Transaction

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.IconAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityAddExpensesCategoryBinding

class AddExpensesCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpensesCategoryBinding
    private var selectedIcon: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpensesCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addExpenseCategoryBackBtn.setOnClickListener {
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

        binding.editExpenseCategoryBtn.setOnClickListener {
            val intent = Intent(this, EditExpensesCategoryActivity::class.java)
            startActivity(intent)
        }

        binding.chooseIconBtn.setOnClickListener {
            showChooseIconDialog()
        }
    }

    private fun saveCategory(categoryName: String) {
        // Get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val categoryRef = db.collection("ExpenseCategories").document()  // Create a reference with auto-generated ID

        // Get the selected icon URI
        val iconUri = selectedIcon?.let { getUriFromDrawable(it) }

        val categoryData = hashMapOf(
            "uid" to userId,
            "name" to categoryName,
            "iconUri" to iconUri
        )

        categoryRef.set(categoryData)
            .addOnSuccessListener {
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

    private fun showChooseIconDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_choose_icon, null)
        val gridView: GridView = dialogView.findViewById(R.id.iconGridView)
        val iconIds = arrayOf(
            R.drawable.category_expense1,
            R.drawable.category_expense2,
            R.drawable.category_expenses3,
            R.drawable.category_food1,
            R.drawable.category_food2,
            R.drawable.category_beverage1,
            R.drawable.category_beverage2,
            R.drawable.category_beverage3,
            R.drawable.category_fees1,
            R.drawable.category_fees2,
            R.drawable.category_fees3,
            R.drawable.category_rent1,
            R.drawable.category_rent2,
            R.drawable.category_entertainment1,
            R.drawable.category_entertainment2,
            R.drawable.category_entertainment3,
            R.drawable.category_electricity1,
            R.drawable.category_water1,
            R.drawable.category_water2
        )
        val iconAdapter = IconAdapter(this, iconIds)
        gridView.adapter = iconAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose Icon")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedIcon = iconIds[position]
            // Load selected icon into ImageView
            Glide.with(this)
                .load(selectedIcon)
                .into(binding.selectedIconImageView)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getUriFromDrawable(drawableId: Int): String {
        return "android.resource://${packageName}/$drawableId"
    }
}
