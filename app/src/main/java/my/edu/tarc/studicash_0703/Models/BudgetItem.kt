package my.edu.tarc.studicash_0703.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class BudgetItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    var spent: Double = 0.0,
    var progress: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val icon: Int = 0
)



