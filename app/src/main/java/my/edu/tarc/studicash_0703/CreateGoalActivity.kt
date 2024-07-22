package my.edu.tarc.studicash_0703

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.Models.IncomeCategory
import my.edu.tarc.studicash_0703.databinding.ActivityCreateBudgetBinding
import my.edu.tarc.studicash_0703.databinding.ActivityCreateGoalBinding
import java.util.Calendar

class CreateGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGoalBinding
    private val calendar = Calendar.getInstance()
    private var incomeCategories = listOf<IncomeCategory>()
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()


    }
}