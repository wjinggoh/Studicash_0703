package my.edu.tarc.studicash_0703.Models

import NotificationType

data class NotificationItem(
    val type: NotificationType,
    val title: String,
    val message: String
)
