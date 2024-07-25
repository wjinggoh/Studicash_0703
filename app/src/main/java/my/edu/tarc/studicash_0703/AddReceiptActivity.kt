package my.edu.tarc.studicash_0703

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import my.edu.tarc.studicash_0703.Models.ReceiptItems
import my.edu.tarc.studicash_0703.adapter.ReceiptItemsAdapter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class AddReceiptActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var recognizeTextButton: Button
    private lateinit var itemsTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveButton: Button
    private var receiptImageBitmap: Bitmap? = null
    private val receiptItems = mutableListOf<ReceiptItems>()
    private val itemAdapter = ReceiptItemsAdapter(receiptItems)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)

        imageView = findViewById(R.id.imageView)
        selectImageButton = findViewById(R.id.selectImage)
        recognizeTextButton = findViewById(R.id.recognizeText)
        itemsTextView = findViewById(R.id.itemsTextView)
        recyclerView = findViewById(R.id.recyclerView2)
        saveButton = findViewById(R.id.saveButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = itemAdapter

        selectImageButton.setOnClickListener {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST)
        }

        recognizeTextButton.setOnClickListener {
            receiptImageBitmap?.let { bitmap ->
                val processedBitmap = convertToGrayscale(bitmap)
                    .let { applyThreshold(it) }
                    .let { resizeBitmap(it, 800, 600) }

                processedBitmap?.let {
                    recognizeTextFromImage(it) { receiptText ->
                        val itemsAndAmounts = extractItemsAndAmounts(receiptText)
                        val totalAmount = detectTotalAmount(receiptText)

                        receiptItems.clear()
                        receiptItems.addAll(itemsAndAmounts.map { ReceiptItems(it.first, it.second) })
                        receiptItems.add(ReceiptItems(itemName = "Total", itemAmount = totalAmount))
                        itemAdapter.notifyDataSetChanged()

                        itemsTextView.text = "Recognized Items (Total: $${totalAmount})"
                    }
                } ?: Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            receiptImageBitmap?.let { bitmap ->
                val processedBitmap = convertToGrayscale(bitmap)
                    .let { applyThreshold(it) }
                    .let { resizeBitmap(it, 800, 600) }

                processedBitmap?.let {
                    recognizeTextFromImage(it) { receiptText ->
                        val itemsAndAmounts = extractItemsAndAmounts(receiptText)
                        val totalAmount = detectTotalAmount(receiptText)
                        val receiptFormat = detectReceiptFormat(receiptText)

                        saveTransaction(itemsAndAmounts, totalAmount, receiptFormat)

                        val receiptDetails = mapOf(
                            "receiptFormat" to receiptFormat,
                            "totalAmount" to totalAmount,
                            "items" to itemsAndAmounts.map { mapOf("name" to it.first, "amount" to it.second) }
                        )

                        saveReceiptHistory(processedBitmap, receiptDetails)
                    }
                } ?: Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data ?: return
            receiptImageBitmap = resizeBitmap(imageUri, 800, 600)
            Glide.with(this)
                .load(imageUri)
                .override(800, 600) // Resize to desired dimensions
                .into(imageView)
        }
    }

    private fun recognizeTextFromImage(image: Bitmap, onComplete: (String) -> Unit) {
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener { firebaseVisionText ->
                onComplete(firebaseVisionText.text)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onComplete("")
            }
    }

    private fun detectReceiptFormat(receiptText: String): String {
        return when {
            receiptText.contains("Aeon", ignoreCase = true) -> "Aeon"
            receiptText.contains("Speedmart 99", ignoreCase = true) -> "Speedmart 99"
            else -> "Unknown"
        }
    }

    private fun extractItemsAndAmounts(receiptText: String): List<Pair<String, Double>> {
        val itemList = mutableListOf<Pair<String, Double>>()

        // Updated regex to capture items with the specified pattern
        val itemRegex = Regex("""(\d+x\s+)?(\d{13})\s+([\d,.]+)\s+([\w\s]+)""", RegexOption.IGNORE_CASE)

        // Find and extract items and their amounts
        itemRegex.findAll(receiptText).forEach { matchResult ->
            val itemName = matchResult.groupValues[4].trim()  // The item name is in the 4th group
            val itemAmount = matchResult.groupValues[3].replace(",", "").toDoubleOrNull() ?: 0.0  // The item price is in the 3rd group
            itemList.add(Pair(itemName, itemAmount))
        }

        // Extract total amount
        val totalAmount = detectTotalAmount(receiptText)
        if (totalAmount > 0.0) {
            itemList.add(Pair("Total", totalAmount))
        }

        return itemList
    }

    private fun detectTotalAmount(receiptText: String): Double {
        val totalRegex = Regex("""(?:Total|TOTAL|Grand\s*Total|GRAND\s*TOTAL|Amt\s*Paid|Amount\s*Paid)\s*[:\s]*([\d,.]+)""", RegexOption.IGNORE_CASE)
        val matchResult = totalRegex.find(receiptText)
        return matchResult?.groupValues?.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    }

    private fun saveTransaction(items: List<Pair<String, Double>>, totalAmount: Double, receiptFormat: String) {
        val transaction = hashMapOf(
            "items" to items.map { mapOf("name" to it.first, "amount" to it.second) },
            "totalAmount" to totalAmount,
            "receiptFormat" to receiptFormat,
            "timestamp" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance().collection("transactions").add(transaction)
    }

    private fun saveReceiptHistory(receiptImage: Bitmap, receiptDetails: Map<String, Any>) {
        val baos = ByteArrayOutputStream()
        receiptImage.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageData = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference.child("receipts/${System.currentTimeMillis()}.jpg")
        val uploadTask = storageRef.putBytes(imageData)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val receiptHistory = receiptDetails.toMutableMap()
                receiptHistory["imageUri"] = uri.toString()

                FirebaseFirestore.getInstance().collection("receipt_history").add(receiptHistory)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Receipt processed and saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        Toast.makeText(this, "Failed to save receipt history", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            Toast.makeText(this, "Failed to upload receipt image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(imageUri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val inputStream = contentResolver.openInputStream(imageUri) ?: return null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, this)
            inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }

        inputStream.close()
        return BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri), null, options)
    }

    private fun resizeBitmap(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val inSampleSize = calculateInSampleSize(width, height, reqWidth, reqHeight)

        val scaledWidth = width / inSampleSize
        val scaledHeight = height / inSampleSize
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                val gray = (red + green + blue) / 3
                grayscaleBitmap.setPixel(x, y, (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray)
            }
        }
        return grayscaleBitmap
    }

    private fun applyThreshold(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val thresholdBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val threshold = 128 // You can adjust the threshold value if needed

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (pixel shr 16) and 0xFF
                val color = if (gray < threshold) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                thresholdBitmap.setPixel(x, y, color)
            }
        }
        return thresholdBitmap
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
