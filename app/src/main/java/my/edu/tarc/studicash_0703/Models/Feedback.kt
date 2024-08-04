package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp

data class Feedback(
    val email: String = "",
    val feedback: String = "",
    val timestamp: Timestamp? = null
)
