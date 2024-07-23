package my.edu.tarc.studicash_0703.sidebar

import my.edu.tarc.studicash_0703.ExpenseNotificationWorker
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import my.edu.tarc.studicash_0703.databinding.ActivityNotificationsBinding
import java.util.concurrent.TimeUnit

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.notificationBackBtn.setOnClickListener {
            onBackPressed()
        }

        // Schedule the Worker to run daily
        val workRequest = PeriodicWorkRequestBuilder<ExpenseNotificationWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
