package my.edu.tarc.studicash_0703

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import my.edu.tarc.studicash_0703.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.editProfileBack.setOnClickListener {
            onBackPressed()
        }

        retrieveUserData()

        // Set click listener for the save button to update profile
        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun retrieveUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val docRef = db.collection("User").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("name")
                        val email = document.getString("email")
                        val profileImageUrl = document.getString("image")

                        binding.etUsername.setText(username)
                        binding.etEmail.setText(email)
                        profileImageUrl?.let {
                            Picasso.get().load(it).into(binding.ivProfilePicture)
                        }
                    } else {
                        Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val newPassword = binding.etPassword.text.toString()

            val userUpdates = mutableMapOf<String, Any>()
            if (username.isNotEmpty()) userUpdates["name"] = username
            if (email.isNotEmpty()) userUpdates["email"] = email

            saveUserUpdates(userId, userUpdates, newPassword)
        }
    }


    private fun saveUserUpdates(userId: String, userUpdates: Map<String, Any>, newPassword: String) {
        if (userUpdates.isNotEmpty()) {
            db.collection("User").document(userId)
                .update(userUpdates)
                .addOnSuccessListener {
                    if (newPassword.isNotEmpty()) {
                        updatePassword(newPassword)
                    } else {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            if (newPassword.isNotEmpty()) {
                updatePassword(newPassword)
            } else {
                Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        val currentUser = auth.currentUser
        currentUser?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile and password updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error updating password: ${task.exception}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
