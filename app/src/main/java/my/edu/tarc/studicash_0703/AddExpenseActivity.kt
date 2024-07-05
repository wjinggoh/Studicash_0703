package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityAddExpenseBinding

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.SaveExpense.setOnClickListener {
            val expenseTitle = binding.expensedescription.editText?.text.toString()
            val selectedCategory = binding.categories.selectedItem.toString()
            val selectedDate = binding.dateView.text.toString()

            if (expenseTitle.isEmpty()) {
                Toast.makeText(this@AddExpenseActivity, "Please enter an expense title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val expenseData = hashMapOf(
                "expenseTitle" to expenseTitle,
                "category" to selectedCategory,
                "date" to selectedDate
                // Add more fields as necessary
            )

            db.collection("expenses")
                .add(expenseData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this@AddExpenseActivity, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                    // Optionally clear input fields or reset UI
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@AddExpenseActivity, "Error saving expense: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
