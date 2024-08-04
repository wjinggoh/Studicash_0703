package my.edu.tarc.studicash_0703.Transaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentEditTransactionBinding

class EditTransactionFragment : DialogFragment() {
    private lateinit var binding: FragmentEditTransactionBinding
    private var transactionId: String? = null
    private lateinit var transaction: Transaction

    private val predefinedExpenseCategories = listOf("Food", "Transport", "Utilities", "Entertainment", "Other")
    private val predefinedIncomeCategories = listOf("Salary", "Allowance", "Freelance", "Other")

    private var expenseCategories = mutableListOf<String>()
    private var incomeCategories = mutableListOf<String>()
    private var paymentMethods = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionId = arguments?.getString("transactionId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditTransactionBinding.inflate(inflater, container, false)

        transactionId?.let { fetchTransaction(it) }
        fetchCategories() // Fetch categories and payment methods from Firestore

        binding.saveButton.setOnClickListener {
            updateTransaction()
        }
        binding.editTransactionBackBtn.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun fetchCategories() {
        val firestore = FirebaseFirestore.getInstance()

        // Fetch expense categories
        firestore.collection("ExpenseCategories")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid).get()
            .addOnSuccessListener { result ->
                expenseCategories = (result.map { document ->
                    document.getString("name") ?: ""
                }.toMutableList() + predefinedExpenseCategories).toMutableList()

                // If a transaction is loaded, update the spinner
                if (::transaction.isInitialized) {
                    setupSpinners()
                }
            }

        // Fetch income categories
        firestore.collection("IncomeCategories")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid).get()
            .addOnSuccessListener { result ->
                incomeCategories = (result.map { document ->
                    document.getString("name") ?: ""
                }.toMutableList() + predefinedIncomeCategories).toMutableList()

                // If a transaction is loaded, update the spinner
                if (::transaction.isInitialized) {
                    setupSpinners()
                }
            }

        // Fetch payment methods
        firestore.collection("paymentMethods")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid).get()
            .addOnSuccessListener { result ->
                paymentMethods = result.map { document ->
                    document.getString("details") ?: ""
                }.toMutableList()

                // If a transaction is loaded, update the spinner
                if (::transaction.isInitialized) {
                    setupSpinners()
                }
            }
    }

    private fun setupSpinners() {
        val categories = if (transaction.expense) {
            Log.d(TAG, "Setting up expense categories")
            expenseCategories
        } else {
            Log.d(TAG, "Setting up income categories")
            incomeCategories
        }

        // Set up adapter for category spinner
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editCategorySpinner.adapter = categoryAdapter

        // Set up adapter for payment method spinner
        val paymentMethodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paymentMethods)
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editPaymentMethodSpinner.adapter = paymentMethodAdapter
    }

    private fun fetchTransaction(transactionId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Try fetching both collections to see which one has the document
        val expenseDoc = FirebaseFirestore.getInstance().collection("expenseTransactions").document(transactionId).get()
        val incomeDoc = FirebaseFirestore.getInstance().collection("incomeTransactions").document(transactionId).get()

        expenseDoc.addOnSuccessListener { document ->
            if (document.exists()) {
                transaction = document.toObject(Transaction::class.java)!!
                setupSpinners()
                updateUI()
            } else {
                incomeDoc.addOnSuccessListener { document ->
                    if (document.exists()) {
                        transaction = document.toObject(Transaction::class.java)!!
                        setupSpinners()
                        updateUI()
                    } else {
                        Log.e(TAG, "Transaction document does not exist in both collections.")
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error fetching transaction", exception)
        }
    }

    private fun updateUI() {
        binding.titleEditText.setText(transaction.title)
        binding.amountEditText.setText(transaction.amount.toString())
        binding.dateEditText.setText(transaction.date)

        binding.editCategorySpinner.setSelection(getCategoryPosition(transaction.category))
        binding.editPaymentMethodSpinner.setSelection(getPaymentMethodPosition(transaction.paymentMethod))
    }

    private fun getCategoryPosition(category: String): Int {
        val categories = if (transaction.expense) expenseCategories else incomeCategories
        return categories.indexOf(category).takeIf { it >= 0 } ?: 0 // Default to 0 if not found
    }

    private fun getPaymentMethodPosition(paymentMethod: String): Int {
        return paymentMethods.indexOf(paymentMethod).takeIf { it >= 0 } ?: 0 // Default to 0 if not found
    }

    private fun updateTransaction() {
        val updatedTransaction = transaction.copy(
            title = binding.titleEditText.text.toString(),
            amount = binding.amountEditText.text.toString().toDouble(),
            category = binding.editCategorySpinner.selectedItem.toString(),
            date = binding.dateEditText.text.toString(),
            paymentMethod = binding.editPaymentMethodSpinner.selectedItem.toString()
        )

        val collection = if (updatedTransaction.expense) {
            FirebaseFirestore.getInstance().collection("expenseTransactions")
        } else {
            FirebaseFirestore.getInstance().collection("incomeTransactions")
        }

        collection.document(transactionId!!).set(updatedTransaction)
            .addOnSuccessListener {
                Log.d(TAG, "Transaction updated successfully.")
                dismiss() // Close the dialog
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating transaction: ${exception.message}", exception)
            }
    }

    companion object {
        private const val TAG = "EditTransactionFragment"

        fun newInstance(transactionId: String): EditTransactionFragment {
            val fragment = EditTransactionFragment()
            val args = Bundle()
            args.putString("transactionId", transactionId)
            fragment.arguments = args
            return fragment
        }
    }
}
