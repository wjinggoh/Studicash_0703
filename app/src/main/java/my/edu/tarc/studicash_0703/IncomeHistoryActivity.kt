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
import my.edu.tarc.studicash_0703.databinding.ActivityIncomeHistoryBinding

class IncomeHistoryActivity : AppCompatActivity(), TransactionAdapter.OnTransactionClickListener {
    private lateinit var binding: ActivityIncomeHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val incomeTransactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding
        binding = ActivityIncomeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transactionAdapter = TransactionAdapter(this, mutableListOf(), this)
        binding.historyRecycleView.apply {
            layoutManager = LinearLayoutManager(this@IncomeHistoryActivity)
            adapter = transactionAdapter
        }

        binding.allButton.setOnClickListener {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.expensesButton.setOnClickListener {
            val intent = Intent(this, ExpensesHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.incomesButtonMain.setOnClickListener {
            fetchIncomeTransactions()
        }

        fetchIncomeTransactions()
    }

    private fun fetchIncomeTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expenseCollection = FirebaseFirestore.getInstance().collection("incomeTransactions")
            .whereEqualTo("userId", userId)

        expenseCollection.get()
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
        allTransactions.addAll(incomeTransactions)

        // Sort transactions by date in descending order
        allTransactions.sortByDescending { it.date }

        // Update the adapter with the new dataset
        transactionAdapter.updateData(allTransactions)
    }

    private fun deleteTransaction(transactionId: String, isExpense: Boolean) {
        val collection = FirebaseFirestore.getInstance().collection("incomeTransactions")

        collection.document(transactionId).delete()
            .addOnSuccessListener {
                fetchIncomeTransactions() // Refresh the list after deletion
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
        const val TAG = "IncomeHistory"
    }
}
