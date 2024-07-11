package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.Models.IncomeCategory
import my.edu.tarc.studicash_0703.Models.PaymentMethod
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.databinding.ActivityAddTransactionBinding
import com.google.firebase.Timestamp
import my.edu.tarc.studicash_0703.adapter.CategorySpinnerAdapter
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var incomeCategories = listOf<IncomeCategory>()
    private var expenseCategories = listOf<ExpenseCategory>()
    private var paymentMethods = listOf<PaymentMethod>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        incomeCategories = getIncomeCategories()
        expenseCategories = getExpenseCategories()

        setupCategorySpinner(expenseCategories)
        setupPaymentMethodSpinner()

        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.rbAddExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setupCategorySpinner(expenseCategories)
            }
        }

        binding.rbAddIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setupCategorySpinner(incomeCategories)
            }
        }

        binding.addCategoryButton.setOnClickListener {
            if (binding.rbAddIncome.isChecked) {
                // Start AddIncomeCategoryActivity
                val intent = Intent(this, AddIncomeCategoryActivity::class.java)
                startActivity(intent)
            } else if (binding.rbAddExpense.isChecked) {
                // Start AddExpenseCategoryActivity
                val intent = Intent(this, AddExpensesCategoryActivity::class.java)
                startActivity(intent)
            }
        }

        binding.saveTransactionButton.setOnClickListener {
            validateAndSaveTransaction()
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupCategorySpinner(categories: List<Any>) {
        val adapter = if (categories.firstOrNull() is ExpenseCategory) {
            CategorySpinnerAdapter(this, categories as List<ExpenseCategory>)
        } else {
            CategorySpinnerAdapter(this, categories as List<IncomeCategory>)
        }
        binding.categorySpinner.adapter = adapter
    }

    private fun fetchPaymentMethods() {
        firestore.collection("PaymentMethods")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Handle case where no documents are found
                    Toast.makeText(this, "No payment methods found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                paymentMethods = result.map { document ->
                    val type = document.getString("type") ?: ""
                    val details = document.getString("details") ?: ""
                    PaymentMethod(type, details)
                }

                setupPaymentMethodSpinner()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch payment methods: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error fetching payment methods", exception)
            }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$year-${month + 1}-$dayOfMonth"
                binding.dateView.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateAndSaveTransaction() {
        val title = binding.transactionTitleInput.editText?.text.toString()
        val amountText = binding.transactionAmountInput.editText?.text.toString()
        val amount = amountText.toDoubleOrNull()
        val category = binding.categorySpinner.selectedItem.toString()
        val date = binding.dateView.text.toString()
        val paymentMethod = binding.paymentMethodSpinner.selectedItem.toString()
        val isExpense = binding.rbAddExpense.isChecked
        val userId = auth.currentUser?.uid

        if (title.isEmpty() || amount == null || date.isEmpty() || userId == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0) {
            binding.transactionAmountInput.error = "Invalid amount"
            return
        }

        saveTransaction(title, amount, category, date, paymentMethod, isExpense, userId)
    }

    private fun setupPaymentMethodSpinner() {
        // Fetch payment methods from Firestore
        firestore.collection("paymentMethods")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Handle case where no documents are found
                    Toast.makeText(this, "No payment methods found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                paymentMethods = result.map { document ->
                    val type = document.getString("type") ?: ""
                    val details = document.getString("details") ?: ""
                    PaymentMethod(type, details)
                }

                // Create adapter and set it to the spinner
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods.map { it.type })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.paymentMethodSpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch payment methods: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error fetching payment methods", exception)
            }
    }


    private fun saveTransaction(
        title: String,
        amount: Double,
        category: String,
        date: String,
        paymentMethod: String,
        isExpense: Boolean,
        userId: String
    ) {
        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = date,
            paymentMethod = paymentMethod,
            isExpense = isExpense,
            userId = userId,
            timestamp = Timestamp.now(),
            expenseColorRes = R.drawable.expense,
            incomeColorRes = R.drawable.income
        )

        val collectionName = if (isExpense) {
            "expenseTransactions"
        } else {
            "incomeTransactions"
        }

        firestore.collection(collectionName)
            .add(transaction)
            .addOnSuccessListener {
                Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save transaction: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getIncomeCategories(): List<IncomeCategory> {
        return listOf(
            IncomeCategory(R.drawable.salary, "Salary"),
            IncomeCategory(R.drawable.allowance, "Allowance"),
            IncomeCategory(R.drawable.freelancer, "Freelance"),
            IncomeCategory(R.drawable.custom_category, "Other")
        )
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
