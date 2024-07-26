package my.edu.tarc.studicash_0703

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.GoalItem
import my.edu.tarc.studicash_0703.databinding.GoalItemBinding

class GoalAdapter(
    private val goals: List<GoalItem>,
    private val onEditClick: (GoalItem) -> Unit,
    private val onDeleteClick: (GoalItem) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = GoalItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.bind(goal)
    }

    inner class GoalViewHolder(private val binding: GoalItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(goal: GoalItem) {
            binding.goalItemName.text = goal.name
            binding.goalAmt.text = goal.amount.toString()
            binding.GoalSavedAmt.text = goal.saved.toString()
            binding.goalProgressBar.progress = goal.progress
            binding.goalProgress.text = "${goal.progress}%"

            // Bind startDate and endDate
            binding.goalStartDate.text = goal.startDate
            binding.goalEndDate.text = goal.endDate

            binding.deletegoalBtn.setOnClickListener {
                onDeleteClick(goal)
            }
            binding.editgoalBtn.setOnClickListener {
                onEditClick(goal)
            }
        }
    }

    override fun getItemCount(): Int = goals.size
}
