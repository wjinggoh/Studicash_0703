package my.edu.tarc.studicash_0703.Models

data class Budget(
    val category: String,
    val amount: Double,
    val spent: Double,
    val startDate: String,
    val endDate: String
)
