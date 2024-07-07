package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Adapter.ExpenseAdapter
import my.edu.tarc.studicash_0703.Models.Expense
import my.edu.tarc.studicash_0703.databinding.FragmentAddBinding

class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private var expensesList: MutableList<Expense> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Initialize RecyclerView and Adapter
        expenseAdapter = ExpenseAdapter(requireContext(), expensesList)
        binding.historyRecycleView.adapter = expenseAdapter
        binding.historyRecycleView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch data from Firestore
        fetchExpenseDataFromFirestore()

        // Set click listener for createTransactionbtn
        binding.createTransactionbtn.setOnClickListener {
            val transactionFragment = ChooseTransactionFragment()
            transactionFragment.show(childFragmentManager, "ChooseTransactionDialog")
        }

        return binding.root
    }

    private fun fetchExpenseDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Expense")
            .get()
            .addOnSuccessListener { documents ->
                expensesList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val expenseTitle = document.getString("expenseTitle") ?: ""
                    val expenseAmount = document.getDouble("expenseAmount") ?: 0.0
                    val date = document.getString("date") ?: ""
                    val category = document.getString("category") ?: ""
                    val paymentMethod = document.getString("paymentmethod") ?: ""

                    val expense = Expense(expenseTitle, expenseAmount, date, category, paymentMethod)
                    expensesList.add(expense)
                }
                // Notify adapter of data change
                expenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle the error here
                println("Error fetching data: ${exception.message}")
            }
    }



}
