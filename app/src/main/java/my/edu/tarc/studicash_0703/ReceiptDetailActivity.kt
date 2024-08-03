package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Receipt
import my.edu.tarc.studicash_0703.databinding.ActivityReceiptDetailBinding

class ReceiptDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptDetailBinding
    private var receiptId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receipt = intent.getParcelableExtra<Receipt>("Receipt")

        receipt?.let {
            binding.receiptDetailTitle.text = it.type
            binding.receiptDetailTotal.text = it.total

            receiptId = it.id
            Log.d("ReceiptDetailActivity", "Receipt ID: $receiptId")

            // Load image using Glide
            val receiptImageUriString = it.imageUri
            receiptImageUriString?.let { uriString ->
                Glide.with(this)
                    .load(uriString)
                    .placeholder(R.drawable.baseline_image_400) // Placeholder image
                    .error(R.drawable.baseline_image_400) // Error image
                    .into(binding.receiptDetailImage)
            }

        } ?: run {
            // Handle the case where receipt is null
            binding.receiptDetailTitle.text = "No details available"
            binding.receiptDetailTotal.text = "N/A"
            binding.receiptDetailImage.setImageResource(R.drawable.baseline_image_400) // Set a placeholder image
        }

        binding.receiptDetailBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.receiptDeleteBtn.setOnClickListener {
            receiptId?.let { id ->
                showDeleteConfirmationDialog(id)
            }
        }
    }



    private fun showDeleteConfirmationDialog(receiptId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Receipt")
            .setMessage("Are you sure you want to delete this receipt?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteReceipt(receiptId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteReceipt(receiptId: String) {
        if (receiptId.isNotEmpty()) {
            val firestore = FirebaseFirestore.getInstance()
            val documentRef = firestore.collection("Receipt").document(receiptId)

            Log.d("ReceiptDetailActivity", "Deleting document at path: ${documentRef.path}")

            documentRef.delete()
                .addOnSuccessListener {
                    Log.d("ReceiptDetailActivity", "Receipt successfully deleted!")
                    // Notify previous activity to refresh data
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)
                    finish() // Close the current activity
                }
                .addOnFailureListener { e ->
                    Log.w("ReceiptDetailActivity", "Error deleting receipt", e)
                }
        } else {
            Log.e("ReceiptDetailActivity", "Invalid receipt ID")
        }
    }
}
