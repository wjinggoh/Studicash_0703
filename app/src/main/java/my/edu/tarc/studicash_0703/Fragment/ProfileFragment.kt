package my.edu.tarc.studicash_0703.Fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.studicash_0703.EditProfileActivity
import my.edu.tarc.studicash_0703.Models.User
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentProfileBinding
import my.edu.tarc.studicash_0703.sidebar.NotificationsActivity
import my.edu.tarc.studicash_0703.utils.USER_NODE
import java.text.DecimalFormat

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val CHANNEL_ID = "balance_notification_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.editBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.notificationBtn.setOnClickListener {
            val intent = Intent(requireContext(), NotificationsActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        fetchUserProfile()
        fetchTransactions()
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection(USER_NODE).document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    user?.let {
                        binding.userName.text = it.name
                        binding.email.text = it.email
                        binding.profileGender.text = it.gender
                        if (!it.image.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(it.image)
                                .placeholder(R.drawable.profile_user__128dp)
                                .error(R.drawable.profile_user__128dp)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(binding.profileImage)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to fetch user profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchTransactions() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            var totalIncome = 0.0
            var totalExpenses = 0.0

            // Fetch income transactions
            firestore.collection("incomeTransactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val amount = document.getDouble("amount") ?: 0.0
                        totalIncome += amount
                    }

                    // Fetch expense transactions
                    firestore.collection("expenseTransactions")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot.documents) {
                                val amount = document.getDouble("amount") ?: 0.0
                                totalExpenses += amount
                            }

                            // Calculate balance
                            val balance = totalIncome - totalExpenses
                            val df = DecimalFormat("0.00")

                            // Display balance
                            binding.profileBalance.text = df.format(balance)
                            binding.profileTotalIncome.text = df.format(totalIncome)
                            binding.profileTotalExpenses.text = df.format(totalExpenses)

                            // Check if balance is negative and send notification
                            if (balance < 0) {
                                sendNotification(balance)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Failed to fetch expense transactions: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to fetch income transactions: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendNotification(balance: Double) {
        createNotificationChannel()

        val intent = Intent(requireContext(), ProfileFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Balance Alert")
            .setContentText("Your balance is negative: $balance, please be alert!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(requireContext())) {
            notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Balance Notification Channel"
            val descriptionText = "Channel for balance notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
