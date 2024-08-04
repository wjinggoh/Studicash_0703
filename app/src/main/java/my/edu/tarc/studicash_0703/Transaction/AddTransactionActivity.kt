package my.edu.tarc.studicash_0703.Transaction

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.Models.IncomeCategory
import my.edu.tarc.studicash_0703.PaymentMethod.PaymentMethod
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.databinding.ActivityAddTransactionBinding
import com.google.firebase.Timestamp
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.CategorySpinnerAdapter
import my.edu.tarc.studicash_0703.sidebar.NotificationsActivity
import java.text.SimpleDateFormat
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

        fetchCategories()
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
                val intent = Intent(this, AddIncomeCategoryActivity::class.java)
                startActivity(intent)
            } else if (binding.rbAddExpense.isChecked) {
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

    override fun onResume() {
        super.onResume()
        fetchCategories() // Fetch categories on resume
    }

    private fun fetchCategories() {
        val uid = auth.currentUser?.uid ?: return // Exit if UID is null

        // Fetch Income Categories
        firestore.collection("IncomeCategories")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                val fetchedIncomeCategories = result.map { document ->
                    IncomeCategory(
                        icon = R.drawable.custom_category, // Fallback icon resource
                        name = document.getString("name") ?: "",
                        iconUri = document.getString("iconUri") // Fetch icon URL
                    )
                }

                // Combine fetched categories with predefined categories (if any)
                incomeCategories = fetchedIncomeCategories + getIncomeCategories()

                // If the Add Income radio button is checked, update the spinner
                if (binding.rbAddIncome.isChecked) {
                    setupCategorySpinner(incomeCategories)
                }
            }

        // Fetch Expense Categories
        firestore.collection("ExpenseCategories")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                val fetchedExpenseCategories = result.map { document ->
                    ExpenseCategory(
                        icon = R.drawable.custom_category, // Fallback icon resource
                        name = document.getString("name") ?: "",
                        iconUri = document.getString("iconUri") // Fetch icon URL
                    )
                }

                // Combine fetched categories with predefined categories (if any)
                expenseCategories = fetchedExpenseCategories + getExpenseCategories()

                // If the Add Expense radio button is checked, update the spinner
                if (binding.rbAddExpense.isChecked) {
                    setupCategorySpinner(expenseCategories)
                }
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
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("PaymentMethods")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "No payment methods found!", Toast.LENGTH_SHORT).show()
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
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("paymentMethods")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "No payment methods found, please add your payment method in Settings!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                paymentMethods = result.map { document ->
                    val details = document.getString("details") ?: ""
                    PaymentMethod(details)
                }

                // Create an adapter with the fetched payment methods
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    paymentMethods.map { "${it.type}: ${it.details}" }
                )
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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = sdf.format(sdf.parse(date))

        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = formattedDate,
            paymentMethod = paymentMethod,
            expense = isExpense,
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
            ExpenseCategory(R.drawable.receipts, "Receipt"),
            ExpenseCategory(R.drawable.custom_category, "Other")
        )
    }

    private fun sendNewTransactionNotification(transaction: Transaction) {
        try {
            val channelId = "transaction_notifications"
            val notificationId = (System.currentTimeMillis() % 10000).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Transaction Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new transactions"
                }
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            val notificationIntent = Intent(this, NotificationsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo) // Use your app icon
                .setContentTitle("New Transaction Added")
                .setContentText("Transaction '${transaction.title}' of amount ${transaction.amount} was added.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Failed to send notification: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Notification", "Error sending notification", e)
        }
    }

}
