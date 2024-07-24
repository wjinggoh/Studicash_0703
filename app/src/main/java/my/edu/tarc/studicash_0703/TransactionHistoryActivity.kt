package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryBinding

class TransactionHistoryActivity : AppCompatActivity(), TransactionAdapter.OnTransactionClickListener {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val expenseTransactions = mutableListOf<Transaction>()
    private val incomeTransactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()

    private val addDurationActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val startDate = data?.getStringExtra("startDate")
            val endDate = data?.getStringExtra("endDate")
            binding.StartDate.text = startDate
            binding.endDate.text = endDate
            fetchTransactionsWithinDateRange(startDate, endDate)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transactionAdapter = TransactionAdapter(this, mutableListOf(), this)
        binding.historyRecycleView.apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            adapter = transactionAdapter
        }

        binding.allButtonMain.setOnClickListener {
            fetchAllTransactionsFromFirestore()
        }

        binding.expensesButton.setOnClickListener {
            val intent = Intent(this, ExpensesHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.incomesButton.setOnClickListener {
            val intent = Intent(this, IncomeHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.AddDurationButton.setOnClickListener {
            val intent = Intent(this, TransactionHistoryAddDurationActivity::class.java)
            addDurationActivityResultLauncher.launch(intent)
        }

        binding.backBtn3.setOnClickListener {
            onBackPressed()
        }

        fetchAllTransactionsFromFirestore()
    }

    private fun fetchAllTransactionsFromFirestore() {
        fetchExpenseTransactions()
        fetchIncomeTransactions()
    }

    private fun fetchExpenseTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expenseCollection = db.collection("expenseTransactions")
            .whereEqualTo("userId", userId)

        expenseCollection.get()
            .addOnSuccessListener { result ->
                expenseTransactions.clear()
                for (document in result) {
                    val transaction = document.toObject(Transaction::class.java).copy(
                        id = document.id // Ensure the ID is set from Firestore
                    )
                    expenseTransactions.add(transaction)
                }
                updateRecyclerView()
                Log.d(ExpensesHistoryActivity.TAG, "Expense transactions fetched: ${expenseTransactions.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(ExpensesHistoryActivity.TAG, "Error fetching expense transactions", exception)
            }
    }

    private fun fetchIncomeTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val incomeCollection = FirebaseFirestore.getInstance().collection("incomeTransactions")
            .whereEqualTo("userId", userId)

        incomeCollection.get()
            .addOnSuccessListener { result ->
                incomeTransactions.clear()
                for (document in result) {
                    val transaction = document.toObject(Transaction::class.java).copy(
                        id = document.id // Ensure the ID is set from Firestore
                    )
                    incomeTransactions.add(transaction)
                }
                updateRecyclerView()
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun updateRecyclerView() {
        val allTransactions = mutableListOf<Transaction>()
        allTransactions.addAll(expenseTransactions)
        allTransactions.addAll(incomeTransactions)

        allTransactions.sortByDescending { it.date }

        transactionAdapter.updateData(allTransactions)
    }

    private fun fetchTransactionsWithinDateRange(startDate: String?, endDate: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expenseCollection = FirebaseFirestore.getInstance().collection("expenseTransactions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate ?: "")
            .whereLessThanOrEqualTo("date", endDate ?: "")

        val incomeCollection = FirebaseFirestore.getInstance().collection("incomeTransactions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate ?: "")
            .whereLessThanOrEqualTo("date", endDate ?: "")

        val allTransactions = mutableListOf<Transaction>()

        expenseCollection.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val transaction = document.toObject(Transaction::class.java).copy(
                        id = document.id
                    )
                    allTransactions.add(transaction)
                }

                incomeCollection.get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val transaction = document.toObject(Transaction::class.java).copy(
                                id = document.id
                            )
                            allTransactions.add(transaction)
                        }

                        allTransactions.sortByDescending { it.date }
                        transactionAdapter.updateData(allTransactions)
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                    }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun showDeleteConfirmationDialog(transactionId: String, isExpense: Boolean) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteTransaction(transactionId, isExpense)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDelete(transactionId: String, isExpense: Boolean) {
        showDeleteConfirmationDialog(transactionId, isExpense)
    }

    override fun onEdit(transactionId: String) {
        // Handle edit transaction
    }

    private fun deleteTransaction(transactionId: String, isExpense: Boolean) {
        val collection = if (isExpense) {
            FirebaseFirestore.getInstance().collection("expenseTransactions")
        } else {
            FirebaseFirestore.getInstance().collection("incomeTransactions")
        }

        collection.document(transactionId).delete()
            .addOnSuccessListener {
                Log.d(ExpensesHistoryActivity.TAG, "Transaction deleted successfully.")
                fetchExpenseTransactions() // Refresh expenses
                fetchIncomeTransactions() // Refresh incomes if needed
            }
            .addOnFailureListener { exception ->
                Log.e(ExpensesHistoryActivity.TAG, "Error deleting transaction: ${exception.message}", exception)
            }
    }
}
