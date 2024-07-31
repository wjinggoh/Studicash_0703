package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import my.edu.tarc.studicash_0703.Models.Receipt
import my.edu.tarc.studicash_0703.databinding.ActivityReceiptDetailBinding

class ReceiptDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receipt = intent.getParcelableExtra<Receipt>("Receipt")

        receipt?.let {
            binding.receiptDetailTitle.text = it.type
            binding.receiptDetailTotal.text = it.total

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
    }
}
