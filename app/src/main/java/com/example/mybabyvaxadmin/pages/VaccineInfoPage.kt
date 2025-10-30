package com.example.mybabyvaxadmin.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mybabyvaxadmin.databinding.ActivityVaccineInfoPageBinding

class VaccineInfoPage : AppCompatActivity() {
    private lateinit var binding: ActivityVaccineInfoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaccineInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)
        val vaccineName = intent.getStringExtra("vaccineName")
        val vaccineRoute = intent.getStringExtra("vaccineRoute")
        val vaccineType = intent.getStringExtra("vaccineType")
        val vaccineDescription = intent.getStringExtra("vaccineDescription")
        val vaccineSideEffects = intent.getStringExtra("vaccineSideEffects")
        val vaccineEligibleAge = intent.getStringExtra("vaccineEligibleAge")

        binding.vaccineName.text = vaccineName
        binding.routeTv.text = vaccineRoute ?: "-"
        binding.typeTv.text = vaccineType ?: "-"
        binding.descriptionTv.text = vaccineDescription ?: "-"
        binding.sideEffectsTv.text = vaccineSideEffects ?: "-"
        binding.eligibleAgeTv.text = vaccineEligibleAge ?: "-"
    }
}
