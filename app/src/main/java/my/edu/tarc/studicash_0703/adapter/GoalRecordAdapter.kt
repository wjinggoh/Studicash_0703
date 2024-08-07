import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.GoalRecord
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.GoalRecordItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoalRecordAdapter(
    private val context: Context,
    private var goalRecords: MutableList<GoalRecord>,
    private val listener: OnGoalRecordClickListener
) : RecyclerView.Adapter<GoalRecordAdapter.GoalRecordViewHolder>() {

    interface OnGoalRecordClickListener {
        fun onDelete(goalRecordId: String, goalName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalRecordViewHolder {
        val binding = GoalRecordItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return GoalRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalRecordViewHolder, position: Int) {
        val goalRecord = goalRecords[position]
        holder.bind(goalRecord)
    }

    override fun getItemCount(): Int {
        return goalRecords.size
    }

    fun updateData(newGoalRecords: MutableList<GoalRecord>) {
        val sortedRecords = newGoalRecords.sortedWith { o1, o2 ->
            val date1 = o1.saveDate?.toDate() ?: Date(Long.MAX_VALUE)
            val date2 = o2.saveDate?.toDate() ?: Date(Long.MAX_VALUE)
            date1.compareTo(date2)
        }

        goalRecords.clear()
        goalRecords.addAll(sortedRecords)
        notifyDataSetChanged()
    }

    inner class GoalRecordViewHolder(private val binding: GoalRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.goalRecordDeleteBtn.setOnClickListener {
                val goalRecord = goalRecords[adapterPosition]
                listener.onDelete(goalRecord.goalRecordId, goalRecord.goal)
            }
        }

        fun bind(goalRecord: GoalRecord) {
            binding.goalRecordName.text = goalRecord.goal
            binding.goalRecordAmount.text = String.format("%.2f", goalRecord.saveAmount)
            goalRecord.saveDate?.let {
                binding.goalRecordDate.text = formatDate(it)
            } ?: run {
                binding.goalRecordDate.text = "Invalid Date"
            }
            binding.goalRecordIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.goal)) // Adjust with actual icon resource if different
        }

        private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = timestamp.toDate()
            return sdf.format(date)
        }
    }
}
