import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import my.edu.tarc.studicash_0703.adapter.TransactionAdapter
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryBinding
import com.google.firebase.Timestamp
import java.util.Date
import my.edu.tarc.studicash_0703.R


class TransactionHistoryActivity : AppCompatActivity() {
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
        transactionAdapter = TransactionAdapter(this, transactionsList)

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
                transactionsList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactionsList.add(transaction)
                    // Convert timestamp to Date manually
                    val timestamp = transaction.timestamp?.toDate()
                    println("Expense timestamp: $timestamp")
                }
                transactionAdapter.notifyDataSetChanged()
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
                transactionsList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactionsList.add(transaction)
                    // Convert timestamp to Date manually
                    val timestamp = transaction.timestamp?.toDate()
                    println("Income timestamp: $timestamp")
                }
                transactionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error fetching incomes: ${exception.message}")
            }
    }
}
