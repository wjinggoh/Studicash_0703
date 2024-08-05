package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import my.edu.tarc.studicash_0703.Object.AdminSession
import my.edu.tarc.studicash_0703.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view binding
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjust padding for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if admin is logged in
        if (!AdminSession.isLoggedIn) {
            // Redirect to login activity if not logged in
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle Logout button click
        binding.LogoutBtn.setOnClickListener {
            // Confirm admin cannot log out
            Toast.makeText(this, "Admin cannot log out.", Toast.LENGTH_SHORT).show()
            // Optionally, you can disable or hide the logout button
            binding.LogoutBtn.isEnabled = false
        }


        binding.FeedbackComplaintReceiveBtn.setOnClickListener {
            val intent = Intent(this, FeedbackComplaintReceiveActivity::class.java)
            startActivity(intent)
        }
    }
}


