package my.edu.tarc.studicash_0703.Fragment

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
import my.edu.tarc.studicash_0703.databinding.FragmentAddBinding

class AddFragment : Fragment() {
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

        // Initialize RecyclerView and Adapter
        transactionAdapter = TransactionAdapter(requireContext(), mutableListOf())
        binding.recyclerView.adapter = transactionAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch transactions
        fetchExpenseTransactions()
        fetchIncomeTransactions()

        return binding.root
    }

    private fun fetchExpenseTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId ?: return  // Return if user is not logged in

        val expenseCollection = FirebaseFirestore.getInstance().collection("expenseTransactions")
            .whereEqualTo("userId", userId)

        expenseCollection.get()
            .addOnSuccessListener { result ->
                expenseTransactions.clear() // Clear previous data
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val category = document.getString("category") ?: ""
                    val date = document.getString("date") ?: ""
                    val paymentMethod = document.getString("paymentMethod") ?: ""
                    val isExpense = document.getBoolean("isExpense") ?: true

                    val transaction = Transaction(
                        title = title,
                        amount = amount,
                        category = category,
                        date =date,
                        paymentMethod = paymentMethod,
                        isExpense = isExpense,
                        userId = userId
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId ?: return  // Return if user is not logged in

        val incomeCollection = FirebaseFirestore.getInstance().collection("incomeTransactions")
            .whereEqualTo("userId", userId)

        incomeCollection.get()
            .addOnSuccessListener { result ->
                incomeTransactions.clear() // Clear previous data
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val category = document.getString("category") ?: ""
                    val date = document.getString("date") ?: ""
                    val paymentMethod = document.getString("paymentMethod") ?: ""
                    val isExpense = document.getBoolean("isExpense") ?: false

                    val transaction = Transaction(
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        isExpense = isExpense,
                        userId = userId
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

        // Sort and limit to the latest 5 transactions
        val latestTransactions = allTransactions
            .sortedByDescending { it.date }
            .take(5)

        transactionAdapter.updateData(latestTransactions)
    }

    companion object {
        private const val TAG = "AddFragment"
    }
}
