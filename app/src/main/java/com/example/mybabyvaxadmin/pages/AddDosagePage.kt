package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.ActivityAddDosagePageBinding
import com.example.mybabyvaxadmin.models.Dose

class AddDosagePage : AppCompatActivity() {
    private val databaseService = DatabaseService()
    private lateinit var binding: ActivityAddDosagePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddDosagePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        populateSpinner()



        binding.saveDoseButton.setOnClickListener {
            saveFunction()
        }
    }

    /**
     * populate the spinner with string list
     */
    private fun populateSpinner() {
        val intervalOptions = listOf("Days", "Weeks", "Months")
        val adapter = ArrayAdapter(
            this@AddDosagePage,
            android.R.layout.simple_spinner_dropdown_item,
            intervalOptions
        )
        binding.intervalUnitSpinner.adapter = adapter

    }

    /**
     * save to the database the dose
     */
    private fun saveFunction() {
        val vaccineId = intent.getStringExtra("vaccineId") ?: return
        Log.d("vaccineId", vaccineId)
        val doseName = binding.doseNameEditText.text.toString().trim()
        val interval = binding.intervalNumberEditText.text.toString().trim()
        val intervalUnit = binding.intervalUnitSpinner.selectedItem.toString().trim()
        val doseDescription = binding.descriptionEditText.text.toString().trim()

        if (doseName.isEmpty() || interval.isEmpty() || intervalUnit.isEmpty() || doseDescription.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val dose = Dose(
            id = null,
            name = doseName,
            intervalNumber = interval.toInt(),
            intervalUnit = intervalUnit,
            description = doseDescription

        )
        databaseService.addVaccineDosage(vaccineId, dose, object : InterfaceClass.StatusCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(this@AddDosagePage, message, Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onError(error: String) {
                Toast.makeText(this@AddDosagePage, error, Toast.LENGTH_SHORT).show()
            }

        })

    }


}