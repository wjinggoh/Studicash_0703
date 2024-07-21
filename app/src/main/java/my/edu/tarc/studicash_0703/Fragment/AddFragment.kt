package my.edu.tarc.studicash_0703.Fragment

import android.content.Intent
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.TransactionHistoryActivity
import my.edu.tarc.studicash_0703.databinding.FragmentAddBinding

class AddFragment : Fragment(), TransactionAdapter.OnTransactionClickListener {
    private lateinit var binding: FragmentAddBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val expenseTransactions = mutableListOf<Transaction>()
    private val incomeTransactions = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)

        binding.createTransactionbtn.setOnClickListener {
            val transactionFragment = ChooseTransactionFragment()
            transactionFragment.show(childFragmentManager, "ChooseTransactionDialog")
        }

        binding.viewFullHistoryBtn.setOnClickListener{
            val intent = Intent(requireContext(), TransactionHistoryActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView and Adapter
        transactionAdapter = TransactionAdapter(requireContext(), mutableListOf(), this)
        binding.addFragmentRecycleView.adapter = transactionAdapter
        binding.addFragmentRecycleView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch transactions
        fetchExpenseTransactions()
        fetchIncomeTransactions()


        return binding.root
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

        val latestTransactions = allTransactions
            .sortedByDescending { it.date }
            .take(5)

        transactionAdapter.updateData(latestTransactions)
    }


    override fun onDelete(transactionId: String, isExpense: Boolean) {
        deleteTransaction(transactionId, isExpense)
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
                if (isExpense) {
                    fetchExpenseTransactions() // Refresh expenses
                    Log.d(TAG, "Fetched ${expenseTransactions.size} expense transactions.")
                } else {
                    fetchIncomeTransactions() // Refresh incomes

                    Log.d(TAG, "Fetched ${incomeTransactions.size} income transactions.")

                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting transaction: ${exception.message}", exception)
            }
    }



    private fun verifyTransactionIdExists(transactionId: String, isExpense: Boolean) {
        val collection = if (isExpense) {
            FirebaseFirestore.getInstance().collection("expenseTransactions")
        } else {
            FirebaseFirestore.getInstance().collection("incomeTransactions")
        }

        collection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.id == transactionId) {
                        Log.d(TAG, "Transaction ID $transactionId exists.")
                        return@addOnSuccessListener
                    }
                }
                Log.e(TAG, "Transaction ID $transactionId does not exist.")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching transactions: ${exception.message}", exception)
            }
    }




    override fun onEdit(transactionId: String) {
        // Open an edit dialog or fragment
        val editTransactionFragment = EditTransactionFragment.newInstance(transactionId)
        editTransactionFragment.show(childFragmentManager, "EditTransactionDialog")
    }


    companion object {
        private const val TAG = "AddFragment"
    }
}
