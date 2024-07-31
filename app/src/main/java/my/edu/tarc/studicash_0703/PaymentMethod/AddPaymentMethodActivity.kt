package my.edu.tarc.studicash_0703.PaymentMethod

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityAddPaymentMethodBinding

class AddPaymentMethodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentMethodBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
        val uid = auth.currentUser?.uid ?: return // Exit if UID is null

        firestore.collection("paymentMethods")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                binding.paymentMethodsContainer.removeAllViews()
                for (document in result) {
                    val paymentMethod = document.toObject(PaymentMethod::class.java)
                    addPaymentMethodView(paymentMethod, document.id)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load payment methods: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            showDeleteConfirmationDialog(documentId, paymentMethod.details)
        }

        binding.paymentMethodsContainer.addView(paymentMethodView)
    }

    private fun showDeleteConfirmationDialog(documentId: String, paymentMethodDetails: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Payment Method")
            .setMessage("Are you sure you want to delete this payment method: $paymentMethodDetails?")
            .setPositiveButton("Yes") { dialog, _ ->
                deletePaymentMethod(documentId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deletePaymentMethod(documentId: String) {
        firestore.collection("paymentMethods").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Payment method deleted successfully", Toast.LENGTH_SHORT).show()
                loadPaymentMethods() // Refresh the list
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to delete payment method: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
