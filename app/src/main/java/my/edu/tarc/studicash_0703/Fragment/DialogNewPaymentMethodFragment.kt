package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.PaymentMethod
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

        binding.radioGroupPaymentType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_card -> {
                    binding.etBankName.visibility = View.VISIBLE
                    binding.etCardNumber.visibility = View.VISIBLE
                    binding.etWalletName.visibility = View.GONE
                    binding.etCashDetails.visibility = View.GONE
                }
                R.id.radio_ewallet -> {
                    binding.etBankName.visibility = View.GONE
                    binding.etCardNumber.visibility = View.GONE
                    binding.etWalletName.visibility = View.VISIBLE
                    binding.etCashDetails.visibility = View.GONE
                }
                R.id.radio_cash -> {
                    binding.etBankName.visibility = View.GONE
                    binding.etCardNumber.visibility = View.GONE
                    binding.etWalletName.visibility = View.GONE
                    binding.etCashDetails.visibility = View.VISIBLE
                }
            }
        }

        binding.btnSavePaymentMethod.setOnClickListener {
            savePaymentMethod()
        }

        return view
    }

    private fun savePaymentMethod() {
        val paymentMethod = when (binding.radioGroupPaymentType.checkedRadioButtonId) {
            R.id.radio_card -> {
                PaymentMethod(
                    type = "Card",
                    details = "Bank: ${binding.etBankName.text}, Card Number: ${binding.etCardNumber.text}"
                )
            }
            R.id.radio_ewallet -> {
                PaymentMethod(
                    type = "E-Wallet",
                    details = "Wallet: ${binding.etWalletName.text}"
                )
            }
            R.id.radio_cash -> {
                PaymentMethod(
                    type = "Cash",
                    details = "Details: ${binding.etCashDetails.text}"
                )
            }
            else -> return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("paymentMethods")
            .add(paymentMethod)
            .addOnSuccessListener {
                dismiss()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
