package my.edu.tarc.studicash_0703.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import my.edu.tarc.studicash_0703.EditProfile
import my.edu.tarc.studicash_0703.Models.User
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.databinding.FragmentProfileBinding
import my.edu.tarc.studicash_0703.utils.USER_NODE

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.editBtn.setOnClickListener{
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    companion object {
    }

    override fun onStart() {
        super.onStart()
        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject<User>()
                user?.let {
                    binding.userName.text = it.name
                    binding.email.text = it.email
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


    }
}