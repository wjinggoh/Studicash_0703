package my.edu.tarc.studicash_0703.Fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.AddPaymentMethodActivity
import my.edu.tarc.studicash_0703.sidebar.HelpActivity
import my.edu.tarc.studicash_0703.MainActivity
import my.edu.tarc.studicash_0703.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.logoutText.setOnClickListener {
            logout()
        }

        binding.addPaymentMethodBtn.setOnClickListener {
            val intent = Intent(activity, AddPaymentMethodActivity::class.java)
            startActivity(intent)
        }

        binding.helpBtn.setOnClickListener{
            val intent=Intent(activity, HelpActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun logout() {
        // Clear login state
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to MainActivity and clear task stack
        val intent = Intent(activity, my.edu.tarc.studicash_0703.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
