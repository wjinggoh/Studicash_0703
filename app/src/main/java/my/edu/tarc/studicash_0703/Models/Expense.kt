package my.edu.tarc.studicash_0703.Models

data class Expense(
    val expenseTitle: String? = null,
    val expenseAmount: Double? = null,
    val date: String? = null,
    val category: String? = null,
    val paymentmethod: String? = null,
    val userId: String? = null
)