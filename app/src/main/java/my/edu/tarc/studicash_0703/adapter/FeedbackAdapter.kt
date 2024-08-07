package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Feedback
import my.edu.tarc.studicash_0703.databinding.ItemFeedbackBinding

class FeedbackAdapter(
    private val feedbackList: List<Feedback>,
    private val onItemClicked: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val binding = ItemFeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedbackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.bind(feedback)
    }

    override fun getItemCount(): Int {
        return feedbackList.size
    }

    inner class FeedbackViewHolder(private val binding: ItemFeedbackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feedback: Feedback) {
            binding.emailTextView.text = feedback.email
            binding.feedbackTextView.text = feedback.feedbackText
            binding.root.setOnClickListener {
                onItemClicked(feedback)
            }
        }
    }
}
