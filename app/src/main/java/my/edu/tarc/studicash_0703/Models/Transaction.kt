package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp
import my.edu.tarc.studicash_0703.R

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val paymentMethod: String = "",
    val paymentMethodDetails: String? = null,
    val expense: Boolean = true,
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val expenseColorRes: Int = R.drawable.expense,
    val incomeColorRes: Int = R.drawable.income,
    val receiptId: String? = null
)
