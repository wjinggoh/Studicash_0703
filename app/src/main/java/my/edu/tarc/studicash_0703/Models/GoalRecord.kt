package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp

data class GoalRecord(
    val userId: String = "",
    val goal: String = "",
    val saveAmount: Double = 0.0,
    val saveDate: Timestamp = Timestamp.now(),
    val goalRecordId: String = ""
)
