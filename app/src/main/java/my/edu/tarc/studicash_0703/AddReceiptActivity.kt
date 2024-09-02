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
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import my.edu.tarc.studicash_0703.Models.Receipt
import my.edu.tarc.studicash_0703.Models.ReceiptsViewModel
import my.edu.tarc.studicash_0703.Models.SharedViewModel
import my.edu.tarc.studicash_0703.databinding.ActivityAddReceiptBinding
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID

class AddReceiptActivity : AppCompatActivity() {
    private lateinit var viewModel: SharedViewModel

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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
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
                Log.e("AddReceiptActivity", "No image URI provided")
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.receiptHistory.setOnClickListener {
            val intent = Intent(this, ReceiptHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadAction(data: Intent) {
        try {
            val stream = contentResolver.openInputStream(data.data!!)
            val resizedBitmap = getResizedBitmap(stream, 1024, 1024)
            if (::photoImage.isInitialized) photoImage.recycle()
            photoImage = resizedBitmap
            firebaseImage = FirebaseVisionImage.fromBitmap(photoImage)
            binding.imageResult.setImageBitmap(photoImage)
            imageURI = data.data
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


                val taxKeywords = listOf("TAX", "SST", "GST")
                val taxFound = taxKeywords.any { keyword -> text.contains(keyword, ignoreCase = true) }

                if (taxFound) {
                    binding.editTAX.setText(receipts.tax, TextView.BufferType.EDITABLE)
                } else {
                    binding.editTAX.setText("unapplicable", TextView.BufferType.EDITABLE)
                }
            }
            .addOnFailureListener {

                Log.e("AddReceiptActivity", "Text recognition failed")
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
                    // Create a Receipt object with the image URI and category
                    val taxValue = if (binding.editTAX.text.toString() == "Unapplicable") "" else binding.editTAX.text.toString()
                    val receipt = Receipt(
                        total = binding.editTotal.text.toString(),
                        tax = taxValue,
                        type = binding.editLocation.text.toString(),
                        userId = getCurrentUserId(), // Get the user ID
                        imageUri = downloadUri.toString(),
                        id = receiptId,
                        category = "Receipt" // Ensure category is set to "Receipt"
                    )

                    // Save receipt details to Firestore
                    firestoreRef.set(receipt).addOnSuccessListener {
                        Log.d("AddReceiptActivity", "Receipt successfully saved to Firestore with ID: $receiptId")

                        // Update the transaction with the receipt ID and category
                        updateTransactionWithReceipt(receiptId, "Receipt")

                        // Reset UI elements
                        resetUI()
                    }.addOnFailureListener { e ->
                        Log.e("AddReceiptActivity", "Error saving receipt to Firestore: ${e.message}")
                    }
                }.addOnFailureListener { e ->
                    Log.e("AddReceiptActivity", "Error getting download URL: ${e.message}")
                }
            }.addOnFailureListener { e ->
                Log.e("AddReceiptActivity", "Error uploading image to Firebase Storage: ${e.message}")
            }
        } ?: run {
            Log.e("AddReceiptActivity", "No image URI provided")
        }
    }


    private fun updateTransactionWithReceipt(receiptId: String, category: String) {
        val transactionId = getTransactionId()
        Log.d("AddReceiptActivity", "Transaction ID: $transactionId")

        if (transactionId.isNotEmpty()) {
            val transactionRef = firestoreReference.collection("expenseTransactions").document(transactionId)

            transactionRef.update(
                mapOf(
                    "receiptId" to receiptId,
                    "category" to category
                )
            )
                .addOnSuccessListener {
                    Log.d("AddReceiptActivity", "Transaction successfully updated with receipt ID: $receiptId and category: $category")
                }
                .addOnFailureListener { e ->
                    Log.e("AddReceiptActivity", "Error updating transaction with receipt ID and category: ${e.message}")
                }
        } else {
            Log.e("AddReceiptActivity", "Transaction ID is empty, cannot update transaction")
        }
    }






    private fun getTransactionId(): String {
        return viewModel.getTransactionId() ?: ""
    }




    private fun getCurrentUserId(): String {
        val firebaseAuth = FirebaseAuth.getInstance()
        return firebaseAuth.currentUser?.uid ?: "default_user_id" // Handle case where user is not signed in
    }

    private fun resetUI() {
        // Clear the image view
        binding.imageResult.setImageDrawable(null)
        // Clear the text fields
        binding.editTotal.text.clear()
        binding.editTAX.text.clear()
        binding.editLocation.text.clear()
        // Reset the imageURI
        imageURI = null
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

    private fun getResizedBitmap(inputStream: InputStream?, maxWidth: Int, maxHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        // Read the InputStream into a byte array
        val byteArray = inputStream?.readBytes() ?: throw IllegalArgumentException("Input stream is null")

        // Create a ByteArrayInputStream for decoding the bounds
        val byteArrayInputStream1 = ByteArrayInputStream(byteArray)
        BitmapFactory.decodeStream(byteArrayInputStream1, null, options)
        val imageWidth = options.outWidth
        val imageHeight = options.outHeight

        var scaleFactor = 1
        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            val halfWidth = imageWidth / 2
            val halfHeight = imageHeight / 2

            while ((halfWidth / scaleFactor) >= maxWidth && (halfHeight / scaleFactor) >= maxHeight) {
                scaleFactor *= 2
            }
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor

        // Create another ByteArrayInputStream for decoding the actual bitmap
        val byteArrayInputStream2 = ByteArrayInputStream(byteArray)
        return BitmapFactory.decodeStream(byteArrayInputStream2, null, options) ?: throw IllegalArgumentException("Failed to decode bitmap")
    }



}
