package my.edu.tarc.studicash_0703.Models

data class UpcomingTransaction(
    val category: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val frequency: String = ""
)
