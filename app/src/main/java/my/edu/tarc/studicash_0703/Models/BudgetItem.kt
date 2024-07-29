package my.edu.tarc.studicash_0703.Models

import com.google.firebase.firestore.PropertyName

data class BudgetItem(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("name") val name: String = "",
    @get:PropertyName("category") val category: String = "",
    @get:PropertyName("amount") val amount: Double = 0.0,
    @get:PropertyName("spent") var spent: Double = 0.0,
    @get:PropertyName("progress") var progress: Int = 0,
    @get:PropertyName("startDate") val startDate: String = "",
    @get:PropertyName("endDate") val endDate: String = "",
    @get:PropertyName("icon") val icon: Int = 0
)
