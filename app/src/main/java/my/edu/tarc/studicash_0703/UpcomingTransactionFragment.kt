package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.adapter.UpcomingTransactionAdapter
import my.edu.tarc.studicash_0703.databinding.FragmentUpcomingTransactionBinding

class UpcomingTransactionFragment : Fragment() {
    private lateinit var binding: FragmentUpcomingTransactionBinding
    private lateinit var upcomingTransactionAdapter: UpcomingTransactionAdapter
    private val upcomingTransactions = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpcomingTransactionBinding.inflate(inflater, container, false)

        // Initialize RecyclerView and Adapter
        upcomingTransactionAdapter = UpcomingTransactionAdapter(upcomingTransactions)
        binding.recyclerViewUpcomingTransactions.adapter = upcomingTransactionAdapter
        binding.recyclerViewUpcomingTransactions.layoutManager = LinearLayoutManager(requireContext())

        // Fetch upcoming transactions (this would be replaced with actual data fetching logic)
        fetchUpcomingTransactions()

        return binding.root
    }

    private fun fetchUpcomingTransactions() {
        // Replace with actual fetching logic
        // Example: upcomingTransactions.add(Transaction("Example Transaction", 100.0, Date()))

        // Update RecyclerView
        upcomingTransactionAdapter.notifyDataSetChanged()
    }
}
