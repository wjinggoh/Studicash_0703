package my.edu.tarc.studicash_0703.Models

data class ReceiptItems(
    val itemName: String,
    val itemAmount: Double? = null
)
 {
     fun toMap(): Map<String, Any> {
         return mapOf(
             "itemName" to itemName,
             "itemAmount" to (itemAmount ?: 0.0) // Use 0.0 if itemAmount is null
         )
     }

}
