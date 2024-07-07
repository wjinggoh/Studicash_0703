package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Expense
import my.edu.tarc.studicash_0703.R
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var todaySpendingAmt: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        todaySpendingAmt = view.findViewById(R.id.todaySpendingAmt)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calculate and display today's spending
        calculateTodaySpending()
    }

    private fun calculateTodaySpending() {
        val db = FirebaseFirestore.getInstance()

        // Get today's date
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(currentDate)

        Log.d(TAG, "Formatted Date: $formattedDate")

        db.collection("Expense")
            .whereEqualTo("date", formattedDate)
            .get()
            .addOnSuccessListener { documents ->
                var totalSpending = 0.0
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expense.expenseAmount?.let {
                        totalSpending += it
                    }
                    Log.d(TAG, "Expense: $expense")
                }
                Log.d(TAG, "Total Spending Today: $totalSpending")

                activity?.runOnUiThread {
                    todaySpendingAmt.text = String.format(Locale.getDefault(), "%.2f", totalSpending)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error calculating spending: $e")
                todaySpendingAmt.text = "Error calculating spending"
            }
    }

    companion object {
        const val TAG = "my.edu.tarc.studicash_0703.Fragment.HomeFragment"
    }
}
