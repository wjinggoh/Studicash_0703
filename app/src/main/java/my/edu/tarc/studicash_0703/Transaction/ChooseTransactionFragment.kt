package my.edu.tarc.studicash_0703.Transaction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import my.edu.tarc.studicash_0703.AddGoalRecordActivity
import my.edu.tarc.studicash_0703.AddReceiptActivity
import my.edu.tarc.studicash_0703.databinding.FragmentChooseTransactionBinding

class ChooseTransactionFragment : DialogFragment() {
    private lateinit var binding: FragmentChooseTransactionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChooseTransactionBinding.inflate(inflater, container, false)

        binding.addTransactionButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
        }

        binding.addReceiptButton.setOnClickListener{
            startActivity(Intent(requireContext(), AddReceiptActivity::class.java))
        }

        binding.addGoalRecordButton.setOnClickListener{
            startActivity(Intent(requireContext(), AddGoalRecordActivity::class.java))

        }



        return binding.root
    }
}