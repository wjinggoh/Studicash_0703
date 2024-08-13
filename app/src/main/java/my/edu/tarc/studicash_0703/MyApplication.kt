package my.edu.tarc.studicash_0703

import BalanceCheckWorker
import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val workRequest = PeriodicWorkRequestBuilder<BalanceCheckWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
