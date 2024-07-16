package my.edu.tarc.studicash_0703

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import my.edu.tarc.studicash_0703.adapter.ReceiptItemsAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityAddReceiptBinding
import my.edu.tarc.studicash_0703.Models.ReceiptItems
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddReceiptActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddReceiptBinding
    private val REQUEST_IMAGE_SELECT = 1
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedImageBitmap: Bitmap? = null
    private val receiptItems = mutableListOf<ReceiptItems>()
    private lateinit var adapter: ReceiptItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.selectImage.setOnClickListener { selectImage() }
        binding.recognizeText.setOnClickListener { recognizeTextFromImage() }
        binding.saveButton.setOnClickListener { saveReceipt() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ReceiptItemsAdapter(receiptItems)
        binding.recyclerView2.layoutManager = LinearLayoutManager(this)
        binding.recyclerView2.adapter = adapter
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    val bitmap = loadImageBitmap(imageUri)
                    if (bitmap != null) {
                        if (isReceiptImage(bitmap)) {
                            selectedImageBitmap = bitmap
                            binding.imageView.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(this@AddReceiptActivity, "Only receipt images are allowed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadImageBitmap(uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddReceiptActivity, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
                null
            }
        }
    }

    private fun isReceiptImage(bitmap: Bitmap): Boolean {
        // Check image dimensions
        val width = bitmap.width
        val height = bitmap.height

        // Check aspect ratio or size
        val aspectRatio = width.toFloat() / height.toFloat()
        if (aspectRatio < 0.5 || aspectRatio > 2.0) {
            return false  // Example: Only accept images with a certain aspect ratio
        }

        // Check average color intensity or presence of specific features
        val pixelThreshold = 0.1  // Example threshold for pixel intensity

        var totalIntensity = 0.0
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                // Example: Calculate intensity (greyscale)
                val intensity = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0
                totalIntensity += intensity
            }
        }

        val averageIntensity = totalIntensity / (width * height)
        return averageIntensity < pixelThreshold  // Example: Accept images with low average intensity (potentially receipt-like)
    }

    private fun recognizeTextFromImage() {
        val bitmap = selectedImageBitmap
        if (bitmap != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val recognizedText = recognizeText(bitmap)
                recognizedText?.let {
                    processExtractedText(it)
                } ?: Toast.makeText(this@AddReceiptActivity, "Text recognition failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun recognizeText(bitmap: Bitmap): com.google.mlkit.vision.text.Text? {
        return withContext(Dispatchers.IO) {
            val image = InputImage.fromBitmap(bitmap, 0)
            val options = TextRecognizerOptions.Builder().build()
            val recognizer = TextRecognition.getClient(options)
            try {
                recognizer.process(image).await()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddReceiptActivity, "Text recognition failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
                null
            }
        }
    }

    private fun processExtractedText(visionText: com.google.mlkit.vision.text.Text) {
        val resultText = visionText.text
        binding.itemsTextView.text = resultText

        // Extract the items and amounts from the recognized text
        val itemsAndAmounts = extractItemsAndAmounts(resultText)
        if (itemsAndAmounts.isNotEmpty()) {
            receiptItems.clear()
            receiptItems.addAll(itemsAndAmounts)
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "No items or amounts detected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractItemsAndAmounts(text: String): List<ReceiptItems> {
        val items = mutableListOf<ReceiptItems>()
        val lines = text.split("\n")
        var currentItem: ReceiptItems? = null

        for (line in lines) {
            when {
                line.matches(Regex("\\d{13}")) -> {
                    currentItem?.let { items.add(it) }
                    currentItem = ReceiptItems(itemName = "", itemAmount = 0.0)
                }
                currentItem?.itemName.isNullOrEmpty() -> {
                    currentItem = currentItem!!.copy(itemName = line.trim())
                }
                line.matches(Regex(".*\\d+\\.\\d{2}")) -> {
                    val amount = line.split(" ").last().toDoubleOrNull() ?: 0.0
                    currentItem = currentItem!!.copy(itemAmount = amount)
                    items.add(currentItem!!)
                    currentItem = null
                }
                // Handle additional cases if needed
            }
        }

        currentItem?.let { items.add(it) }
        return items
    }

    private fun saveReceipt() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (receiptItems.isEmpty()) {
            Toast.makeText(this, "No items to save", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val batch = firestore.batch()

        receiptItems.forEach { item ->
            val expense = hashMapOf(
                "expenseTitle" to item.itemName,
                "category" to "Receipt",
                "amount" to item.itemAmount,
                "date" to date,
                "userId" to user.uid,
                "timestamp" to Timestamp.now()
            )
            val docRef = firestore.collection("expenseTransactions").document()
            batch.set(docRef, expense)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Expenses saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save expenses: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
