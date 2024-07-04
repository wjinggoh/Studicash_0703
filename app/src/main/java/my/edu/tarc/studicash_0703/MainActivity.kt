package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)


        binding.loginButton.setOnClickListener{
            val email=binding.email.editText?.text.toString()
            val password=binding.password.editText?.text.toString()

            if(email.isEmpty()||password.isEmpty()){
                    Toast.makeText(this@MainActivity,"Please fill in all fields",Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener{task->
                        if(task.isSuccessful){
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish() // Optionally call finish() to close the current activity
                        }else{
                            Toast.makeText(this@MainActivity,"Authentication failed:${task.exception?.localizedMessage}",
                                Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
            }
        }

        binding.createAnAccount.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterAccount::class.java))
            finish()
        }

        binding.forgotPage.setOnClickListener {
            startActivity(Intent(this@MainActivity, forgotPasswordActivity::class.java))
        }

    }
}