package my.edu.tarc.studicash_0703.Models

data class FAQItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)
