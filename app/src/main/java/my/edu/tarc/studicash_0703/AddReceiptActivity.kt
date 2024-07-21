package my.edu.tarc.studicash_0703

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import my.edu.tarc.studicash_0703.Models.ReceiptItems
import my.edu.tarc.studicash_0703.adapter.ReceiptItemsAdapter
import java.io.ByteArrayOutputStream
import java.io.InputStream

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
            // Launch image picker
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST)
        }

        recognizeTextButton.setOnClickListener {
            receiptImageBitmap?.let {
                recognizeTextFromImage(it) { receiptText ->
                    val receiptFormat = detectReceiptFormat(receiptText)
                    val itemsAndAmounts = extractItemsAndAmounts(receiptText)
                    val totalAmount = detectTotalAmount(receiptText)

                    receiptItems.clear()
                    receiptItems.addAll(itemsAndAmounts.map { ReceiptItems(it.first, it.second) })
                    itemAdapter.notifyDataSetChanged()

                    itemsTextView.text = "Recognized Items (Total: $${totalAmount})"

                    // Optionally, display recognized text or other info
                }
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            receiptImageBitmap?.let {
                recognizeTextFromImage(it) { receiptText ->
                    val receiptFormat = detectReceiptFormat(receiptText)
                    val itemsAndAmounts = extractItemsAndAmounts(receiptText)
                    val totalAmount = detectTotalAmount(receiptText)

                    saveTransaction(itemsAndAmounts, totalAmount, receiptFormat)

                    val receiptDetails = mapOf(
                        "receiptFormat" to receiptFormat,
                        "totalAmount" to totalAmount,
                        "items" to itemsAndAmounts
                    )

                    saveReceiptHistory(it, receiptDetails)
                }
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data ?: return
            val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
            receiptImageBitmap = BitmapFactory.decodeStream(imageStream)
            imageView.setImageBitmap(receiptImageBitmap)
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
        val itemRegex = Regex("""(\w+.*)\s+(\d+\.\d{2})""")

        itemRegex.findAll(receiptText).forEach { matchResult ->
            val itemName = matchResult.groupValues[1]
            val itemAmount = matchResult.groupValues[2].toDouble()
            itemList.add(Pair(itemName, itemAmount))
        }

        return itemList
    }

    private fun detectTotalAmount(receiptText: String): Double {
        val totalRegex = Regex("""Total\s*:\s*(\d+\.\d{2})""")
        val matchResult = totalRegex.find(receiptText)
        return matchResult?.groupValues?.get(1)?.toDouble() ?: 0.0
    }

    private fun categorizeItem(itemName: String): String {
        val categoryMap = mapOf(
            "milk" to "Groceries",
            "bread" to "Groceries",
            "detergent" to "Household",
            // Add more mappings as needed
        )

        return categoryMap.entries.find { itemName.contains(it.key, ignoreCase = true) }?.value ?: "Miscellaneous"
    }

    private fun saveTransaction(items: List<Pair<String, Double>>, totalAmount: Double, receiptFormat: String) {
        val transaction = hashMapOf(
            "items" to items.map { mapOf("name" to it.first, "amount" to it.second, "category" to categorizeItem(it.first)) },
            "totalAmount" to totalAmount,
            "receiptFormat" to receiptFormat,
            "timestamp" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance().collection("transactions").add(transaction)
    }

    private fun saveReceiptHistory(receiptImage: Bitmap, receiptDetails: Map<String, Any>) {
        // Convert receiptImage to byte array
        val baos = ByteArrayOutputStream()
        receiptImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Upload image to Firebase Storage
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

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
