package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp

sealed class FinancialRecord {
    data class Income(
        val incomeTitle: String? = null,
        val incomeAmount: Double? = null,
        val date: String? = null,
        val category: String? = null,
        val paymentMethod: String? = null,
        val userId: String? = null,
        val timestamp: Timestamp? = null
    ) : FinancialRecord()

    data class Expense(
        val expenseTitle: String? = null,
        val expenseAmount: Double? = null,
        val date: String? = null,
        val category: String? = null,
        val paymentmethod: String? = null,
        val userId: String? = null,
        val timestamp: Timestamp? = null
    ) : FinancialRecord()
}
