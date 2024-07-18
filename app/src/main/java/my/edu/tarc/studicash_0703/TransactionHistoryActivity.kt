package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryBinding

class TransactionHistoryActivity : AppCompatActivity(), TransactionAdapter.OnTransactionClickListener {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val expenseTransactions = mutableListOf<Transaction>()
    private val incomeTransactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()

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

        // Toggle between expense and income RecyclerViews
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.expensesButton -> fetchExpenseTransactions()
                    R.id.incomesButton -> fetchIncomeTransactions()
                }
            } else {
                // When no button is selected, show all transactions
                fetchAllTransactionsFromFirestore()
            }
        }

        // Fetch initial data (all transactions)
        fetchAllTransactionsFromFirestore()
    }

    private fun fetchAllTransactionsFromFirestore() {
        fetchExpenseTransactions()
        fetchIncomeTransactions()
    }

    private fun fetchExpenseTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expenseCollection = FirebaseFirestore.getInstance().collection("expenseTransactions")
            .whereEqualTo("userId", userId)
        // Example: add additional filters if needed
        // .whereGreaterThan("amount", 100.0)

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
                Log.d(TAG, "Expense transactions fetched: ${expenseTransactions.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching expense transactions", exception)
            }
    }

    private fun fetchIncomeTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val incomeCollection = FirebaseFirestore.getInstance().collection("incomeTransactions")
            .whereEqualTo("userId", userId)
        // Example: add additional filters if needed
        // .whereGreaterThan("amount", 100.0)

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
                Log.d(TAG, "Income transactions fetched: ${incomeTransactions.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching income transactions", exception)
            }
    }

    private fun updateRecyclerView() {
        val allTransactions = mutableListOf<Transaction>()
        allTransactions.addAll(expenseTransactions)
        allTransactions.addAll(incomeTransactions)

        // Sort transactions by date in descending order
        allTransactions.sortByDescending { it.date }

        // Update the adapter with the new dataset
        transactionAdapter.updateData(allTransactions)
    }

    override fun onDelete(transactionId: String, isExpense: Boolean) {
        deleteTransaction(transactionId, isExpense)
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
                if (isExpense) {
                    fetchExpenseTransactions() // Refresh the list after deletion
                } else {
                    fetchIncomeTransactions() // Refresh for income
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting transaction: ${exception.message}", exception)
            }
    }

    companion object {
        private const val TAG = "TransactionHistory"
    }
}
