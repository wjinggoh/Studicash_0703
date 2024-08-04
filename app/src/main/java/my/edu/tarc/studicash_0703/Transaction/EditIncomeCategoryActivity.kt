package my.edu.tarc.studicash_0703.Transaction

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.IncomeCategory
import my.edu.tarc.studicash_0703.adapter.IncomeCategoryAdapter
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityEditIncomeCategoryBinding

class EditIncomeCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditIncomeCategoryBinding
    private lateinit var incomeCategoryAdapter: IncomeCategoryAdapter
    private lateinit var incomeCategories: MutableList<IncomeCategory>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditIncomeCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeCategories = mutableListOf()

        binding.editIncomeRecycleView.layoutManager = LinearLayoutManager(this)
        incomeCategoryAdapter = IncomeCategoryAdapter(this, incomeCategories)
        binding.editIncomeRecycleView.adapter = incomeCategoryAdapter

        binding.editIncomeCategoryBackBtn.setOnClickListener {
            finish()
        }

        fetchIncomeCategoriesFromFirestore()
    }

    private fun fetchIncomeCategoriesFromFirestore() {
        val currentUserUid = getCurrentUserUid() // Get the current user's UID

        db.collection("IncomeCategories")
            .whereEqualTo("uid", currentUserUid) // Filter by UID
            .get()
            .addOnSuccessListener { result ->
                incomeCategories.clear()
                for (document in result) {
                    val icon = document.getLong("icon")?.toInt() ?: R.drawable.baseline_image_48
                    val name = document.getString("name") ?: ""
                    val iconUri = document.getString("iconUri") ?: ""
                    val id = document.id
                    val uid = document.getString("uid") // Retrieve UID from Firestore document
                    incomeCategories.add(IncomeCategory(icon, name, iconUri, id))
                }
                incomeCategoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentUserUid(): String? {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        return currentUser?.uid
    }

    fun editCategory(category: IncomeCategory) {
        val dialogFragment = DialogEditIncomeCategoryFragment.newInstance(category) { updatedCategory ->
            updateCategory(updatedCategory, incomeCategories.indexOf(category))
        }
        dialogFragment.show(supportFragmentManager, "EditCategoryDialog")
    }

    fun updateCategory(updatedCategory: IncomeCategory, position: Int) {
        incomeCategories[position] = updatedCategory
        incomeCategoryAdapter.notifyItemChanged(position)
    }

    fun deleteCategory(position: Int) {
        val categoryId = incomeCategories[position].id
        db.collection("IncomeCategories").document(categoryId)
            .delete()
            .addOnSuccessListener {
                incomeCategories.removeAt(position)
                incomeCategoryAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error deleting category: $exception", Toast.LENGTH_SHORT).show()
            }
    }
}
