package my.edu.tarc.studicash_0703.budget

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Budget
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.ActivityBudgetTrackingBinding
import org.json.JSONObject
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

        binding.addBudgetBtn.setOnClickListener{
            val intent = Intent(this, AddBudgetActivity::class.java)
            startActivity(intent)
        }

        binding.BudgetBackBtn.setOnClickListener {
            onBackPressed()
        }
        loadBudgetsFromPreferences()
        loadTransactions()
    }

    private fun loadBudgetsFromPreferences() {
        val sharedPreferences = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)
        val budgetSet = sharedPreferences.getStringSet("BudgetList", setOf()) ?: setOf()

        budgetList = budgetSet.mapNotNull { budgetJson ->
            try {
                val jsonObject = JSONObject(budgetJson)
                val amount = jsonObject.getDouble("amount")
                val category = jsonObject.getString("category")
                val startDate = jsonObject.getString("startDate")
                val endDate = jsonObject.getString("endDate")
                Budget(amount, category, startDate, endDate)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        setupBarChartIfReady()
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
                        userId = doc.getString("userId") ?: "",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    ).takeIf { it.amount > 0 }
                }
                setupBarChartIfReady()
            }
            .addOnFailureListener { exception ->
                Log.e("BudgetTrackingActivity", "Error loading transactions: ", exception)
            }
    }

    private fun setupBarChartIfReady() {
        if (::budgetList.isInitialized && ::transactionList.isInitialized) {
            setupBarChart()
        }
    }

    private fun setupBarChart() {
        val categoryUsage = calculateCategoryUsage()

        val entries = budgetList.mapIndexed { index, budget ->
            val usage = categoryUsage[budget.category] ?: 0.0
            BarEntry(index.toFloat(), (usage / budget.amount * 100).toFloat()) // Calculate percentage usage
        }

        val dataSet = BarDataSet(entries, "Budget Usage (%)").apply {
            color = Color.BLUE
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        // Chart configuration
        barChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            xAxis.labelCount = budgetList.size
            xAxis.valueFormatter = IndexAxisValueFormatter(budgetList.map { it.category }) // Set X-axis labels
            invalidate() // Refresh the chart
        }
    }

    private fun calculateCategoryUsage(): Map<String, Double> {
        val categoryUsage = HashMap<String, Double>()

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
