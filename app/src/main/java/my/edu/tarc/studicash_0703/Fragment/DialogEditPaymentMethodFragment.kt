package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.PaymentMethod
import my.edu.tarc.studicash_0703.databinding.FragmentDialogEditPaymentMethodBinding

class DialogEditPaymentMethodFragment : DialogFragment() {

    private var _binding: FragmentDialogEditPaymentMethodBinding? = null
    private val binding get() = _binding!!

    private lateinit var documentId: String
    private lateinit var paymentMethod: PaymentMethod

    companion object {
        private const val ARG_DOCUMENT_ID = "document_id"
        private const val ARG_PAYMENT_METHOD = "payment_method"

        fun newInstance(documentId: String, paymentMethod: PaymentMethod): DialogEditPaymentMethodFragment {
            val fragment = DialogEditPaymentMethodFragment()
            val args = Bundle()
            args.putString(ARG_DOCUMENT_ID, documentId)
            args.putParcelable(ARG_PAYMENT_METHOD, paymentMethod)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getString(ARG_DOCUMENT_ID).orEmpty()
            paymentMethod = it.getParcelable(ARG_PAYMENT_METHOD)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogEditPaymentMethodBinding.inflate(inflater, container, false)
        val view = binding.root

        // Populate fields with payment method data
        when (paymentMethod.type) {
            "Card" -> {
                binding.etBankName.visibility = View.VISIBLE
                binding.etCardNumber.visibility = View.VISIBLE
                binding.etWalletName.visibility = View.GONE
                binding.etCashDetails.visibility = View.GONE
                val details = paymentMethod.details.split(", ")
                if (details.size == 2) {
                    binding.etBankName.setText(details[0].removePrefix("Bank: "))
                    binding.etCardNumber.setText(details[1].removePrefix("Card Number: "))
                }
            }
            "E-Wallet" -> {
                binding.etBankName.visibility = View.GONE
                binding.etCardNumber.visibility = View.GONE
                binding.etWalletName.visibility = View.VISIBLE
                binding.etCashDetails.visibility = View.GONE
                binding.etWalletName.setText(paymentMethod.details.removePrefix("Wallet: "))
            }
            "Cash" -> {
                binding.etBankName.visibility = View.GONE
                binding.etCardNumber.visibility = View.GONE
                binding.etWalletName.visibility = View.GONE
                binding.etCashDetails.visibility = View.VISIBLE
                binding.etCashDetails.setText(paymentMethod.details.removePrefix("Details: "))
            }
        }

        binding.btnSavePaymentMethod.setOnClickListener {
            savePaymentMethod()
        }

        return view
    }

    private fun savePaymentMethod() {
        paymentMethod = when (paymentMethod.type) {
            "Card" -> {
                PaymentMethod(
                    type = "Card",
                    details = "Bank: ${binding.etBankName.text}, Card Number: ${binding.etCardNumber.text}"
                )
            }
            "E-Wallet" -> {
                PaymentMethod(
                    type = "E-Wallet",
                    details = "Wallet: ${binding.etWalletName.text}"
                )
            }
            "Cash" -> {
                PaymentMethod(
                    type = "Cash",
                    details = "Details: ${binding.etCashDetails.text}"
                )
            }
            else -> return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("paymentMethods").document(documentId)
            .set(paymentMethod)
            .addOnSuccessListener {
                dismiss()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
