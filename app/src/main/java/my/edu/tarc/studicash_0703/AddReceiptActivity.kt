package my.edu.tarc.studicash_0703

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.squareup.picasso.Picasso
import my.edu.tarc.studicash_0703.Models.ReceiptsViewModel
import my.edu.tarc.studicash_0703.databinding.ActivityAddReceiptBinding
import java.io.FileNotFoundException
import java.util.UUID

class AddReceiptActivity : AppCompatActivity() {

    private val UPLOAD_ACTION = 2001
    private val PERMISSION_ACTION = 2002
    private val CAMERA_ACTION = 2003
    private lateinit var photoImage: Bitmap
    private lateinit var firebaseImage: FirebaseVisionImage
    private var imageURI: Uri? = null

    private lateinit var receiptsViewModel: ReceiptsViewModel
    private lateinit var binding: ActivityAddReceiptBinding

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val firestoreReference: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel manually
        receiptsViewModel = ReceiptsViewModel() // Initialize as needed

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_ACTION)
        }

        setUI()
    }

    private fun setUI() {
        binding.txtUpload.setOnClickListener {
            startActivityForResult(receiptsViewModel.uploadIntent(), UPLOAD_ACTION)
        }

        binding.txtCamera.setOnClickListener {
            startActivityForResult(receiptsViewModel.cameraIntent(this), CAMERA_ACTION)
        }

        // Handle back button click
        binding.receiptBackBtn.setOnClickListener {
            onBackPressed()
        }

        // Handle Save button click
        binding.btnConfirm.setOnClickListener {
            val total = binding.editTotal.text.toString()
            val tax = binding.editTAX.text.toString()
            val type = binding.editLocation.text.toString()
            // Save receipt to database
            if (imageURI != null) {
                saveReceiptToDatabase()
            } else {
                // Handle case where no image is selected
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun uploadAction(data: Intent) {
        try {
            val stream = contentResolver.openInputStream(data.data!!)
            if (::photoImage.isInitialized) photoImage.recycle()
            photoImage = BitmapFactory.decodeStream(stream)
            firebaseImage = FirebaseVisionImage.fromBitmap(photoImage)
            binding.imageResult.setImageBitmap(photoImage)
            imageURI = data.data // Store the image URI
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun cameraAction() {
        try {
            Picasso.get().load(receiptsViewModel.imageURI).into(binding.imageResult)
            firebaseImage = FirebaseVisionImage.fromFilePath(this, receiptsViewModel.imageURI!!)
            imageURI = receiptsViewModel.imageURI // Store the image URI
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun textRecognitionAction() {
        var text = ""
        receiptsViewModel.textDeviceDetector.processImage(firebaseImage)
            .addOnSuccessListener {
                for (block in it.textBlocks) text += block.text + "\n"
                val receipts = receiptsViewModel.getReceipts(text)
                binding.editTotal.setText(receipts.total, TextView.BufferType.EDITABLE)
                binding.editLocation.setText(receipts.type, TextView.BufferType.EDITABLE)
                binding.editTAX.setText(receipts.tax, TextView.BufferType.EDITABLE)
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun saveReceiptToDatabase() {
        // Generate a unique ID for the receipt
        val receiptId = UUID.randomUUID().toString()
        val storageRef = storageReference.child("receipts/$receiptId.jpg")
        val firestoreRef = firestoreReference.collection("Receipt").document(receiptId)

        // Upload image to Firebase Storage
        imageURI?.let { uri ->
            val uploadTask = storageRef.putFile(uri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val receipt = hashMapOf(
                        "total" to binding.editTotal.text.toString(),
                        "tax" to binding.editTAX.text.toString(),
                        "location" to binding.editLocation.text.toString(),
                        "imageUrl" to downloadUri.toString()
                    )

                    // Save receipt details to Firestore
                    firestoreRef.set(receipt).addOnSuccessListener {
                        // Data saved successfully
                        Log.d("AddReceiptActivity", "Receipt successfully saved to Firestore with ID: $receiptId")
                        finish()
                    }.addOnFailureListener { e ->
                        // Handle failure
                        Log.e("AddReceiptActivity", "Error saving receipt to Firestore: ${e.message}")
                    }
                }.addOnFailureListener { e ->
                    // Handle failure
                    Log.e("AddReceiptActivity", "Error getting download URL: ${e.message}")
                }
            }.addOnFailureListener { e ->
                // Handle failure
                Log.e("AddReceiptActivity", "Error uploading image to Firebase Storage: ${e.message}")
            }
        } ?: run {
            // Handle case where imageURI is null
            Log.e("AddReceiptActivity", "No image URI provided")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                UPLOAD_ACTION -> uploadAction(data!!)
                CAMERA_ACTION -> cameraAction()
            }
            textRecognitionAction()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ACTION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                binding.imageResult.isEnabled = true
            }
        }
    }
}
