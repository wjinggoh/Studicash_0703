package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityForgotPasswordBinding
import my.edu.tarc.studicash_0703.databinding.ActivityRegisterAccountBinding

class forgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.forgotButton.setOnClickListener {
            val email = binding.email.editText?.text.toString().trim()

            if (isValidEmail(email)) {
                resetPassword(email)
            } else {
                showToast("Please enter a valid email")
            }
        }

        binding.back.setOnClickListener {
            val intent = Intent(this@forgotPasswordActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Optionally call finish() to close the current activity
        }

    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun resetPassword(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password reset email sent successfully")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val e = task.exception
                    val errorMessage = e?.localizedMessage ?: "Failed to send password reset email. Please try again later."
                    showToast(errorMessage)
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}