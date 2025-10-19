package com.example.mybabyvaxadmin.pages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybabyvaxadmin.adapters.BabyAdapter
import com.example.mybabyvaxadmin.databinding.ActivityScheduleInfoPageBinding
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.mybabyvaxadmin.models.Baby
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleInfoPageBinding
    private lateinit var babyAdapter: BabyAdapter
    private val databaseService = DatabaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)

        babyAdapter = BabyAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = babyAdapter


        binding.backButton.setOnClickListener { finish() }


        val vaccineName = intent.getStringExtra("vaccineName") ?: "Unknown Vaccine"
        val doseName = intent.getStringExtra("doseName") ?: "Unknown Dose"
        val date = intent.getStringExtra("date") ?: "No Date"
        val babyIds = intent.getStringArrayListExtra("babyIds") ?: arrayListOf()


        binding.vaccineName.text = vaccineName
        binding.vaccineDetails.text = "$doseName • $date"


        loadBabies(babyIds)
    }

    private fun loadBabies(babyIds: List<String>) {
        if (babyIds.isEmpty()) {
            Toast.makeText(this, "No babies found for this schedule", Toast.LENGTH_SHORT).show()
            return
        }

        runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        val babies = mutableListOf<Baby>()

        lifecycleScope.launch(Dispatchers.IO) {

            var completedCount = 0

            for (babyId in babyIds) {
                databaseService.fetchBabyById(babyId, object : InterfaceClass.BabyCallback {
                    override fun onBabyLoaded(baby: Baby) {
                        babies.add(baby)
                        completedCount++
                        if (completedCount == babyIds.size) {
                            updateUI(babies)
                        }
                    }

                    override fun onError(error: String) {
                        completedCount++
                        if (completedCount == babyIds.size) {
                            updateUI(babies)
                        }
                    }
                })
            }
        }
    }

    private fun updateUI(babies: List<Baby>) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.loading.visibility = View.GONE
            if (babies.isEmpty()) {
                Toast.makeText(this@ScheduleInfoPage, "No baby data found", Toast.LENGTH_SHORT)
                    .show()
            } else {
                babyAdapter.submitList(babies)
            }
        }
    }

}
