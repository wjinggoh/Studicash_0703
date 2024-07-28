package my.edu.tarc.studicash_0703.Models

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReceiptsViewModel @Inject constructor() {
    val textDeviceDetector: FirebaseVisionTextRecognizer
    lateinit var imageURI: Uri

    init {
        textDeviceDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer()
    }

    fun uploadIntent(): Intent {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    fun cameraIntent(context: Context): Intent {
        val photoURI: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            createImageFile(context) // Use the method to create a valid File
        )
        imageURI = photoURI // Initialize imageURI here
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName =  "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun getReceipts(text: String): Receipt {
        val originalResult = text.findFloat()
        if (originalResult.isEmpty()) return Receipt()
        else {
            val receipts = Receipt()
            val totalF = Collections.max(originalResult)
            val secondLargestF = findSecondLargestFloat(originalResult)
            receipts.total = totalF.toString()
            receipts.tax = if (secondLargestF == 0.0f) "0" else "%.2f".format(totalF - secondLargestF)
            receipts.type = text.firstLine()
            return receipts
        }
    }

    private fun findSecondLargestFloat(input: ArrayList<Float>?): Float {
        if (input == null || input.isEmpty() || input.size == 1) return 0.0f
        else {
            try {
                val tempSet = HashSet(input)
                val sortedSet = TreeSet(tempSet)
                return sortedSet.elementAt(sortedSet.size - 2)
            } catch (e: Exception) {
                return 0.0f
            }
        }
    }

    fun getReceiptsImageURL(context: Context, image: Bitmap): Task<Uri> {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("receipts/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = imageRef.putBytes(data)
        return uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }
    }

    fun getMapReceipts(receipt: Receipt): Map<String, Any> {
        return mapOf(
            "total" to receipt.total,
            "tax" to receipt.tax,
            "type" to receipt.type
        )
    }
}
