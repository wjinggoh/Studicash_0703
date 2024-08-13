package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.NotificationItem
import my.edu.tarc.studicash_0703.databinding.NotificationItemBinding
import java.text.SimpleDateFormat
import java.util.*


class NotificationAdapter(
    private val context: Context,
    private val notifications: MutableList<NotificationItem> // Still mutable to allow updates
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(private val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationItem) {
            Log.d("NotificationAdapter", "Binding notification: $notification")
            binding.notificationTitle.text = notification.title
            binding.notificationMessage.text = notification.message
            binding.notificationDate.text = notification.date // Assuming you have a TextView for the date
        }
    }


    // Method to update the list and notify adapter
    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

}
