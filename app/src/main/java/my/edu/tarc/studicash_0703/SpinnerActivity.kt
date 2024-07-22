package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SpinnerActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spinner)

        spinner = findViewById(R.id.spinner)

        // Set up Spinner with items
        val items = arrayOf("Goal Tracking", "Budget Tracking")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set default selection to Budget Tracking
        spinner.setSelection(1)

        // Set up Spinner item selection listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> openGoalTrackingActivity()
                    1 -> openBudgetTrackingActivity()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when no item is selected (if necessary)
            }
        }
    }

    private fun openGoalTrackingActivity() {
        val intent = Intent(this, GoalTrackingActivity::class.java)
        startActivity(intent)
    }

    private fun openBudgetTrackingActivity() {
        val intent = Intent(this, BudgetTrackingActivity::class.java)
        startActivity(intent)
    }
}
