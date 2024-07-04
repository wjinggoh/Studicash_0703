package my.edu.tarc.studicash_0703.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentAddBinding
import my.edu.tarc.studicash_0703.databinding.FragmentProfileBinding

class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Set click listener for createTransactionbtn
        binding.createTransactionbtn.setOnClickListener {
            findNavController().navigate(R.id.action_addFragment_to_chooseFragment)
        }

        return binding.root
    }

    companion object {

    }
}