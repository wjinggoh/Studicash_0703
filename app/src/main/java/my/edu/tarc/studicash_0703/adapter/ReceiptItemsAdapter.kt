package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.ReceiptItems
import my.edu.tarc.studicash_0703.databinding.ReceiptItemsBinding

class ReceiptItemsAdapter(private val items: List<ReceiptItems>) :
    RecyclerView.Adapter<ReceiptItemsAdapter.ReceiptItemsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemsViewHolder {
        val binding = ReceiptItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptItemsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ReceiptItemsViewHolder(private val binding: ReceiptItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReceiptItems) {
            binding.itemName.text = item.itemName
            binding.itemAmount.text = item.itemAmount.toString()
        }
    }
}
