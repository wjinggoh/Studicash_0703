package my.edu.tarc.studicash_0703.sidebar

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
            if (isGmailInstalled()) {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:wjingggoh15@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Hi Studicash Team,")
                    putExtra(Intent.EXTRA_TEXT, "Body of the email")
                }
                startActivity(emailIntent)
            } else {
                Toast.makeText(this, "Gmail app is not installed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isGmailInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo("com.google.android.gm", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
