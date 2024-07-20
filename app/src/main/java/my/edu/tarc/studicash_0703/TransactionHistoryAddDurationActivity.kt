package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import my.edu.tarc.studicash_0703.databinding.ActivityTransactionHistoryAddDurationBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionHistoryAddDurationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryAddDurationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionHistoryAddDurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.selectDateButton2.setOnClickListener { showDatePickerDialog(binding.dateView1) }
        binding.selectDateButton3.setOnClickListener { showDatePickerDialog(binding.dateView2) }

        binding.addDurationConfirmBtn.setOnClickListener {
            val startDate = binding.dateView1.text.toString()
            val endDate = binding.dateView2.text.toString()
            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    putExtra("startDate", startDate)
                    putExtra("endDate", endDate)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.imageView8.setOnClickListener { finish() }
    }

    private fun showDatePickerDialog(dateView: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateView.text = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
