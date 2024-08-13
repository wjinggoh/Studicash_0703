package my.edu.tarc.studicash_0703

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import my.edu.tarc.studicash_0703.Models.Feedback
import my.edu.tarc.studicash_0703.adapter.FeedbackAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityFeedbackComplaintReceiveBinding
import java.util.Date

class FeedbackComplaintReceiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackComplaintReceiveBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFeedbackComplaintReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView
        binding.feedbackRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchFeedbackData()

        binding.receiveBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun fetchFeedbackData() {
        firestore.collection("Feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING)  // Order by timestamp
            .get()
            .addOnSuccessListener { result ->
                val feedbackList = result.map { document ->
                    val email = document.getString("email") ?: ""
                    val feedback = document.getString("feedback") ?: ""

                    val timestamp = when (val timestampField = document.get("timestamp")) {
                        is Timestamp -> timestampField
                        is Long -> Timestamp(Date(timestampField))  // Convert Long to Date, then to Timestamp
                        else -> Timestamp.now()  // Default value in case of unexpected type
                    }

                    Feedback(email, feedback, timestamp)
                }
                binding.feedbackRecyclerView.adapter = FeedbackAdapter(feedbackList) { feedback ->
                    sendEmail(feedback.email)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                exception.printStackTrace()
            }
    }

    private fun sendEmail(email: String) {
        val subject = "Hi Studicash Team,"
        val emailBody = "Body of the email"

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Log.e("FeedbackComplaintReceiveActivity", "Error launching email client", e)
            Toast.makeText(this, "Error launching email client", Toast.LENGTH_SHORT).show()
        }
    }
}
