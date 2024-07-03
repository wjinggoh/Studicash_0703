package my.edu.tarc.studicash_0703

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import my.edu.tarc.studicash_0703.databinding.ActivityRegisterAccountBinding
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import my.edu.tarc.studicash_0703.utils.USER_NODE
import my.edu.tarc.studicash_0703.utils.USER_PROFILE_FOLDER
import my.edu.tarc.studicash_0703.Models.User
import java.io.File
import java.io.IOException

class RegisterAccount : AppCompatActivity() {

    private var fileUri: Uri? = null


    val binding by lazy {
        ActivityRegisterAccountBinding.inflate(layoutInflater)
    }

    lateinit var user: my.edu.tarc.studicash_0703.Models.User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

            val text =
                "<font color=#FF000000>Already have an account</font> <font color=#00C9B8>Login ?</font>"
            binding.backSignIn.setText(Html.fromHtml(text))
            user = User()

            binding.uploadStudentIdPhoto.setOnClickListener {
                cameraCheckPermission()
            }


            binding.backSignIn.setOnClickListener {
                startActivity(Intent(this@RegisterAccount, MainActivity::class.java))
                finish()
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
                        "s",
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
                                        ContentValues.TAG,
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

    private fun cameraCheckPermission(){

        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(

                    object:MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let{

                                if(report.areAllPermissionsGranted()){
                                    camera()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                        ) {
                           showRorationalDialogForPermission()
                        }

                    }
                ).onSameThread().check()
    }

    private fun camera(){
        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,CAMERA_REQUEST_CODE)
    }

    private fun showRorationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions"
            +"required for this feature. It can be enable under App settings.")
            .setPositiveButton("GO TO SETTINGS"){_,_->

                try{
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri=Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)


                }catch(e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }

            .setNegativeButton("CANCEL"){dialog,_->
                dialog.dismiss()
            }.show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}