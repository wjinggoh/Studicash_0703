package my.edu.tarc.studicash_0703.sidebar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityFeedbackAndComplaintBinding

class FeedbackAndComplaintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackAndComplaintBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view binding
        binding = ActivityFeedbackAndComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjust padding for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle submit button click
        binding.submitButton.setOnClickListener {
            val feedback = binding.feedbackEditText.text.toString().trim()
            if (feedback.isNotEmpty()) {
                submitFeedback(feedback)
            } else {
                Toast.makeText(this, "Please enter your feedback or complaint", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitFeedback(feedback: String) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown User"
        val feedbackData = hashMapOf(
            "feedback" to feedback,
            "email" to userEmail,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("Feedback")
            .add(feedbackData)
            .addOnSuccessListener {
                Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                binding.feedbackEditText.text?.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
            }
    }

}
