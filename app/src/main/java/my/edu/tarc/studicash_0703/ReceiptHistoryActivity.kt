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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.adapter.ReceiptAdapter
import com.google.firebase.auth.FirebaseAuth

class ReceiptHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceiptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.ReceiptRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize with an empty list
        adapter = ReceiptAdapter(emptyList())
        recyclerView.adapter = adapter

        // Fetch receipts and update adapter
        fetchReceiptsAndUpdateUI()

        val backBtn = findViewById<View>(R.id.receiptHistoryBackBtn)
        backBtn.setOnClickListener {
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
