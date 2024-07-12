package my.edu.tarc.studicash_0703

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import my.edu.tarc.studicash_0703.databinding.ActivityAddTransactionBinding
import my.edu.tarc.studicash_0703.databinding.ActivityGoalTrackingBinding

class GoalTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGoalTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.GoalBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

}