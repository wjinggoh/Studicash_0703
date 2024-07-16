package my.edu.tarc.studicash_0703

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.studicash_0703.Models.Insight
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.adapter.InsightAdapter
import my.edu.tarc.studicash_0703.databinding.ActivityReportBinding
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        setupSpinner()
        binding.insightsRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.reportBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.timeframe_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.timeFrameSpinner.adapter = adapter

        binding.timeFrameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedTimeframe = parent.getItemAtPosition(position).toString()
                fetchExpenses(selectedTimeframe)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchExpenses(timeframe: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        val calendar = Calendar.getInstance()
        when (timeframe) {
            "Daily" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "Monthly" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "Yearly" -> {
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            else -> {
                Log.e("ReportActivity", "Invalid timeframe")
                return
            }
        }

        val startDate = calendar.time
        Log.d("Firestore", "Start Date: $startDate")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedStartDate = dateFormat.format(startDate)

        firestore.collection("expenseTransactions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", formattedStartDate)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Firestore", "No expenses found for $timeframe")
                    showNoExpensesMessage(timeframe)
                } else {
                    val categoryTotals = mutableMapOf<String, Double>()

                    documents.forEach { document ->
                        val transaction = document.toObject(Transaction::class.java)
                        if (transaction.isExpense) {
                            val currentAmount = categoryTotals[transaction.category] ?: 0.0
                            categoryTotals[transaction.category] = currentAmount + transaction.amount
                        }
                    }

                    Log.d("Firestore", "Category Totals: $categoryTotals")

                    // Prepare insights data
                    val insights = categoryTotals.map { Insight(it.key, it.value) }

                    // Update RecyclerView
                    binding.insightsRecyclerView.visibility = View.VISIBLE
                    binding.insightsRecyclerView.adapter = InsightAdapter(insights)

                    // Update Pie Chart
                    updatePieChart(categoryTotals)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching expenses", exception)
            }
    }

    private fun showNoExpensesMessage(timeframe: String) {
        binding.pieChart.visibility = View.GONE // Hide the pie chart
        binding.noExpensesTextView.visibility = View.VISIBLE // Show the no expenses message
        binding.noExpensesTextView.text = "No expenses available for $timeframe."
        binding.insightsRecyclerView.visibility = View.GONE // Hide the RecyclerView
    }

    private fun updatePieChart(categoryTotals: Map<String, Double>) {
        binding.noExpensesTextView.visibility = View.GONE // Hide the no expenses message
        binding.pieChart.visibility = View.VISIBLE // Show the pie chart

        val entries = categoryTotals.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Expenses").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 16f
            valueTextColor = Color.WHITE
        }
        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            this.data = pieData
            description.isEnabled = false
            setEntryLabelColor(Color.BLACK)
            animateY(1400)
            invalidate() // Refresh the chart
        }
    }
}
