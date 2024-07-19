package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Fragment.EditTransactionFragment
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityExpensesHistoryBinding
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryBinding

class ExpensesHistoryActivity : AppCompatActivity(), TransactionAdapter.OnTransactionClickListener {
    private lateinit var binding: ActivityExpensesHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val expenseTransactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding
        binding = ActivityExpensesHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transactionAdapter = TransactionAdapter(this, mutableListOf(), this)
        binding.historyRecycleView.apply {
            layoutManager = LinearLayoutManager(this@ExpensesHistoryActivity)
            adapter = transactionAdapter
        }

        binding.allButton.setOnClickListener {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.expensesButton.setOnClickListener {
            fetchExpenseTransactions()
        }

        binding.incomesButtonMain.setOnClickListener {
            val intent = Intent(this, IncomeHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        fetchExpenseTransactions()
    }

    private fun fetchExpenseTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expenseCollection = FirebaseFirestore.getInstance().collection("expenseTransactions")
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
                Log.d(TAG, "Expense transactions fetched: ${expenseTransactions.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching expense transactions", exception)
            }
    }

    private fun updateRecyclerView() {
        val allTransactions = mutableListOf<Transaction>()
        allTransactions.addAll(expenseTransactions)

        // Sort transactions by date in descending order
        allTransactions.sortByDescending { it.date }

        // Update the adapter with the new dataset
        transactionAdapter.updateData(allTransactions)
    }

    private fun deleteTransaction(transactionId: String, isExpense: Boolean) {
        val collection = FirebaseFirestore.getInstance().collection("expenseTransactions")

        collection.document(transactionId).delete()
            .addOnSuccessListener {
                fetchExpenseTransactions() // Refresh the list after deletion
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting transaction: ${exception.message}", exception)
            }
    }

    override fun onDelete(transactionId: String, isExpense: Boolean) {
        deleteTransaction(transactionId, isExpense)
    }

    override fun onEdit(transactionId: String) {
        // Handle the edit event, e.g., start an edit activity
        val intent = Intent(this, EditTransactionFragment::class.java)
        intent.putExtra("transactionId", transactionId)
        startActivity(intent)
    }

    companion object {
        const val TAG = "ExpensesHistory"
    }
}
