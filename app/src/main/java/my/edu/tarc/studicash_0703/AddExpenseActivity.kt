package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.CategoryItem
import my.edu.tarc.studicash_0703.Adapter.CategorySpinnerAdapter
import my.edu.tarc.studicash_0703.Fragment.AddFragment
import my.edu.tarc.studicash_0703.databinding.ActivityAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private val categories = mutableListOf<CategoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addCategoryButton.setOnClickListener {
            startActivity(Intent(this, AddExpensesCategoryActivity::class.java))
        }

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        categories.clear()

        // Predefined categories
        categories.addAll(listOf(
            CategoryItem(R.drawable.food, "Food"),
            CategoryItem(R.drawable.transport, "Transport"),
            CategoryItem(R.drawable.shopping, "Shopping"),
            CategoryItem(R.drawable.house, "Rental"),
            CategoryItem(R.drawable.fees, "Fees"),
            CategoryItem(R.drawable.travel, "Travel")
        ))

        // Load custom categories from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("Categories")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: continue
                    categories.add(CategoryItem(R.drawable.custom_category, name))
                }
                val adapter = CategorySpinnerAdapter(this, categories)
                binding.categoriesSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading categories: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupViews() {
        binding.back.setOnClickListener {
            startActivity(Intent(this@AddExpenseActivity, AddFragment::class.java))
            finish()
        }
        binding.selectDate.setOnClickListener {
            showDatePicker()
        }

        binding.SaveExpense.setOnClickListener {
            val expenseTitle = binding.yourexpenseTitle.editText?.text.toString()
            val expenseAmount = binding.yourexpenseAmt.editText?.text.toString()
            val selectedDate = binding.dateView.text.toString()
            val selectedCategory = binding.categoriesSpinner.selectedItem.toString()
            val paymentMethod = binding.paymentMethods.selectedItem.toString()

            if (expenseTitle.isEmpty()) {
                Toast.makeText(this@AddExpenseActivity, "Please enter an expense title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val expenseData = hashMapOf(
                "expenseTitle" to expenseTitle,
                "expenseAmount" to expenseAmount,
                "date" to selectedDate,
                "category" to selectedCategory,
                "paymentMethod" to paymentMethod
            )

            db.collection("Expense")
                .add(expenseData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this@AddExpenseActivity, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddExpenseActivity, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@AddExpenseActivity, "Error saving expense: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { timestamp ->
            val selectedDate = Date(timestamp)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(selectedDate)
            binding.dateView.text = formattedDate
        }

        picker.show(supportFragmentManager, picker.toString())
    }
}
