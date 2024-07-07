package my.edu.tarc.studicash_0703.Models

data class CategoryItem(
    val icon: Int,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}
