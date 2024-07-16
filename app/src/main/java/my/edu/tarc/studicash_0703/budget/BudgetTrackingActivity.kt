package my.edu.tarc.studicash_0703.budget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import my.edu.tarc.studicash_0703.databinding.ActivityBudgetTrackingBinding

class BudgetTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BudgetBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addBudgetBtn.setOnClickListener {
            val intent = Intent(this, AddBudgetActivity::class.java) // Use 'this' instead of requireContext()
            startActivity(intent)
        }
    }
}
