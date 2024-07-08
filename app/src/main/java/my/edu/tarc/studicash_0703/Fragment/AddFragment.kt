package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
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
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId == null) {
            // Handle the case where the user is not logged in
            println("User not logged in")
            return
        }

        db.collection("Expense")
            .whereEqualTo("userId", userId) // Filter by user ID
            .get()
            .addOnSuccessListener { documents ->
                expensesList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
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
