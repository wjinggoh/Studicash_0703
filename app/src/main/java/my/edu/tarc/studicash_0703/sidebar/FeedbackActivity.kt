package my.edu.tarc.studicash_0703.sidebar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import my.edu.tarc.studicash_0703.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendFeedbackButton.setOnClickListener {
            Log.d("FeedbackActivity", "Send Feedback button clicked")
            sendFeedback()
        }

        binding.feedbackBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun sendFeedback() {
        val userEmail = binding.userEmailEditText.text.toString()
        val feedback = binding.feedbackEditText.text.toString()

        if (userEmail.isBlank() || feedback.isBlank()) {
            Toast.makeText(this, "Please enter both email and feedback.", Toast.LENGTH_SHORT).show()
            return
        }

        val recipientEmail = "wjingggoh15@gmail.com"
        val subject = "User Feedback"
        val emailBody = "Sender: $userEmail\n\nFeedback:\n$feedback"

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$recipientEmail")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Choose an email client:"))
        } catch (e: Exception) {
            Log.e("FeedbackActivity", "Error launching email client", e)
            Toast.makeText(this, "Error launching email client", Toast.LENGTH_SHORT).show()
        }
    }
}
