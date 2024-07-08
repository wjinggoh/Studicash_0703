package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Adapter.CategorySpinnerAdapter
import my.edu.tarc.studicash_0703.Models.CategoryItem
import my.edu.tarc.studicash_0703.Models.Income
import my.edu.tarc.studicash_0703.Models.PaymentMethod
import my.edu.tarc.studicash_0703.databinding.ActivityAddIncomeBinding
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    private val categories = mutableListOf<CategoryItem>()
    private val paymentMethods = mutableListOf<PaymentMethod>()
    private val userId: String? by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addCategoryButton.setOnClickListener {
            startActivity(Intent(this, AddIncomeCategoryActivity::class.java))
        }

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
        loadPaymentMethods()
    }

    private fun loadCategories() {
        categories.clear()

        // Predefined categories for income
        categories.addAll(listOf(
            CategoryItem(R.drawable.salary, "Salary"),
            CategoryItem(R.drawable.freelancer, "Freelance"),
            CategoryItem(R.drawable.investment, "Investment"),
            CategoryItem(R.drawable.allowance, "Allowance")
        ))

        // Load custom categories from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("IncomeCategories")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: continue
                    categories.add(CategoryItem(R.drawable.custom_category, name))
                }
                val adapter = CategorySpinnerAdapter(this, categories)
                binding.incomeCategoriesSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPaymentMethods() {
        paymentMethods.clear()

        val db = FirebaseFirestore.getInstance()
        db.collection("paymentMethods")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val type = document.getString("type") ?: continue
                    val details = document.getString("details") ?: ""
                    paymentMethods.add(PaymentMethod(type, details))
                }
                Log.d("AddIncomeActivity", "Payment Methods: $paymentMethods") // Log fetched data

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods.map { it.details })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.paymentMethodSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading payment methods: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupViews() {
        binding.backIncome.setOnClickListener {
            finish() // Go back to the previous activity
        }
        binding.selectDate.setOnClickListener {
            showDatePicker()
        }

        binding.saveIncome.setOnClickListener {
            val incomeTitle = binding.yourIncomeTitle.editText?.text.toString()
            val incomeAmount = binding.yourIncomeAmt.editText?.text.toString().toDoubleOrNull()
            val selectedDate = binding.incomeDateView.text.toString()
            val selectedCategory = binding.incomeCategoriesSpinner.selectedItem as CategoryItem
            val paymentMethod = paymentMethods.getOrNull(binding.paymentMethodSpinner.selectedItemPosition)

            if (incomeTitle.isEmpty() || incomeAmount == null) {
                Toast.makeText(this@AddIncomeActivity, "Please enter valid income details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this@AddIncomeActivity, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentMethod == null) {
                Toast.makeText(this@AddIncomeActivity, "Please select a valid payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val incomeData = Income(
                incomeTitle = incomeTitle,
                incomeAmount = incomeAmount,
                date = selectedDate,
                category = selectedCategory.name,
                paymentMethod = paymentMethod.details,
                userId = userId
            )

            db.collection("Income")
                .add(incomeData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this@AddIncomeActivity, "Income saved successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddIncomeActivity, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@AddIncomeActivity, "Error saving income: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { timestamp ->
            val selectedDate = Date(timestamp)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = sdf.format(selectedDate)
            binding.incomeDateView.text = formattedDate
        }

        picker.show(supportFragmentManager, picker.toString())
    }
}
