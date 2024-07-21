package my.edu.tarc.studicash_0703.Models

data class ExpenseCategory(
    val icon: Int,
    val name: String,
    val id: String = ""
) {
    override fun toString(): String {
        return name
    }
}
