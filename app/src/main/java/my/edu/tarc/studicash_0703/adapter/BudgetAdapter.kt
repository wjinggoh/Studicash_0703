import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Budget.EditBudgetActivity
import my.edu.tarc.studicash_0703.Models.BudgetItem
import my.edu.tarc.studicash_0703.databinding.BudgetItemBinding

class BudgetAdapter(
    private val context: Context,
    private val budgets: List<BudgetItem>,
    private val onEditClick: (BudgetItem) -> Unit,
    private val onDeleteClick: (BudgetItem) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(private val binding: BudgetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(budget: BudgetItem) {
            binding.budgetCategory.text = budget.category
            binding.budgetItemName.text = budget.name
            binding.budgetAmount.text = "RM ${budget.amount}"
            binding.budgetStartDate.text = budget.startDate
            binding.budgetEndDate.text = budget.endDate
            binding.budgetProgressBar.progress = budget.progress
            binding.usageProgress.text = "${budget.progress}%"

            binding.editBudgetBtn.setOnClickListener {
                // Pass only the budget ID for editing
                val intent = Intent(context, EditBudgetActivity::class.java).apply {
                    putExtra("budgetId", budget.id)
                }
                context.startActivity(intent)
            }

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
