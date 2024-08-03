package my.edu.tarc.studicash_0703.Models

import NotificationType
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val type: NotificationType,
    val title: String,
    val message: String,
    val date: String,
    val timestamp: Timestamp = Timestamp.now()
)
