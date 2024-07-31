package my.edu.tarc.studicash_0703.PaymentMethod

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentDialogNewPaymentMethodBinding

class DialogNewPaymentMethodFragment : DialogFragment() {

    private var _binding: FragmentDialogNewPaymentMethodBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogNewPaymentMethodBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize the spinners
        setupSpinners()

        binding.radioGroupPaymentType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_card -> {
                    binding.spinnerBank.visibility = View.VISIBLE
                    binding.etCardNumber.visibility = View.VISIBLE
                    binding.spinnerWalletName.visibility = View.GONE
                    binding.etCashDetails.visibility = View.GONE
                }
                R.id.radio_ewallet -> {
                    binding.spinnerBank.visibility = View.GONE
                    binding.etCardNumber.visibility = View.GONE
                    binding.spinnerWalletName.visibility = View.VISIBLE
                    binding.etCashDetails.visibility = View.GONE
                }
                R.id.radio_cash -> {
                    binding.spinnerBank.visibility = View.GONE
                    binding.etCardNumber.visibility = View.GONE
                    binding.spinnerWalletName.visibility = View.GONE
                    binding.etCashDetails.visibility = View.VISIBLE
                }
            }
        }

        binding.btnSavePaymentMethod.setOnClickListener {
            savePaymentMethod()
        }

        return view
    }

    private fun setupSpinners() {
        // Set up the bank spinner
        val bankAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.banks_array, // Define this in strings.xml
            android.R.layout.simple_spinner_item
        )
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBank.adapter = bankAdapter

        // Set up the e-wallet spinner
        val walletAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ewallets_array, // Define this in strings.xml
            android.R.layout.simple_spinner_item
        )
        walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWalletName.adapter = walletAdapter
    }

    private fun savePaymentMethod() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val paymentMethod = when (binding.radioGroupPaymentType.checkedRadioButtonId) {
            R.id.radio_card -> {
                val bankName = binding.spinnerBank.selectedItem.toString()
                val cardNumber = binding.etCardNumber.text.toString()
                val cleanedCardNumber = cardNumber.replace(Regex("[^0-9]"), "")
                Log.d("PaymentMethod", "Bank: $bankName, Card Number: $cleanedCardNumber") // Debugging line
                if (isValidCardNumber(bankName, cleanedCardNumber)) {
                    PaymentMethod(
                        type = "Card",
                        details = "$bankName, $cardNumber",
                        uid = uid
                    )
                } else {
                    showToast("Invalid card number format for $bankName")
                    return
                }
            }
            R.id.radio_ewallet -> {
                PaymentMethod(
                    type = "E-Wallet",
                    details = binding.spinnerWalletName.selectedItem.toString(),
                    uid = uid
                )
            }
            R.id.radio_cash -> {
                PaymentMethod(
                    type = "Cash",
                    details = binding.etCashDetails.text.toString(),
                    uid = uid
                )
            }
            else -> {
                showToast("Please select a payment method type.")
                return
            }
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("paymentMethods")
            .add(paymentMethod)
            .addOnSuccessListener {
                dismiss()
            }
            .addOnFailureListener { e ->
                Log.e("PaymentMethod", "Error adding document", e)
                showToast("Failed to save payment method.")
            }
    }

    private fun isValidCardNumber(bankName: String, cardNumber: String): Boolean {
        val cleanedCardNumber = cardNumber.replace(Regex("[^0-9]"), "")

        return when (bankName) {
            "Maybank (Malayan Banking Berhad)" -> cleanedCardNumber.length == 12
            "CIMB Bank Berhad" -> cleanedCardNumber.length in listOf(12, 15)
            "Public Bank Berhad" -> cleanedCardNumber.length == 12
            "Hong Leong Bank Berhad" -> cleanedCardNumber.length == 12
            "RHB Bank Berhad" -> cleanedCardNumber.length == 12
            "AmBank (AmBank (M) Berhad)" -> cleanedCardNumber.length == 12
            "UOB Malaysia (United Overseas Bank (Malaysia) Bhd)" -> cleanedCardNumber.length == 12
            "Bank Islam Malaysia Berhad" -> cleanedCardNumber.length == 14
            "Bank Rakyat (Bank Kerjasama Rakyat Malaysia Berhad)" -> cleanedCardNumber.length == 12
            "Standard Chartered Bank Malaysia Berhad" -> cleanedCardNumber.length == 13
            "HSBC Bank Malaysia Berhad" -> cleanedCardNumber.length == 12
            "Deutsche Bank (Malaysia) Berhad" -> cleanedCardNumber.length == 12
            "OCBC Bank" -> cleanedCardNumber.length == 12
            else -> false
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
