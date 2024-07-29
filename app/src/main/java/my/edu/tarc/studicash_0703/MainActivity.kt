package my.edu.tarc.studicash_0703

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.Worker.TransactionWorker
import my.edu.tarc.studicash_0703.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        // Check if the user is already logged in
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startActivity(Intent(this@MainActivity, my.edu.tarc.studicash_0703.HomeActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.email.editText?.text.toString()
            val password = binding.password.editText?.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Save login state
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true)
                            editor.apply()

                            val intent = Intent(this@MainActivity, my.edu.tarc.studicash_0703.HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "Authentication failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.createAnAccount.setOnClickListener {
            startActivity(Intent(this@MainActivity, my.edu.tarc.studicash_0703.RegisterAccountActivity::class.java))
            finish()
        }

        binding.forgotPage.setOnClickListener {
            startActivity(Intent(this@MainActivity, my.edu.tarc.studicash_0703.forgotPasswordActivity::class.java))
        }

        scheduleTransactionWorker()
    }

    private fun scheduleTransactionWorker() {
        // Set up the input data for the worker
        val inputData = workDataOf(
            "goalName" to "Savings Goal",
            "savingsNeeded" to 50.0, // Change as needed
            "timestamp" to System.currentTimeMillis()
        )

        // Create a PeriodicWorkRequest
        val transactionWorkRequest = PeriodicWorkRequestBuilder<TransactionWorker>(1, TimeUnit.DAYS)
            .setInputData(inputData)
            .build()

        // Enqueue the worker
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TransactionWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            transactionWorkRequest
        )
    }
}
