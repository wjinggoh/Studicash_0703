package my.edu.tarc.studicash_0703

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityAddIncomeBinding
import my.edu.tarc.studicash_0703.databinding.ActivityForgotPasswordBinding

class AddIncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_income)

    }
}