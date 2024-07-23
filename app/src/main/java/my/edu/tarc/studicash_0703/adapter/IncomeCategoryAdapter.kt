package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.EditIncomeCategoryActivity
import my.edu.tarc.studicash_0703.Models.IncomeCategory
import my.edu.tarc.studicash_0703.databinding.CategoryItemBinding

class IncomeCategoryAdapter(
    private val context: Context,
    private val incomeCategories: MutableList<IncomeCategory>
) : RecyclerView.Adapter<IncomeCategoryAdapter.IncomeCategoryViewHolder>() {

    inner class IncomeCategoryViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeCategoryViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return IncomeCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomeCategoryViewHolder, position: Int) {
        val category = incomeCategories[position]
        holder.binding.categoryIcon.setImageResource(category.icon)
        holder.binding.categoryName.text = category.name

        holder.binding.editCategoryButton.setOnClickListener {
            // Implement edit functionality
            if (context is EditIncomeCategoryActivity) {
                // context.updateCategory(updatedCategory, position)
            }
        }

        holder.binding.deleteCategoryButton.setOnClickListener {
            if (context is EditIncomeCategoryActivity) {
                context.deleteCategory(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return incomeCategories.size
    }
}