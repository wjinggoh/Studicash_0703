package my.edu.tarc.studicash_0703.sidebar

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.FAQItem
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.adapter.FAQAdapter

class HelpActivity : AppCompatActivity() {
    private lateinit var faqRecyclerView: RecyclerView

    private val faqList = listOf(
        FAQItem("What is Studicash?", "Studicash is an app that helps you manage your finances."),
        FAQItem("How to create an account?", "You can create an account by clicking on the Register button on the home screen."),
        FAQItem("How can I reset my password?", "You can reset your password by clicking on the Forgot Password"),
        FAQItem("How can I track my expenses?","You can track your expenses usage percentage by clicking on the Report button on the home screen. To add your expenses, you can click on the Add button which is the second button on the bottom navigation bar and lastly click on the Create Transaction to choose if you want to create new transaction or direct scan receipt images for auto-input.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        faqRecyclerView = findViewById(R.id.faqRecyclerView)
        faqRecyclerView.layoutManager = LinearLayoutManager(this)
        faqRecyclerView.adapter = FAQAdapter(faqList)

        findViewById<ImageView>(R.id.helpBackBtn).setOnClickListener {
            onBackPressed()
        }
    }
}
