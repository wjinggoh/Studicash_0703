package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.databinding.ActivityEditBudgetBinding

class EditBudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBudgetBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var selectedBudgetId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedBudgetId = intent.getStringExtra("BUDGET_ID")

        setupSpinner()
        loadBudgetData()
        setupSaveButton()
    }

    private fun setupSpinner() {
        firestore.collection("Goal").get().addOnSuccessListener { result ->
            val items = mutableListOf<String>()
            for (document in result) {
                val name = document.getString("name") ?: ""
                items.add(name)
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.editBudgetSpinner.adapter = adapter

            binding.editBudgetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    // Handle item selection if needed
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }.addOnFailureListener { exception ->
            // Handle the error
            exception.printStackTrace()
        }
    }

    private fun loadBudgetData() {
        selectedBudgetId?.let { budgetId ->
            firestore.collection("Budget").document(budgetId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val startDate = document.getString("startDate") ?: ""
                    val endDate = document.getString("endDate") ?: ""

                    binding.budgetNameEditText.setText(name)
                    binding.budgetAmountEditText.setText(amount.toString())
                    binding.startDateEditText.setText(startDate)
                    binding.endDateEditText.setText(endDate)

                    val categoryPosition = (binding.editBudgetSpinner.adapter as ArrayAdapter<String>).getPosition(category)
                    binding.editBudgetSpinner.setSelection(categoryPosition)
                }
            }.addOnFailureListener { exception ->
                // Handle the error
                exception.printStackTrace()
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveBudgetButton.setOnClickListener {
            val name = binding.budgetNameEditText.text.toString()
            val category = binding.editBudgetSpinner.selectedItem.toString()
            val amount = binding.budgetAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val startDate = binding.startDateEditText.text.toString()
            val endDate = binding.endDateEditText.text.toString()

            val budgetData = mapOf(
                "name" to name,
                "category" to category,
                "amount" to amount,
                "startDate" to startDate,
                "endDate" to endDate
            )

            selectedBudgetId?.let { budgetId ->
                firestore.collection("Budget").document(budgetId)
                    .set(budgetData)
                    .addOnSuccessListener {
                        // Handle successful save
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        // Handle failed save
                        exception.printStackTrace()
                    }
            }
        }
    }
}
