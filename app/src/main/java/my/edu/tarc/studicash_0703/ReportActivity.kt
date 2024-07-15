package my.edu.tarc.studicash_0703

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.databinding.ActivityReportBinding

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.reportBackBtn.setOnClickListener {
            onBackPressed()
        }

        fetchExpensesAndDisplayChart()
    }

    private fun fetchExpensesAndDisplayChart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Log.d("UserID", "User not logged in.")
            return
        }

        firestore.collection("expenseTransactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FirestoreData", "Fetched ${documents.size()} documents.")
                val expenseMap = mutableMapOf<String, Float>()
                var totalAmount = 0f

                for (document in documents) {
                    val category = document.getString("category") ?: continue
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    totalAmount += amount
                    expenseMap[category] = expenseMap.getOrDefault(category, 0f) + amount
                }

                // Create PieEntries
                val entries = ArrayList<PieEntry>()
                for ((category, amount) in expenseMap) {
                    if (totalAmount > 0) {
                        val percentage = (amount / totalAmount) * 100
                        entries.add(PieEntry(percentage, category))
                    }
                }

                // Set up the PieChart
                if (entries.isNotEmpty()) {
                    setupPieChart(entries)
                } else {
                    Log.d("PieChart", "No data available")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FetchData", "Error getting documents: ", exception)
            }
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "Expense Categories")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.invalidate() // Refresh

        // Set the value formatter
        data.setValueFormatter(PercentFormatter())

        // Optional customizations
        binding.pieChart.description.isEnabled = false
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(Color.WHITE)
        binding.pieChart.setTransparentCircleColor(Color.WHITE)
        binding.pieChart.setTransparentCircleAlpha(110)
        binding.pieChart.holeRadius = 58f
        binding.pieChart.transparentCircleRadius = 61f
        binding.pieChart.setDrawCenterText(true)
        binding.pieChart.centerText = "Expenses"
        binding.pieChart.setCenterTextSize(24f)
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.setEntryLabelTextSize(12f)
    }
}
