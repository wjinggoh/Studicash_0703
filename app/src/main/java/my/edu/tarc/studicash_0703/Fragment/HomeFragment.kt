package my.edu.tarc.studicash_0703.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import my.edu.tarc.studicash_0703.ReportActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.BudgetTrackingActivity
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        drawerLayout = binding.drawerLayout

        // Set up menu icon click listener to toggle drawer
        binding.sidebaricon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.goalTrackingBtn.setOnClickListener {
            val intent = Intent(requireContext(), BudgetTrackingActivity::class.java)
            startActivity(intent)
        }

        binding.reportBtn.setOnClickListener{
            val intent = Intent(requireContext(), ReportActivity::class.java)
            startActivity(intent)
        }
        binding.sidebarDrawer.setNavigationItemSelectedListener(this)

        // Fetch and display user details
        displayUserDetails()

        // Calculate today's spending
        calculateTodaySpending()

        calculateMonthIncomeExpense()

        return binding.root
    }

    private fun displayUserDetails() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val headerView = binding.sidebarDrawer.getHeaderView(0)

        val userImageView = headerView.findViewById<ImageView>(R.id.userImageView)
        val userNameTextView = headerView.findViewById<TextView>(R.id.userNameTextView)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.userEmailTextView)

        user.displayName?.let {
            userNameTextView.text = it
        }

        user.email?.let {
            userEmailTextView.text = it
        }

        user.photoUrl?.let {
            Glide.with(this).load(it).into(userImageView)
        }

        db.collection("User").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name")
                    val userEmail = document.getString("email")
                    val userImageUrl = document.getString("imageUrl")

                    userName?.let {
                        userNameTextView.text = it
                    }

                    userEmail?.let {
                        userEmailTextView.text = it
                    }

                    userImageUrl?.let {
                        Glide.with(this).load(it).into(userImageView)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user details", e)
            }
    }

    private fun calculateTodaySpending() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId == null) {
            binding.todaySpendingAmt.text = "User not logged in"
            return
        }

        // Get today's date
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = sdf.format(currentDate)

        Log.d(TAG, "Formatted Date: $formattedDate")

        db.collection("expenseTransactions")
            .whereEqualTo("userId", userId) // Filter by user ID
            .whereEqualTo("date", formattedDate)
            .get()
            .addOnSuccessListener { documents ->
                var totalSpending = 0.0
                for (document in documents) {
                    val expense = document.toObject(Transaction::class.java)
                    expense.amount.let {
                        totalSpending += it
                    }
                    Log.d(TAG, "Expense: $expense")
                }
                Log.d(TAG, "Total Spending Today: $totalSpending")

                activity?.runOnUiThread {
                    binding.todaySpendingAmt.text = String.format(Locale.getDefault(), "%.2f", totalSpending)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting documents: ", e)
                binding.todaySpendingAmt.text = "Error calculating spending"
            }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navHome -> {
                // Handle Home action
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.navNotification -> {
                // Navigate to ProfileFragment
                findNavController().navigate(R.id.action_fragment_home_to_notificationsActivity)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.navHelp->{
                // Navigate to ProfileFragment
                findNavController().navigate(R.id.action_fragment_home_to_helpActivity)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }

            R.id.navContact->{
                // Navigate to ProfileFragment
                findNavController().navigate(R.id.action_fragment_home_to_contactUsActivity)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            else -> return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun calculateMonthIncomeExpense() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId == null) {
            // Handle case where user is not logged in
            return
        }

        // Get start and end dates for the current month
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.set(currentYear, currentMonth, 1)
        val startDate = calendar.time
        calendar.set(Calendar.MONTH, currentMonth + 1)
        val endDate = calendar.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startFormattedDate = sdf.format(startDate)
        val endFormattedDate = sdf.format(endDate)

        Log.d(TAG, "Start Date: $startFormattedDate, End Date: $endFormattedDate")

        // Fetch income transactions
        db.collection("incomeTransactions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startFormattedDate)
            .get()
            .addOnSuccessListener { documents ->
                var totalIncome = 0.0
                for (document in documents) {
                    val income = document.toObject(Transaction::class.java)
                    income.amount.let {
                        totalIncome += it
                    }
                    Log.d(TAG, "Income: $income")
                }
                Log.d(TAG, "Total Income This Month: $totalIncome")

                // Update UI with total income
                binding.monthIncomeAmt.text = String.format(Locale.getDefault(), "%.2f", totalIncome)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting income documents: ", e)
                binding.monthIncomeAmt.text = "Error calculating income"
            }

        // Fetch expense transactions
        db.collection("expenseTransactions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startFormattedDate)
            .whereLessThan("date", endFormattedDate)
            .get()
            .addOnSuccessListener { documents ->
                var totalExpense = 0.0
                for (document in documents) {
                    val expense = document.toObject(Transaction::class.java)
                    expense.amount.let {
                        totalExpense += it
                    }
                    Log.d(TAG, "Expense: $expense")
                }
                Log.d(TAG, "Total Expense This Month: $totalExpense")

                // Update UI with total expense
                binding.monthExpenseAmt.text = String.format(Locale.getDefault(), "%.2f", totalExpense)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting expense documents: ", e)
                binding.monthExpenseAmt.text = "Error calculating expense"
            }
    }



    companion object {
        const val TAG = "HomeFragment"
    }
}
