package com.example.mybabyvaxadmin.pages

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.FragmentHomePageBinding
import com.example.mybabyvaxadmin.models.Users
import com.example.iptfinal.services.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class homePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), android.R.color.white)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val sessionManager = SessionManager(requireContext())

        if (currentUser == null) {
            binding.username.text = "No user found"
            return
        }

        val userId = currentUser.uid


        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(Users::class.java)

            if (user != null) {
                sessionManager.saveUser(user)

                val username = if (user.firstname.isNotEmpty() || user.lastname.isNotEmpty()) {
                    "${user.firstname} ${user.lastname}"
                } else {
                    "Admin"
                }
                binding.username.text = username


                if (user.profilePic.isNotEmpty()) {
                    try {
                        if (user.profilePic.startsWith("/9j") || user.profilePic.contains("base64")) {
                            val imageBytes = android.util.Base64.decode(
                                user.profilePic,
                                android.util.Base64.DEFAULT
                            )
                            val bitmap =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.profileImage.setImageBitmap(bitmap)
                        } else {
                            Glide.with(this)
                                .load(user.profilePic)
                                .placeholder(R.drawable.default_profile)
                                .into(binding.profileImage)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.profileImage.setImageResource(R.drawable.default_profile)
                    }
                } else {
                    binding.profileImage.setImageResource(R.drawable.default_profile)
                }
            } else {
                binding.username.text = "Unknown Admin"
            }
        }.addOnFailureListener {
            binding.username.text = "Failed to load Admin info"
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
