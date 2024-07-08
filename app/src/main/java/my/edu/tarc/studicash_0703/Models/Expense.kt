package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp

data class Expense(
    val expenseTitle: String? = null,
    val expenseAmount: Double? = null,
    val date: String? = null,
    val category: String? = null,
    val paymentmethod: String? = null,
    val userId: String? = null,
    val timestamp: Timestamp? = null // Ensure this field is added
)
