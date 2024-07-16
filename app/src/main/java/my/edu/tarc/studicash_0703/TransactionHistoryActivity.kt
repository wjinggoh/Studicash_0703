package my.edu.tarc.studicash_0703

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryBinding
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter

class TransactionHistoryActivity : AppCompatActivity(), TransactionAdapter.OnTransactionClickListener {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private var transactionsList: MutableList<Transaction> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView and Adapter
        transactionAdapter = TransactionAdapter(this, transactionsList, this)

        binding.historyRecycleView.apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            adapter = transactionAdapter
        }

        // Toggle between expense and income RecyclerViews
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.expensesButton -> fetchExpenseDataFromFirestore()
                    R.id.incomesButton -> fetchIncomeDataFromFirestore()
                }
            }
        }

        // Fetch initial data
        fetchExpenseDataFromFirestore()
    }

    private fun fetchExpenseDataFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        db.collection("Expense")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                transactionsList.clear()
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactionsList.add(transaction)
                }
                transactionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error fetching expenses: ${exception.message}")
            }
    }

    private fun fetchIncomeDataFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        db.collection("Income")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                transactionsList.clear()
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactionsList.add(transaction)
                }
                transactionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error fetching incomes: ${exception.message}")
            }
    }

    // Implementing the OnTransactionClickListener methods
    override fun onDelete(transactionId: String, isExpense: Boolean) {
        deleteTransaction(transactionId, isExpense)
    }

    override fun onEdit(transactionId: String) {
        // Handle edit transaction
    }

    private fun deleteTransaction(transactionId: String, isExpense: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val collection = if (isExpense) {
            db.collection("Expense")
        } else {
            db.collection("Income")
        }

        collection.document(transactionId).delete()
            .addOnSuccessListener {
                if (isExpense) {
                    fetchExpenseDataFromFirestore() // Refresh the list after deletion
                } else {
                    fetchIncomeDataFromFirestore() // Refresh for income
                }
            }
            .addOnFailureListener { exception ->
                println("Error deleting transaction: ${exception.message}")
            }
    }
}
