package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import my.edu.tarc.studicash_0703.Adapter.ExpenseAdapter
import my.edu.tarc.studicash_0703.Adapter.IncomeAdapter
import my.edu.tarc.studicash_0703.Models.Expense
import my.edu.tarc.studicash_0703.Models.Income
import my.edu.tarc.studicash_0703.databinding.FragmentAddBinding

class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var incomeAdapter: IncomeAdapter
    private var expensesList: MutableList<Expense> = mutableListOf()
    private var incomesList: MutableList<Income> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Initialize RecyclerViews and Adapters
        expenseAdapter = ExpenseAdapter(requireContext(), expensesList)
        incomeAdapter = IncomeAdapter(requireContext(), incomesList)

        binding.historyRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter // Start with expense adapter
        }

        // Fetch data from Firestore
        fetchExpenseDataFromFirestore()
        fetchIncomeDataFromFirestore()

        // Toggle between expense and income RecyclerViews
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.expensesButton -> binding.historyRecycleView.adapter = expenseAdapter
                    R.id.incomesButton -> binding.historyRecycleView.adapter = incomeAdapter
                }
            }
        }

        // Set click listener for createTransactionbtn
        binding.createTransactionbtn.setOnClickListener {
            val transactionFragment = ChooseTransactionFragment()
            transactionFragment.show(childFragmentManager, "ChooseTransactionDialog")
        }

        return binding.root
    }

    private fun fetchExpenseDataFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId == null) {
            // Handle the case where the user is not logged in
            println("User not logged in")
            return
        }

        db.collection("Expense")
            .whereEqualTo("userId", userId) // Filter by user ID
            .orderBy("date", Query.Direction.DESCENDING) // Then order by date descending
            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp descending
            .get()
            .addOnSuccessListener { documents ->
                expensesList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                    println("Expense timestamp: ${expense.timestamp?.toDate()}")
                }
                expenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error fetching expenses: ${exception.message}")
            }
    }

    private fun fetchIncomeDataFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId == null) {
            // Handle the case where the user is not logged in
            println("User not logged in")
            return
        }

        db.collection("Income")
            .whereEqualTo("userId", userId) // Filter by user ID
            .orderBy("date", Query.Direction.DESCENDING) // Then order by date descending
            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp descending
            .get()
            .addOnSuccessListener { documents ->
                incomesList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val income = document.toObject(Income::class.java)
                    incomesList.add(income)
                    println("Income timestamp: ${income.timestamp?.toDate()}")
                }
                incomeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error fetching incomes: ${exception.message}")
            }
    }
}
