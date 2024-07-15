package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import my.edu.tarc.studicash_0703.MainActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_welcome_page)

        // Automatically navigate to MainActivity after 3 seconds
        Handler().postDelayed({
            val intent = Intent(this, my.edu.tarc.studicash_0703.MainActivity::class.java)
            startActivity(intent)
            finish() // Close this activity
        }, 3000) // 3 seconds delay
    }
}
