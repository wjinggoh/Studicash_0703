package my.edu.tarc.studicash_0703

import android.Manifest
import androidx.core.text.HtmlCompat
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import my.edu.tarc.studicash_0703.Models.User
import my.edu.tarc.studicash_0703.databinding.ActivityRegisterAccountBinding
import my.edu.tarc.studicash_0703.utils.USER_NODE
import my.edu.tarc.studicash_0703.utils.USER_PROFILE_FOLDER
import my.edu.tarc.studicash_0703.utils.uploadImage
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.messaging.FirebaseMessaging
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class RegisterAccount : AppCompatActivity() {

    private var fileUri: Uri? = null

    val binding by lazy {
        ActivityRegisterAccountBinding.inflate(layoutInflater)
    }

    lateinit var user:User


    private fun uploadFile(fileUri: Uri) {
        val storageRef = Firebase.storage.reference.child("your/storage/path/${fileUri.lastPathSegment}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uploadTask = storageRef.putFile(fileUri)

                // Register observers to listen for upload progress, success, or failure
                uploadTask.addOnProgressListener {
                    // Handle upload progress updates
                }.addOnSuccessListener {
                    // Upload completed successfully
                }.addOnFailureListener { exception ->
                    // Handle upload failure, including the "server terminated session" error
                    if (exception is IOException && exception.message?.contains("The server has terminated the upload session") == true) {
                        // Implement retry logic with exponential backoff
                    } else {
                        // Handle other upload errors
                    }
                }

                // Optionally, use await() to suspend the coroutine until the upload completes
                val uploadResult = uploadTask.await()
            } catch (e: Exception) {
                // Handle exceptions during upload
                e.printStackTrace()
            }
        }
    }





    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            fileUri?.let { uri ->
                detectFace(uri) { faceDetected ->
                    if (faceDetected) {
                        uploadFile(uri) // Replace with the new function
                        user.image = uri.toString()
                        binding.profileImage.setImageURI(uri)
                    } else {
                        Toast.makeText(this, "No face detected, please take a valid photo with a face.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            detectFace(uri) { faceDetected ->
                if (faceDetected) {
                    uploadFile(uri) // Replace with the new function
                    user.image = uri.toString()
                    binding.profileImage.setImageURI(uri)
                } else {
                    Toast.makeText(this, "No face detected, please upload a valid photo with a face.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request camera permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Camera permission granted, continue with existing initialization
            val text =
                "<font color=#FF000000>Already have an account</font> <font color=#00C9B8>Login ?</font>"
            binding.backSignIn.setText(Html.fromHtml(text))
            user = User()
            if (intent.hasExtra("MODE")) {
                if (intent.getIntExtra("MODE", -1) == 1) {
                    binding.registerButton.text = "Update Profile"
                    Firebase.firestore.collection(USER_NODE)
                        .document(Firebase.auth.currentUser!!.uid)
                        .get()
                        .addOnSuccessListener {
                            user = it.toObject<User>()!!
                            if (!user.image.isNullOrEmpty()) {
                                Picasso.get().load(user.image).into(binding.profileImage)
                            }
                            binding.userName.editText?.setText(user.name)
                            binding.email.editText?.setText(user.email)
                            binding.password.editText?.setText(user.password)
                            binding.gender.editText?.setText(user.gender)
                        }
                }
            }

            binding.profileImage.setOnClickListener {
                fileUri = createImageFileUri()
                fileUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            }

            binding.uploadStudentIdPhoto.setOnClickListener(){
                launcher.launch("image/*")
            }

            binding.backSignIn.setOnClickListener {
                val intent = Intent(this@RegisterAccount, MainActivity::class.java)
                startActivity(intent)
                finish() // Optionally call finish() to close the current activity
            }

            binding.registerButton.setOnClickListener {
                val email = binding.email.editText?.text.toString().trim()
                val password = binding.password.editText?.text.toString().trim()
                val confirmPassword = binding.confirmPassword.editText?.text.toString().trim()
                val gender = binding.gender.editText?.text.toString().trim()
                val name = binding.userName.editText?.text.toString().trim()

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(
                        this@RegisterAccount,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Validate profile image
                if (user.image == null) {
                    Toast.makeText(
                        this@RegisterAccount,
                        "Please upload your profile image",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val tarucEmailPattern = "[a-zA-Z0-9._%+-]+@student\\.tarc\\.edu\\.my"

                // Email validation
                if (email.isEmpty() || !email.matches(tarucEmailPattern.toRegex())) {
                    Toast.makeText(
                        this@RegisterAccount,
                        "Email must follow the TARUMT email format",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Password validation
                if (password.isEmpty() || password.length < 6) {
                    Toast.makeText(
                        this@RegisterAccount,
                        "Password must be at least 6 characters long",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Confirm password validation
                if (confirmPassword.isEmpty() || confirmPassword != password) {
                    Toast.makeText(
                        this@RegisterAccount,
                        "Confirm password is not match with the password",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                if (!(gender.equals("Male", ignoreCase = true) || gender.equals(
                        "Female",
                        ignoreCase = true
                    ))
                ) {
                    Toast.makeText(
                        this@RegisterAccount,
                        "Please type \"Male\" or \"Female\"",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // If all validations pass, proceed with account creation
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.email.editText?.text.toString(),
                    binding.password.editText?.text.toString()
                ).addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        if (firebaseUser != null) {
                            user.uid = firebaseUser.uid
                            user.name = binding.userName.editText?.text.toString()
                            user.email = binding.email.editText?.text.toString()
                            user.password = binding.password.editText?.text.toString()
                            user.gender = binding.gender.editText?.text.toString()

                            firebaseUser.sendEmailVerification()
                                .addOnCompleteListener { emailVerificationTask ->
                                    if (emailVerificationTask.isSuccessful) {
                                        // Email sent, wait for user to verify
                                        Toast.makeText(
                                            this@RegisterAccount,
                                            "Verification email sent. Please verify your email to continue.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        // Failed to send verification email
                                        Toast.makeText(
                                            this@RegisterAccount,
                                            "Failed to send verification email",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    user.fcmToken = token

                                    Firebase.firestore.collection(USER_NODE)
                                        .document(firebaseUser.uid)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@RegisterAccount,
                                                "Register Successful",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            startActivity(
                                                Intent(
                                                    this@RegisterAccount,
                                                    HomeActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                this@RegisterAccount,
                                                "Failed to save user data: ${e.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Log.e(
                                        TAG,
                                        "Failed to retrieve FCM token: ${task.exception?.localizedMessage}"
                                    )
                                    Toast.makeText(
                                        this@RegisterAccount,
                                        "Failed to retrieve FCM token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@RegisterAccount,
                                "Failed to retrieve current user",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@RegisterAccount,
                            result.exception?.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun createImageFileUri(): Uri {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("selfie_", ".jpg", storageDir)
        return FileProvider.getUriForFile(this, "my.edu.tarc.studicash_0703.fileprovider", file)
    }

    private fun detectFace(uri: Uri, onDetectionComplete: (Boolean) -> Unit) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, uri)
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val detector = FaceDetection.getClient(options)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    onDetectionComplete(faces.isNotEmpty())
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Face detection failed: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDetectionComplete(false)
                }
        } catch (e: IOException) {
            e.printStackTrace()
            onDetectionComplete(false)
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted camera permission, continue with initialization
                // (Already handled in onCreate)
            } else {
                // User denied camera permission, you can show a message or continue with other actions
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                // You may choose to close the app or continue with other actions
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}

