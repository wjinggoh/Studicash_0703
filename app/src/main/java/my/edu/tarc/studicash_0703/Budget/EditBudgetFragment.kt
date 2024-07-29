package my.edu.tarc.studicash_0703.Budget

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.databinding.FragmentEditBudgetBinding
import java.text.SimpleDateFormat
import java.util.*

class EditBudgetFragment : Fragment() {
    private lateinit var binding: FragmentEditBudgetBinding
    private lateinit var dateFormat: SimpleDateFormat
    private var budgetId: String? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            budgetId = it.getString("budgetId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBudgetBinding.inflate(inflater, container, false)

        dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        setupSpinner()
        setupDatePickers()
        setupSaveButton()

        budgetId?.let { fetchBudget(it) }

        binding.editBudgetBackBtn1.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun setupSpinner() {
        val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Other") // Add your categories here
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editBudgetSpinner.adapter = adapter
    }

    private fun setupDatePickers() {
        binding.buttonStartDate.setOnClickListener {
            showDatePicker { date -> binding.editTextStartDate.setText(date) }
        }
        binding.buttonEndDate.setOnClickListener {
            showDatePicker { date -> binding.editTextEndDate.setText(date) }
        }
    }

    private fun showDatePicker(onDateSet: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                onDateSet(dateFormat.format(date))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            saveBudget()
        }
    }

    private fun fetchBudget(budgetId: String) {
        firestore.collection("Budget").document(budgetId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val budgetItem = document.toObject(BudgetItem::class.java)
                    budgetItem?.let {
                        populateUI(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "No such budget found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching budget: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateUI(budgetItem: BudgetItem) {
        binding.budgetNameEditText.setText(budgetItem.name)
        binding.budgetAmountEditText.setText(budgetItem.amount.toString())
        binding.editTextStartDate.setText(budgetItem.startDate)
        binding.editTextEndDate.setText(budgetItem.endDate)
        binding.editBudgetSpinner.setSelection(getCategoryIndex(budgetItem.category))
    }

    private fun getCategoryIndex(category: String): Int {
        val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Other")
        return categories.indexOf(category)
    }

    private fun saveBudget() {
        val name = binding.budgetNameEditText.text.toString()
        val amount = binding.budgetAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val category = binding.editBudgetSpinner.selectedItem.toString()

        if (name.isBlank() || amount <= 0 || startDate.isBlank() || endDate.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedBudget = BudgetItem(
            id = budgetId ?: "",
            name = name,
            amount = amount,
            spent = 0.0,
            progress = 0,
            startDate = startDate,
            endDate = endDate,
            category = category,
            icon = 0 // You may need to handle the icon if applicable
        )

        firestore.collection("Budget").document(budgetId ?: "")
            .set(updatedBudget)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error updating budget: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(budgetId: String): EditBudgetFragment {
            val fragment = EditBudgetFragment()
            val args = Bundle().apply {
                putString("budgetId", budgetId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
