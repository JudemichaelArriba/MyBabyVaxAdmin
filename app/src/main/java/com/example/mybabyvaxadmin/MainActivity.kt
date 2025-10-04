@file:Suppress("DEPRECATION")

package com.example.mybabyvaxadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.services.SessionManager
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.components.bottomNav
import com.example.mybabyvaxadmin.databinding.ActivityMainBinding
import com.example.mybabyvaxadmin.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")
        sessionManager = SessionManager(this)


        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, bottomNav::class.java)
            startActivity(intent)
            finish()
            return
        }


        binding.loginBtn.setOnClickListener {
            val email = binding.emailTv.text.toString().trim()
            val password = binding.passwordTv.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                DialogHelper.showWarning(
                    this,
                    "Missing Fields",
                    "Please enter both email and password."
                )
                return@setOnClickListener
            }

            binding.loadingOverlay.visibility = View.VISIBLE


            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid ?: return@addOnSuccessListener

                    database.child(userId).get().addOnSuccessListener { snapshot ->
                        val user = snapshot.getValue(Users::class.java)

                        if (user != null && user.role == "Admin") {

                            sessionManager.saveUser(user)
                            binding.loadingOverlay.visibility = View.GONE

                            DialogHelper.showSuccess(
                                this,
                                "Login Successful",
                                "Welcome back, ${user.firstname}!"
                            ) {
                                val intent = Intent(this, bottomNav::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } else {

                            FirebaseAuth.getInstance().signOut()
                            binding.loadingOverlay.visibility = View.GONE
                            DialogHelper.showError(
                                this,
                                "Access Denied",
                                "Only admin accounts can access this application."
                            )
                        }

                    }.addOnFailureListener {
                        binding.loadingOverlay.visibility = View.GONE
                        DialogHelper.showError(
                            this,
                            "Database Error",
                            "Failed to load user data. Please try again."
                        )
                    }
                }
                .addOnFailureListener { e ->
                    binding.loadingOverlay.visibility = View.GONE
                    DialogHelper.showError(
                        this,
                        "Login Failed",
                        e.message ?: "An unknown error occurred. Please try again."
                    )
                }
        }
    }
}
