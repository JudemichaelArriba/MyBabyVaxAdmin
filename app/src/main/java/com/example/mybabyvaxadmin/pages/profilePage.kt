package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.os.Bundle
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
    private lateinit var sessionManager: SessionManager

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
        sessionManager = SessionManager(requireContext())


        val cachedUser = sessionManager.getUser()
        if (cachedUser != null) {
            loadUser(cachedUser)
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef =
                FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)


            viewLifecycleOwner.lifecycleScope.launch {
                val user: Users? = try {
                    withContext(Dispatchers.IO) {
                        val snapshot = userRef.get().await()
                        snapshot.getValue(Users::class.java)
                    }
                } catch (e: Exception) {
                    Log.e("ProfilePage", "Failed to fetch user", e)
                    null
                }

                if (_binding != null && user != null) {
                    sessionManager.saveUser(user)
                    loadUser(user)
                }
            }
        } else if (_binding != null) {
            binding.username.text = "Unknown Admin"
            binding.emailText.text = "Not available"
            binding.profileImage.setImageResource(R.drawable.default_profile)
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

    private fun loadUser(user: Users) {
        binding.username.text = "${user.firstname} ${user.lastname}"
        binding.emailText.text = user.email

        val imageUrl =
            if (user.profilePic.startsWith("/9j") || user.profilePic.contains("base64")) {
                "data:image/jpeg;base64,${user.profilePic}"
            } else user.profilePic

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.default_profile)
            .into(binding.profileImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
