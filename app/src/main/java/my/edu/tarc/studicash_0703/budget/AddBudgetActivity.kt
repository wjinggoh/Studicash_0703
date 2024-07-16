package my.edu.tarc.studicash_0703.budget

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityAddBudgetBinding
import java.util.*

class AddBudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBudgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.budgetBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.selectStartDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.startDateView.text = selectedDate
            }
        }

        binding.selectEndDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.endDateView.text = selectedDate
            }
        }

        setupCategorySpinner()

        binding.saveBudgetButton.setOnClickListener {
            val budgetAmount = binding.budgetAmountEditText.text.toString()
            val selectedCategory = binding.budgetCategorySpinner.selectedItem.toString()
            val startDate = binding.startDateView.text.toString()
            val endDate = binding.endDateView.text.toString()

            // Logic to save the budget goes here
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
            onDateSelected(formattedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Food", "Transport", "Entertainment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.budgetCategorySpinner.adapter = adapter
    }
}
