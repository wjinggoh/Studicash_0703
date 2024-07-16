package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Insight
import my.edu.tarc.studicash_0703.R

class InsightAdapter(private val insights: List<Insight>) : RecyclerView.Adapter<InsightAdapter.InsightViewHolder>() {

    inner class InsightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)

        fun bind(insight: Insight) {
            categoryTextView.text = insight.category
            amountTextView.text = insight.totalAmount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.insight_item, parent, false)
        return InsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(insights[position])
    }

    override fun getItemCount() = insights.size
}
