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

    private var selectedImageUri: Uri? = null

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

        binding.ivProfilePicture.setOnClickListener {
            pickImageFromGallery()
        }

        // Retrieve user data
        retrieveUserData()

        // Set click listener for the save button to update profile
        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.ivProfilePicture.setImageURI(selectedImageUri)
        }
    }

    private fun retrieveUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val docRef = db.collection("users").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        val email = document.getString("email")
                        val profileImageUrl = document.getString("profileImageUrl")

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

            val userUpdates = hashMapOf(
                "username" to username,
                "email" to email
            )

            if (selectedImageUri != null) {
                val storageRef = storage.reference.child("profileImages/$userId.jpg")
                storageRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            userUpdates["profileImageUrl"] = uri.toString()
                            saveUserUpdates(userId, userUpdates, newPassword)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error uploading profile image: $e", Toast.LENGTH_SHORT).show()
                    }
            } else {
                saveUserUpdates(userId, userUpdates, newPassword)
            }
        }
    }

    private fun saveUserUpdates(userId: String, userUpdates: Map<String, Any>, newPassword: String) {
        db.collection("users").document(userId)
            .set(userUpdates)
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
