package my.edu.tarc.studicash_0703

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.databinding.ActivityAddTransactionBinding
import com.google.firebase.Timestamp
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up date picker
        binding.selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$year-${month + 1}-$dayOfMonth"
                    binding.dateView.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Save transaction
        binding.saveTransactionButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val title = binding.transactionTitleInput.editText?.text.toString()
        val amount = binding.transactionAmountInput.editText?.text.toString().toDoubleOrNull()
        val category = binding.categorySpinner.selectedItem.toString()
        val date = binding.dateView.text.toString()
        val paymentMethod = binding.paymentMethodSpinner.selectedItem.toString()
        val isExpense = binding.rbAddExpense.isChecked
        val userId = auth.currentUser?.uid

        if (title.isEmpty() || amount == null || date.isEmpty() || userId == null) {
            // Handle empty fields
            return
        }

        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = date,
            paymentMethod = paymentMethod,
            isExpense = isExpense,
            userId = userId,
            timestamp = Timestamp.now() // Use Timestamp.now() to get current time
        )

        firestore.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

}
