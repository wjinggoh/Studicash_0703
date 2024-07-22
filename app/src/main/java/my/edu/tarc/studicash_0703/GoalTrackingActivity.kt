package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import my.edu.tarc.studicash_0703.databinding.ActivityGoalTrackingBinding

class GoalTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = arrayOf("Goal", "Budget")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.goalTrackingSpinner.adapter = adapter

        // Set default selection to Goal
        binding.goalTrackingSpinner.setSelection(0)

        // Set up the OnItemSelectedListener
        binding.goalTrackingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Stay on the GoalTrackingActivity
                        // No action needed as we're already on this page
                    }
                    1 -> {
                        // Switch to BudgetTrackingActivity
                        val intent = Intent(this@GoalTrackingActivity, BudgetTrackingActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: Close this activity if you don't want to return to it
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when no item is selected
            }
        }

        binding.goalTrackingBackBtn.setOnClickListener{
            finish()
        }

        binding.addGoalTrackingBtn.setOnClickListener{
            val intent = Intent(this, CreateGoalActivity::class.java)
            startActivity(intent)
        }
    }
}
