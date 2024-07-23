package my.edu.tarc.studicash_0703

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Fragment.EditTransactionFragment
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryBinding

class TransactionHistoryActivity : AppCompatActivity(), TransactionAdapter.OnTransactionClickListener {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Adapter
        transactionAdapter = TransactionAdapter(this, mutableListOf(), this)
        binding.historyRecycleView.adapter = transactionAdapter
        binding.historyRecycleView.layoutManager = LinearLayoutManager(this)

        // Fetch transactions
        fetchTransactions()

        binding.backBtn3.setOnClickListener {
            finish() // Go back to previous screen
        }
    }

    private fun fetchTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expenseCollection = FirebaseFirestore.getInstance().collection("expenseTransactions")
            .whereEqualTo("userId", userId)
        val incomeCollection = FirebaseFirestore.getInstance().collection("incomeTransactions")
            .whereEqualTo("userId", userId)

        val combinedQuery = FirebaseFirestore.getInstance().collection("allTransactions")
            .whereEqualTo("userId", userId)

        combinedQuery.get()
            .addOnSuccessListener { result ->
                transactions.clear()
                for (document in result) {
                    val transaction = document.toObject(Transaction::class.java).copy(
                        id = document.id // Ensure the ID is set from Firestore
                    )
                    transactions.add(transaction)
                }
                updateRecyclerView()
                Log.d(TAG, "Transactions fetched: ${transactions.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching transactions", exception)
            }
    }

    private fun updateRecyclerView() {
        transactionAdapter.updateData(transactions)
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

    private fun deleteTransaction(transactionId: String, isExpense: Boolean) {
        val collection = if (isExpense) {
            FirebaseFirestore.getInstance().collection("expenseTransactions")
        } else {
            FirebaseFirestore.getInstance().collection("incomeTransactions")
        }

        collection.document(transactionId).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Transaction deleted successfully.")
                fetchTransactions() // Refresh transactions
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting transaction: ${exception.message}", exception)
            }
    }

    override fun onDelete(transactionId: String, isExpense: Boolean) {
        showDeleteConfirmationDialog(transactionId, isExpense)
    }

    override fun onEdit(transactionId: String) {
        // Handle edit action here
        // For example, show an EditTransactionFragment
        val editFragment = EditTransactionFragment.newInstance(transactionId)
        editFragment.show(supportFragmentManager, "EditTransactionDialog")
    }

    companion object {
        private const val TAG = "TransactionHistoryActivity"
    }
}
