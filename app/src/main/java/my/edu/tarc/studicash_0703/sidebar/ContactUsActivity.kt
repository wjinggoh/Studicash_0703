package my.edu.tarc.studicash_0703.sidebar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import my.edu.tarc.studicash_0703.databinding.ActivityContactUsBinding

class ContactUsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contactUsBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.ContactNumberBtn.setOnClickListener {
            val phoneNumber = "+601111155578"  // The phone number you want to call
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(callIntent)
        }

        binding.ContactEmailBtn.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        val email = "wjingggoh15@gmail.com"
        val subject = "Hi Studicash Team,"
        val emailBody = "Body of the email"

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Log.e("ContactUsActivity", "Error launching email client", e)
            Toast.makeText(this, "Error launching email client", Toast.LENGTH_SHORT).show()
        }
    }
}
