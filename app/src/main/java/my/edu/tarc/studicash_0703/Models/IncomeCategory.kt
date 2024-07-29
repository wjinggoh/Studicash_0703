package my.edu.tarc.studicash_0703.Models

data class IncomeCategory(
    val icon: Int,
    val name: String,
    val iconUri: String? = null, // Add the iconUrl property
    val id: String = ""
) {
    override fun toString(): String {
        return name
    }
}