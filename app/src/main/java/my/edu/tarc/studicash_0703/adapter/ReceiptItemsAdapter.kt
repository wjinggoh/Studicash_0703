package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.databinding.ReceiptItemsBinding
import my.edu.tarc.studicash_0703.Models.ReceiptItems

class ReceiptItemsAdapter(private val receiptItems: List<ReceiptItems>) : RecyclerView.Adapter<ReceiptItemsAdapter.ReceiptItemViewHolder>() {

    inner class ReceiptItemViewHolder(private val binding: ReceiptItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(receiptItem: ReceiptItems) {
            binding.itemName.text = receiptItem.itemName
            binding.itemAmount.text = receiptItem.itemAmount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemViewHolder {
        val binding = ReceiptItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptItemViewHolder, position: Int) {
        holder.bind(receiptItems[position])
    }

    override fun getItemCount(): Int {
        return receiptItems.size
    }
}
