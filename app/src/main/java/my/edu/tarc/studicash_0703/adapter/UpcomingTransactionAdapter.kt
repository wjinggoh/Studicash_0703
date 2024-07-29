package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ItemUpcomingTransactionBinding

class UpcomingTransactionAdapter(private val transactionList: List<Transaction>) :
    RecyclerView.Adapter<UpcomingTransactionAdapter.UpcomingTransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingTransactionViewHolder {
        val binding = ItemUpcomingTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UpcomingTransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpcomingTransactionViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    override fun getItemCount(): Int = transactionList.size

    inner class UpcomingTransactionViewHolder(private val binding: ItemUpcomingTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTransactionName.text = transaction.title
            binding.tvTransactionAmount.text = transaction.amount.toString()
            binding.tvTransactionDate.text = transaction.date.toString()
        }
    }
}
