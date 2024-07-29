package my.edu.tarc.studicash_0703

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import my.edu.tarc.studicash_0703.Models.Receipt
import my.edu.tarc.studicash_0703.Models.ReceiptsViewModel
import my.edu.tarc.studicash_0703.databinding.ActivityAddReceiptBinding
import java.io.FileNotFoundException

class AddReceiptActivity : AppCompatActivity() {

    private val UPLOAD_ACTION = 2001
    private val PERMISSION_ACTION = 2002
    private val CAMERA_ACTION = 2003
    private lateinit var photoImage: Bitmap
    private lateinit var firebaseImage: FirebaseVisionImage
    private lateinit var binding: ActivityAddReceiptBinding
    private lateinit var receiptsViewModel: ReceiptsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the ViewModel
        receiptsViewModel = ViewModelProvider(this).get(ReceiptsViewModel::class.java)

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
    }

    private fun uploadAction(data: Intent) {
        try {
            val stream = contentResolver.openInputStream(data.data!!)
            photoImage = BitmapFactory.decodeStream(stream)
            firebaseImage = FirebaseVisionImage.fromBitmap(photoImage)
            binding.imageResult.setImageBitmap(photoImage)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun cameraAction() {
        try {
            Glide.with(this).load(receiptsViewModel.imageURI).into(binding.imageResult)
            firebaseImage = FirebaseVisionImage.fromFilePath(this, receiptsViewModel.imageURI)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun textRecognitionAction() {
        var text = ""
        receiptsViewModel.textDeviceDetector.processImage(firebaseImage)
            .addOnSuccessListener { result ->
                for (block in result.textBlocks) {
                    text += block.text + "\n"
                }
                val receipts = receiptsViewModel.getReceipts(text)
                uploadFirebase(receipts)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_ACTION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                UPLOAD_ACTION -> {
                    uploadAction(data!!)
                }
                CAMERA_ACTION -> {
                    cameraAction()
                }
            }
            textRecognitionAction()
        }
    }

    private fun uploadFirebase(receipt: Receipt) {
        receiptsViewModel.getReceiptsImageURL(this, photoImage).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val imageUrl = task.result
                val receiptData = receiptsViewModel.getMapReceipts(receipt).toMutableMap()
                imageUrl?.let {
                    receiptData["imageUrl"] = it.toString() // Add image URL to the receipt data
                }
                FirebaseFirestore.getInstance().collection("Receipt")
                    .add(receiptData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Upload Successful", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Image URL Fetch Failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}
