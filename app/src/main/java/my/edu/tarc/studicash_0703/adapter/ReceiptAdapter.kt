package my.edu.tarc.studicash_0703.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Receipt
import my.edu.tarc.studicash_0703.ReceiptDetailActivity
import my.edu.tarc.studicash_0703.databinding.ReceiptItemsBinding

class ReceiptAdapter(private var receipts: List<Receipt>) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    inner class ReceiptViewHolder(private val binding: ReceiptItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(receipt: Receipt) {
            binding.itemType.text = receipt.type
            binding.itemTotal.text = receipt.total
            binding.viewReceiptDetails.setOnClickListener {
                val intent= Intent(binding.root.context, ReceiptDetailActivity::class.java)
                intent.putExtra("Receipt",receipt)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ReceiptItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(receipts[position])
    }

    fun updateReceipts(newReceipts: List<Receipt>) {
        receipts = newReceipts
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = receipts.size
}
