package my.edu.tarc.studicash_0703

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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

        // Back button functionality
        binding.imageView2.setOnClickListener { onBackPressed() }
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
                        selectedImageBitmap = bitmap
                        binding.imageView.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(this@AddReceiptActivity, "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun loadImageBitmap(uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, this)
                    inSampleSize = calculateInSampleSize(this, 1000, 1000)
                    inJustDecodeBounds = false
                }
                inputStream.close()
                val inputStreamResized = contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStreamResized, null, options)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (width: Int, height: Int) = options.outWidth to options.outHeight

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
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
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

        // Display the recognized text
        binding.itemsTextView.text = resultText

        // Extract items and amounts from the recognized text
        val lines = resultText.split("\n")
        receiptItems.clear()

        for (line in lines) {
            val cleanedLine = line.trim()
            if (cleanedLine.isNotEmpty()) {
                // Assuming the amount will be extracted or processed later
                val item = ReceiptItems(itemName = cleanedLine, itemAmount = 0.0) // Default amount
                receiptItems.add(item)
            }
        }

        // Update the RecyclerView with the extracted items
        adapter.notifyDataSetChanged()
    }


    private fun saveReceipt() {
        val userId = auth.currentUser?.uid ?: return
        val currentTime = Timestamp.now()
        val receiptData = hashMapOf(
            "userId" to userId,
            "items" to receiptItems.map { it.toMap() },
            "timestamp" to currentTime
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                firestore.collection("receipts").add(receiptData).await()
                Toast.makeText(this@AddReceiptActivity, "Receipt saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddReceiptActivity, "Failed to save receipt: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
