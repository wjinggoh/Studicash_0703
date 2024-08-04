package my.edu.tarc.studicash_0703.Budget

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.CategorySpinnerAdapter
import my.edu.tarc.studicash_0703.databinding.FragmentEditBudgetBinding
import java.text.SimpleDateFormat
import java.util.*

class EditBudgetDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEditBudgetBinding
    private val calendar = Calendar.getInstance()
    private var expenseCategories = listOf<ExpenseCategory>()
    private lateinit var firestore: FirebaseFirestore
    private var budgetId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Get budgetId from arguments
        budgetId = arguments?.getString("budgetId")

        // Fetch categories from Firestore
        fetchCategories()

        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }

        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }

        binding.saveButton.setOnClickListener {
            saveBudget()
        }

        // Load existing budget data
        budgetId?.let {
            loadBudgetData(it)
        }
    }

    private fun showDatePickerDialog(dateTextView: EditText) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateTextView(dateTextView)
        }

        DatePickerDialog(
            requireContext(), dateSetListener,
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTextView(dateTextView: EditText) {
        val format = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale.US)
        dateTextView.setText(sdf.format(calendar.time))
    }

    private fun setupCategorySpinner(categories: List<ExpenseCategory>) {
        val adapter = CategorySpinnerAdapter(requireContext(), categories)
        binding.editBudgetSpinner.adapter = adapter
    }

    private fun fetchCategories() {
        firestore.collection("ExpenseCategories")
            .get()
            .addOnSuccessListener { result ->
                val fetchedCategories = result.mapNotNull { document ->
                    val iconField = document.get("icon")
                    val iconResId = when (iconField) {
                        is String -> {
                            resources.getIdentifier(iconField, "drawable", requireContext().packageName)
                        }
                        is Long -> {
                            iconField.toInt()
                        }
                        else -> {
                            Log.e("EditBudgetDialogFragment", "Unexpected type for 'icon': ${iconField?.javaClass?.name}")
                            R.drawable.money // Use a default icon resource
                        }
                    }
                    val name = document.getString("name") ?: ""
                    ExpenseCategory(iconResId, name)
                }

                expenseCategories = getExpenseCategories() + fetchedCategories
                setupCategorySpinner(expenseCategories)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getExpenseCategories(): List<ExpenseCategory> {
        return listOf(
            ExpenseCategory(R.drawable.food, "Food"),
            ExpenseCategory(R.drawable.fees, "Fees"),
            ExpenseCategory(R.drawable.shopping, "Shopping"),
            ExpenseCategory(R.drawable.transport, "Transportation"),
            ExpenseCategory(R.drawable.custom_category, "Other")
        )
    }

    private fun loadBudgetData(budgetId: String) {
        firestore.collection("Budget").document(budgetId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val amount = document.getDouble("amount")
                    val startDate = document.getString("startDate")
                    val endDate = document.getString("endDate")
                    val category = document.getString("category")

                    binding.budgetNameEditText.setText(name)
                    binding.budgetAmountEditText.setText(amount?.toString())
                    binding.editTextStartDate.setText(startDate)
                    binding.editTextEndDate.setText(endDate)

                    // Find the selected category
                    val selectedCategory = expenseCategories.find { it.name == category }
                    val adapter = binding.editBudgetSpinner.adapter as CategorySpinnerAdapter
                    val position = selectedCategory?.let { adapter.getPosition(it) } ?: 0
                    binding.editBudgetSpinner.setSelection(position)
                } else {
                    Toast.makeText(requireContext(), "No such budget document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading budget data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveBudget() {
        val name = binding.budgetNameEditText.text.toString()
        val amount = binding.budgetAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val selectedCategory = binding.editBudgetSpinner.selectedItem as ExpenseCategory

        if (name.isBlank() || amount <= 0 || startDate.isBlank() || endDate.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetItem = mapOf(
            "name" to name,
            "amount" to amount,
            "startDate" to startDate,
            "endDate" to endDate,
            "category" to selectedCategory.name
        )

        budgetId?.let {
            firestore.collection("Budget").document(it).set(budgetItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error updating budget: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "Budget ID is missing", Toast.LENGTH_SHORT).show()
        }
    }
}

