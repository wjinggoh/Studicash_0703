package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.EditExpensesCategoryActivity
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.databinding.CategoryItemBinding

class ExpenseCategoryAdapter(
    private val context: Context,
    private val expenseCategories: MutableList<ExpenseCategory>
) : RecyclerView.Adapter<ExpenseCategoryAdapter.ExpenseCategoryViewHolder>() {

    inner class ExpenseCategoryViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseCategoryViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ExpenseCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseCategoryViewHolder, position: Int) {
        val category = expenseCategories[position]
        holder.binding.categoryIcon.setImageResource(category.icon)
        holder.binding.categoryName.text = category.name

        holder.binding.editCategoryButton.setOnClickListener {
            // Implement edit functionality
            if (context is EditExpensesCategoryActivity) {
                // context.updateCategory(updatedCategory, position)
            }
        }

        holder.binding.deleteCategoryButton.setOnClickListener {
            if (context is EditExpensesCategoryActivity) {
                context.deleteCategory(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return expenseCategories.size
    }
}
