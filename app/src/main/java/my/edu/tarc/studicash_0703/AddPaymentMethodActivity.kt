package my.edu.tarc.studicash_0703

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Fragment.DialogEditPaymentMethodFragment
import my.edu.tarc.studicash_0703.Fragment.DialogNewPaymentMethodFragment
import my.edu.tarc.studicash_0703.Models.PaymentMethod
import my.edu.tarc.studicash_0703.AddPaymentMethodActivity

class AddPaymentMethodActivity : AppCompatActivity() {

    private lateinit var paymentMethodsContainer: LinearLayout
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment_method)

        paymentMethodsContainer = findViewById(R.id.payment_methods_container)
        addButton = findViewById(R.id.btn_add_payment_method)

        addButton.setOnClickListener {
            val dialog = DialogNewPaymentMethodFragment()
            dialog.show(supportFragmentManager, "NewPaymentMethodDialog")
        }

        // Load payment methods from Firestore
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("paymentMethods")
            .get()
            .addOnSuccessListener { result ->
                paymentMethodsContainer.removeAllViews()
                for (document in result) {
                    val paymentMethod = document.toObject(PaymentMethod::class.java)
                    addPaymentMethodView(paymentMethod, document.id)
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch payment methods
                // For example, log the error or show a toast
            }
    }

    private fun addPaymentMethodView(paymentMethod: PaymentMethod, documentId: String) {
        val paymentMethodView = layoutInflater.inflate(R.layout.created_payment_method_item, paymentMethodsContainer, false)

        val paymentMethodText = paymentMethodView.findViewById<TextView>(R.id.payment_method_text)
        val editButton = paymentMethodView.findViewById<Button>(R.id.btn_edit_payment_method)

        paymentMethodText.text = paymentMethod.details
        editButton.setOnClickListener {
            val dialog = DialogEditPaymentMethodFragment.newInstance(documentId, paymentMethod)
            dialog.show(supportFragmentManager, "EditPaymentMethodDialog")
        }

        paymentMethodsContainer.addView(paymentMethodView)
    }
}
