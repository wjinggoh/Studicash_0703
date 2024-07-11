package my.edu.tarc.studicash_0703.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.GoalTrackingActivity
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
        val view = binding.root

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
            val intent = Intent(requireContext(), GoalTrackingActivity::class.java)
            startActivity(intent)
        }

        binding.sidebarDrawer.setNavigationItemSelectedListener(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calculate and display today's spending
        calculateTodaySpending()
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

        db.collection("Expense")
            .whereEqualTo("userId", userId) // Filter by user ID
            .whereEqualTo("date", formattedDate)
            .get()
            .addOnSuccessListener { documents ->
                var totalSpending = 0.0
                for (document in documents) {
                    val expense = document.toObject(Transaction::class.java)
                    expense.amount?.let {
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
                Log.e(TAG, "Error calculating spending: $e")
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
            R.id.navAccount -> {
                // Navigate to ProfileFragment
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            // Handle other items as needed
            else -> return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}
