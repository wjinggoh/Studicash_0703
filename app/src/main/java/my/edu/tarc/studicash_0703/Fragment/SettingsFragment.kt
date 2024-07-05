package my.edu.tarc.studicash_0703.Fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.MainActivity
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private lateinit var logoutTextView: TextView
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        logoutTextView = view.findViewById(R.id.logout_text)

        logoutTextView.setOnClickListener {
            logout()
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
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
