package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Budget.EditBudgetFragment
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.BudgetItemBinding

class BudgetAdapter(
    private val context: Context,
    private val budgets: List<BudgetItem>,
    private val onEditClick: (BudgetItem) -> Unit,
    private val onDeleteClick: (BudgetItem) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(private val binding: BudgetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(budget: BudgetItem) {
            // Set text for each TextView from BudgetItem
            binding.budgetCategory.text = budget.category
            binding.budgetItemName.text = budget.name
            binding.budgetAmount.text = "RM ${budget.amount}"
            binding.budgetStartDate.text = budget.startDate
            binding.budgetEndDate.text = budget.endDate

            // Update the ProgressBar
            binding.budgetProgressBar.progress = budget.progress
            // Set the progress as a percentage in usageProgress TextView
            binding.usageProgress.text = "${budget.progress}%"

            // Edit button click listener
            binding.editBudgetBtn.setOnClickListener {
                // Convert context to FragmentActivity to get FragmentManager
                if (context is androidx.fragment.app.FragmentActivity) {
                    val fragment = EditBudgetFragment.newInstance(budget.id)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment) // Replace with your actual container ID
                        .addToBackStack(null)
                        .commit()
                }
            }

            // Delete button click listener
            binding.deleteBudgetBtn.setOnClickListener {
                onDeleteClick(budget)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = BudgetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount(): Int = budgets.size
}
