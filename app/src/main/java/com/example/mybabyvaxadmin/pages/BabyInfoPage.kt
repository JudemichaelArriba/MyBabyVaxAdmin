package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.databinding.ActivityBabyInfoPageBinding
import com.example.mybabyvaxadmin.models.Baby
import com.example.mybabyvaxadmin.services.DatabaseService
import com.example.mybabyvaxadmin.interfaces.InterfaceClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BabyInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityBabyInfoPageBinding
    private val databaseService = DatabaseService()
    private lateinit var currentBaby: Baby
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBabyInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val babyId = intent.getStringExtra("baby_id")

        if (babyId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Baby ID missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true)
                currentBaby = fetchBabyDataSuspend(babyId)
                displayBabyInfo(currentBaby)
            } catch (e: Exception) {
                Toast.makeText(
                    this@BabyInfoPage,
                    e.message ?: "Error loading baby information",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }

        binding.backButton.setOnClickListener { finish() }


    }

    private suspend fun fetchBabyDataSuspend(babyId: String): Baby =
        suspendCancellableCoroutine { continuation ->
            databaseService.fetchBabyById(babyId, object : InterfaceClass.BabyCallback {
                override fun onBabyLoaded(baby: Baby) {
                    continuation.resume(baby)
                }

                override fun onError(error: String) {
                    continuation.resumeWithException(Exception(error))
                }
            })
        }

    private suspend fun displayBabyInfo(baby: Baby) = withContext(Dispatchers.Main) {
        binding.scrollView.visibility = View.VISIBLE


        binding.fullNameTextView.text = baby.fullName ?: ""
        binding.genderTextView.text = baby.gender ?: ""
        binding.dateOfBirthTextView.text = baby.dateOfBirth ?: ""
        binding.birthPlaceTextView.text = baby.birthPlace ?: ""
        binding.weightTextView.text = "${baby.weightAtBirth ?: 0.0} kg"
        binding.heightTextView.text = "${baby.heightAtBirth ?: 0} cm"
        binding.bloodTypeTextView.text = baby.bloodType ?: ""

        if (!baby.profileImageUrl.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(baby.profileImageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(this@BabyInfoPage)
                    .load(bitmap)
                    .into(binding.profileImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE


    }
}
