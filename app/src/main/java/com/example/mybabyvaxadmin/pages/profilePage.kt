package com.example.mybabyvaxadmin.pages

import android.content.Intent
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
import com.example.mybabyvaxadmin.MainActivity
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.databinding.FragmentProfilePageBinding
import com.example.mybabyvaxadmin.models.Users
import com.example.iptfinal.services.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class profilePage : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
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
            binding.username.text = "Unknown Admin"
            binding.emailText.text = "Not available"
            return
        }

        val userId = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)


        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snapshot = withContext(Dispatchers.IO) { userRef.get().await() }
                val user = snapshot.getValue(Users::class.java)

                if (user != null && isAdded && _binding != null) {
                    sessionManager.saveUser(user)
                    binding.username.text = "${user.firstname} ${user.lastname}"
                    binding.emailText.text = user.email

                    Log.d(
                        "AdminProfile",
                        "Admin Data Loaded: ${user.firstname} ${user.lastname} - ${user.email}"
                    )

                    if (user.profilePic.isNotEmpty()) {
                        val bitmap = withContext(Dispatchers.IO) {
                            try {
                                if (user.profilePic.startsWith("/9j") || user.profilePic.contains("base64")) {
                                    val imageBytes = Base64.decode(user.profilePic, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                } else null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }

                        if (_binding != null) {
                            if (bitmap != null) {
                                binding.profileImage.setImageBitmap(bitmap)
                            } else {
                                Glide.with(this@profilePage)
                                    .load(user.profilePic)
                                    .placeholder(R.drawable.default_profile)
                                    .into(binding.profileImage)
                            }
                        }
                    } else {
                        if (_binding != null) binding.profileImage.setImageResource(R.drawable.default_profile)
                    }
                } else {
                    if (_binding != null) {
                        binding.username.text = "Admin"
                        binding.emailText.text = "No email found"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_binding != null) {
                    binding.username.text = "Failed to load profile"
                    binding.emailText.text = ""
                }
            }
        }

        binding.logout.setOnClickListener {
            DialogHelper.showWarning(
                requireContext(),
                "Logout",
                "Are you sure you want to log out?",
                onConfirm = {
                    auth.signOut()
                    sessionManager.clearSession()

                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                },
                onCancel = {}
            )
        }

        binding.card1.setOnClickListener {
            startActivity(Intent(requireContext(), AccountInfoPage::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
