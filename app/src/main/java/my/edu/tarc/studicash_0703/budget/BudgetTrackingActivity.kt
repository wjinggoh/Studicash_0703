package my.edu.tarc.studicash_0703.budget

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Budget
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityBudgetTrackingBinding
import java.util.*
import kotlin.collections.HashMap

class BudgetTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetTrackingBinding
    private lateinit var barChart: BarChart
    private lateinit var budgetList: List<Budget>
    private lateinit var transactionList: List<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barChart = binding.barChart

        loadBudgetsFromPreferences()
        loadTransactions()

        binding.addBudgetBtn.setOnClickListener {
            val intent = Intent(this, AddBudgetActivity::class.java)
            startActivity(intent)
        }

        binding.BudgetBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBudgetsFromPreferences() {
        val db = FirebaseFirestore.getInstance()
        db.collection("budgets").get()
            .addOnSuccessListener { documents ->
                budgetList = documents.mapNotNull { doc ->
                    Budget(
                        category = doc.getString("category") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: ""
                    )
                }
                setupBarChartIfReady()
            }
            .addOnFailureListener { exception ->
                Log.e("BudgetTrackingActivity", "Error loading budgets: ", exception)
                Toast.makeText(this, "Error loading budgets", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTransactions() {
        val db = FirebaseFirestore.getInstance()
        db.collection("transactions").get()
            .addOnSuccessListener { documents ->
                transactionList = documents.mapNotNull { doc ->
                    Transaction(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        date = doc.getString("date") ?: "",
                        paymentMethod = doc.getString("paymentMethod") ?: "",
                        paymentMethodDetails = doc.getString("paymentMethodDetails"),
                        expense = doc.getBoolean("expense") ?: true,
                        userId = doc.getString("userId") ?: ""
                    ).takeIf { it.amount > 0 }
                }
                setupBarChartIfReady()
            }
            .addOnFailureListener { exception ->
                Log.e("BudgetTrackingActivity", "Error loading transactions: ", exception)
                Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBarChartIfReady() {
        if (::budgetList.isInitialized && ::transactionList.isInitialized) {
            setupBarChart()
        }
    }

    private fun setupBarChart() {
        // Calculate category usage
        val categoryUsage = calculateCategoryUsage()

        // Prepare entries for the bar chart
        val entries = budgetList.mapIndexed { index, budget ->
            val usage = categoryUsage[budget.category] ?: 0.0
            BarEntry(index.toFloat(), (usage / budget.amount * 100).toFloat()) // Calculate percentage usage
        }

        // Create a dataset for the bar chart
        val dataSet = BarDataSet(entries, "Budget Usage (%)").apply {
            color = getColor(R.color.blue)
        }

        // Create bar data and set it to the chart
        val barData = BarData(dataSet)
        barChart.data = barData

        // Customize chart appearance
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            animateY(1000)
            legend.isEnabled = false

            // Customize X axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(budgetList.map { it.category })
                granularity = 1f
            }

            // Customize Y axis
            axisLeft.apply {
                setDrawGridLines(true)
            }

            // Refresh chart
            invalidate()
        }
    }

    private fun calculateCategoryUsage(): Map<String, Double> {
        val categoryUsage = HashMap<String, Double>()

        // Calculate usage for each budget category based on transactions
        budgetList.forEach { budget ->
            val startDate = parseDate(budget.startDate)
            val endDate = parseDate(budget.endDate)
            var totalUsage = 0.0

            transactionList.forEach { transaction ->
                val transactionDate = parseDate(transaction.date)
                if (transaction.category == budget.category && transaction.expense &&
                    (transactionDate.after(startDate) && transactionDate.before(endDate) ||
                            transactionDate == startDate || transactionDate == endDate)) {
                    totalUsage += transaction.amount
                }
            }

            categoryUsage[budget.category] = totalUsage
        }

        return categoryUsage
    }

    private fun parseDate(dateString: String): Date {
        val parts = dateString.split("-")
        val calendar = Calendar.getInstance()
        calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        return calendar.time
    }
}
