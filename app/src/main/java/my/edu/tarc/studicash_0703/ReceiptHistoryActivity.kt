package my.edu.tarc.studicash_0703

import my.edu.tarc.studicash_0703.Models.Receipt
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.adapter.ReceiptAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityReceiptHistoryBinding

class ReceiptHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptHistoryBinding
    private lateinit var adapter: ReceiptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReceiptHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ReceiptRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize with an empty list
        adapter = ReceiptAdapter(emptyList())
        binding.ReceiptRecyclerView.adapter = adapter

        // Fetch receipts and update adapter
        fetchReceiptsAndUpdateUI()

        binding.receiptHistoryBackBtn.setOnClickListener {
            onBackPressed()
        }


    }

    private fun fetchReceiptsAndUpdateUI() {
        val userId = getCurrentUserUid()  // Get the current user's UID
        if (userId.isEmpty()) {
            Log.w("ReceiptHistoryActivity", "User ID is empty. Cannot fetch receipts.")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Receipt")
            .whereEqualTo("userId", userId) // Filter receipts by userId
            .get()
            .addOnSuccessListener { result ->
                val receipts = result.map { document ->
                    document.toObject(Receipt::class.java)
                }
                adapter.updateReceipts(receipts)
            }
            .addOnFailureListener { exception ->
                Log.w("ReceiptHistoryActivity", "Error fetching receipts", exception)
            }
    }

    private fun getCurrentUserUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}
