package my.edu.tarc.studicash_0703.Transaction

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.adapter.ExpenseCategoryAdapter
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityEditExpensesCategoryBinding

class EditExpensesCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditExpensesCategoryBinding
    private lateinit var expenseCategoryAdapter: ExpenseCategoryAdapter
    private lateinit var expenseCategories: MutableList<ExpenseCategory>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditExpensesCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseCategories = mutableListOf()

        binding.editExpenseRecycleView.layoutManager = LinearLayoutManager(this)
        expenseCategoryAdapter = ExpenseCategoryAdapter(this, expenseCategories)
        binding.editExpenseRecycleView.adapter = expenseCategoryAdapter

        binding.editExpenseCategoryBackBtn.setOnClickListener {
            finish()
        }

        fetchExpenseCategoriesFromFirestore()

    }

    private fun fetchExpenseCategoriesFromFirestore() {
        db.collection("Goal")
            .get()
            .addOnSuccessListener { goalResult ->
                val goalNames = goalResult.documents.mapNotNull { it.getString("name") }

                db.collection("ExpenseCategories")
                    .get()
                    .addOnSuccessListener { result ->
                        expenseCategories.clear()
                        for (document in result) {
                            val icon = document.getLong("icon")?.toInt() ?: R.drawable.baseline_image_48
                            val name = document.getString("name") ?: ""
                            val iconUri = document.getString("iconUri") ?: ""
                            val id = document.id

                            if (!goalNames.contains(name)) {
                                expenseCategories.add(ExpenseCategory(icon, name, iconUri, id))
                            }
                        }
                        expenseCategoryAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error getting expense categories: $exception", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting goal categories: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateCategory(updatedCategory: ExpenseCategory, position: Int) {
        expenseCategories[position] = updatedCategory
        expenseCategoryAdapter.notifyItemChanged(position)
    }

    fun deleteCategory(position: Int) {
        val category = expenseCategories[position]
        showDeleteConfirmationDialog(category.id, category.name, position)
    }

    private fun showDeleteConfirmationDialog(categoryId: String, categoryName: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete the category \"$categoryName\"?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteCategoryFromFirestore(categoryId, position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteCategoryFromFirestore(categoryId: String, position: Int) {
        db.collection("ExpenseCategories").document(categoryId)
            .delete()
            .addOnSuccessListener {
                expenseCategories.removeAt(position)
                expenseCategoryAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error deleting category: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    fun showEditCategoryDialog(category: ExpenseCategory, position: Int) {
        val dialogFragment = DialogEditCategoryFragment.newInstance(category) { updatedCategory ->
            updateCategory(updatedCategory, position)
        }
        dialogFragment.show(supportFragmentManager, "EditCategoryDialog")
    }
}
