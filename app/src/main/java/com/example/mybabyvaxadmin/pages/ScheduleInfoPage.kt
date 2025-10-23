package com.example.mybabyvaxadmin.pages

import android.graphics.Bitmap
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
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ScheduleInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleInfoPageBinding
    private lateinit var babyAdapter: BabyAdapter
    private val databaseService = DatabaseService()

    private var vaccineName: String = ""
    private var doseName: String = ""
    private var date: String = ""
    private var babyIds: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)

        babyAdapter = BabyAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = babyAdapter

        binding.backButton.setOnClickListener { finish() }


        vaccineName = intent.getStringExtra("vaccineName") ?: "Unknown Vaccine"
        doseName = intent.getStringExtra("doseName") ?: "Unknown Dose"
        date = intent.getStringExtra("date") ?: "No Date"
        babyIds = intent.getStringArrayListExtra("babyIds") ?: arrayListOf()

        binding.vaccineName.text = vaccineName
        binding.vaccineDetails.text = "$doseName • $date"


        loadBabies(babyIds)


        binding.btnGenerateQR.setOnClickListener {
            generateQRCode()
        }
    }


    private fun loadBabies(babyIds: List<String>) {
        if (babyIds.isEmpty()) {
            Toast.makeText(this, "No babies found for this schedule", Toast.LENGTH_SHORT).show()
            return
        }

        binding.loading.visibility = View.VISIBLE
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


    private fun generateQRCode() {
        try {
            val qrData = JSONObject().apply {
                put("vaccineName", vaccineName)
                put("doseName", doseName)
                put("date", date)


                val babyArray = org.json.JSONArray()
                babyIds.forEach { babyArray.put(it) }
                put("babyIds", babyArray)

                put("token", System.currentTimeMillis().toString())
            }

            val encoder = BarcodeEncoder()
            val bitmap: Bitmap =
                encoder.encodeBitmap(qrData.toString(), BarcodeFormat.QR_CODE, 600, 600)

            binding.qrCode.setImageBitmap(bitmap)
            Toast.makeText(this, "QR Code generated!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate QR code: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

}
