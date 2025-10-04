package com.example.mybabyvaxadmin.pages

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.FragmentHomePageBinding
import com.example.mybabyvaxadmin.models.Users
import com.example.iptfinal.services.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class homePage : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

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
        sessionManager = SessionManager(requireContext())

        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.username.text = "No user found"
            return
        }

        val userId = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)


        viewLifecycleOwner.lifecycleScope.launch {
            val user: Users? = try {
                withContext(Dispatchers.IO) {
                    val snapshot = userRef.get().await()
                    snapshot.getValue(Users::class.java)
                }
            } catch (e: Exception) {
                Log.e("HomePage", "Failed to get user", e)
                null
            }


            if (_binding == null) return@launch

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

                            val bitmap = withContext(Dispatchers.IO) {
                                try {
                                    val imageBytes = Base64.decode(user.profilePic, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                            }


                            if (_binding != null) bitmap?.let {
                                binding.profileImage.setImageBitmap(
                                    it
                                )
                            }

                        } else {
                            Glide.with(this@homePage)
                                .load(user.profilePic)
                                .placeholder(R.drawable.default_profile)
                                .into(binding.profileImage)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (_binding != null) binding.profileImage.setImageResource(R.drawable.default_profile)
                    }
                } else {
                    binding.profileImage.setImageResource(R.drawable.default_profile)
                }
            } else {
                binding.username.text = "Unknown Admin"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
