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

        // Initialize selectedBudgetId from intent extras
        selectedBudgetId = intent.getStringExtra("BUDGET_ID")

        // Check if selectedBudgetId is null
        if (selectedBudgetId == null) {
            // Handle case where ID is not provided
            // For example, show an error message or finish the activity
            finish()
            return
        }

        // Initialize data and views
        setupSpinner()
        loadBudgetData()

        binding.saveBudgetBtn.setOnClickListener {
            saveBudget()
        }

        binding.imageView11.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSpinner() {
        val budgetCollection = firestore.collection("Budget")

        budgetCollection.get().addOnSuccessListener { result ->
            val budgetItems = mutableListOf<String>()
            val budgetIds = mutableListOf<String>()

            for (document in result) {
                val category = document.getString("category") ?: "" // Adjust field name as needed
                val id = document.id
                budgetItems.add(category)
                budgetIds.add(id)
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, budgetItems)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.editBudgetSpinner.adapter = adapter

            binding.editBudgetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedBudgetId = budgetIds[position]
                    loadBudgetData() // Load the data for the selected budget
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case where nothing is selected
                }
            }
        }.addOnFailureListener { exception ->
            // Handle failure
        }
    }

    private fun loadBudgetData() {
        selectedBudgetId?.let {
            firestore.collection("budgets").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        binding.budgetAmount.setText(document.getDouble("amount")?.toString())
                        binding.budgetStartDate.setText(document.getString("start_date"))
                        binding.budgetEndDate.setText(document.getString("end_date"))
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }

    private fun saveBudget() {
        val name = binding.editBudgetSpinner.selectedItem.toString() // Get the selected budget name
        val amount = binding.budgetAmount.text.toString().toDoubleOrNull()
        val startDate = binding.budgetStartDate.text.toString()
        val endDate = binding.budgetEndDate.text.toString()

        if (name.isEmpty() || amount == null || startDate.isEmpty() || endDate.isEmpty()) {
            // Handle invalid input
            return
        }

        val budgetData = mapOf(
            "name" to name,
            "amount" to amount,
            "start_date" to startDate,
            "end_date" to endDate
        )

        selectedBudgetId?.let {
            firestore.collection("budgets").document(it).set(budgetData)
                .addOnSuccessListener {
                    finish() // Handle success (e.g., show a toast or navigate back)
                }
                .addOnFailureListener { e ->
                    // Handle failure (e.g., show an error message)
                }
        }
    }
}
