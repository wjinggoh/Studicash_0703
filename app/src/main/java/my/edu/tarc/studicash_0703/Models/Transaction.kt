package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp

data class Transaction(
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val paymentMethod: String = "",
    val isExpense: Boolean = true,
    val userId: String = "",
    val timestamp: Timestamp? = null // Use com.google.firebase.Timestamp
)
