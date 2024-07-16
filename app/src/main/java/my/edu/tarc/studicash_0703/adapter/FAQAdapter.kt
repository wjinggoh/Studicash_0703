package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.FAQItem
import my.edu.tarc.studicash_0703.R

class FAQAdapter(private val faqList: List<FAQItem>) : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        private val answerTextView: TextView = itemView.findViewById(R.id.answerTextView)

        fun bind(faqItem: FAQItem) {
            questionTextView.text = faqItem.question
            answerTextView.text = faqItem.answer
            answerTextView.visibility = if (faqItem.isExpanded) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                faqItem.isExpanded = !faqItem.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.faq_item, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.bind(faqList[position])
    }

    override fun getItemCount() = faqList.size
}
