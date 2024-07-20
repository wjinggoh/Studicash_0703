package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Fragment.DialogEditPaymentMethodFragment
import my.edu.tarc.studicash_0703.Fragment.DialogNewPaymentMethodFragment
import my.edu.tarc.studicash_0703.Models.PaymentMethod
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityAddPaymentMethodBinding

class AddPaymentMethodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentMethodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddPaymentMethod.setOnClickListener {
            val dialog = DialogNewPaymentMethodFragment()
            dialog.show(supportFragmentManager, "NewPaymentMethodDialog")
        }

        binding.addPaymentBackBtn.setOnClickListener {
            onBackPressed()
        }

        // Load payment methods from Firestore
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("paymentMethods")
            .get()
            .addOnSuccessListener { result ->
                binding.paymentMethodsContainer.removeAllViews()
                for (document in result) {
                    val paymentMethod = document.toObject(PaymentMethod::class.java)
                    addPaymentMethodView(paymentMethod, document.id)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun addPaymentMethodView(paymentMethod: PaymentMethod, documentId: String) {
        val paymentMethodView = layoutInflater.inflate(R.layout.created_payment_method_item, binding.paymentMethodsContainer, false)

        val paymentMethodText = paymentMethodView.findViewById<TextView>(R.id.payment_method_text)
        val editButton = paymentMethodView.findViewById<Button>(R.id.btn_edit_payment_method)
        val deleteButton = paymentMethodView.findViewById<ImageView>(R.id.btn_delete_payment_method)

        paymentMethodText.text = paymentMethod.details
        editButton.setOnClickListener {
            val dialog = DialogEditPaymentMethodFragment.newInstance(documentId, paymentMethod)
            dialog.show(supportFragmentManager, "EditPaymentMethodDialog")
        }

        deleteButton.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("paymentMethods").document(documentId)
                .delete()
                .addOnSuccessListener {
                    binding.paymentMethodsContainer.removeView(paymentMethodView)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to delete payment method: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }


        binding.paymentMethodsContainer.addView(paymentMethodView)
    }
}
