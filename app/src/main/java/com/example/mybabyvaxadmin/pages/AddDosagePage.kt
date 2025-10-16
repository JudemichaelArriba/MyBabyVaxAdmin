package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.components.bottomNav
import com.example.mybabyvaxadmin.databinding.ActivityAddDosagePageBinding
import com.example.mybabyvaxadmin.models.Dose
import com.example.mybabyvaxadmin.models.Vaccine

class AddDosagePage : AppCompatActivity() {
    private val doseList = mutableListOf<Dose>()
    private lateinit var vaccine: Vaccine
    private val databaseService = DatabaseService()
    private lateinit var binding: ActivityAddDosagePageBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddDosagePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AddDosagePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadVaccineFromIntent()
        populateSpinner()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveDoseButton.setOnClickListener {
            addDoseToList()
        }
        binding.finishButton.setOnClickListener {
            finalizedSave()
        }
    }

    /**
     * populate the spinner with string list
     */
    private fun populateSpinner() {
        val intervalOptions = listOf("Days", "Weeks", "Months", "Year")
        val adapter = ArrayAdapter(
            this@AddDosagePage,
            android.R.layout.simple_spinner_dropdown_item,
            intervalOptions
        )
        binding.intervalUnitSpinner.adapter = adapter

    }


    /** Load info from vaccine page
     *
     */

    private fun loadVaccineFromIntent() {
        vaccine = Vaccine(
            id = null,
            name = intent.getStringExtra("vaccineName") ?: "",
            description = intent.getStringExtra("vaccineDescription") ?: "",
            route = intent.getStringExtra("vaccineRoute") ?: "",
            type = intent.getStringExtra("vaccineType") ?: "",
            sideEffects = intent.getStringExtra("vaccineSideEffects") ?: "",
            eligibleAge = intent.getDoubleExtra("vaccineEligibleAge", 0.0),
            ageUnit = intent.getStringExtra("vaccineAgeUnit") ?: "",

            hasDosage = true
        )
    }


    /**
     * For overall save on database both vaccine and doses
     */

    private fun finalizedSave() {
        if (doseList.isEmpty()) {
            Toast.makeText(this, "Please add at least one dose.", Toast.LENGTH_SHORT).show()
            return

        }

        /**
         * calling function for saving vaccine
         */

        databaseService.addVaccine(vaccine, object : InterfaceClass.StatusCallbackWithId {
            override fun onSuccess(message: String, vaccineId: String) {
                binding.loadingOverlay.visibility = View.VISIBLE
                for (dose in doseList) {

                    /**
                     * calling function for saving doses
                     */
                    databaseService.addVaccineDosage(
                        vaccineId,
                        dose,
                        object : InterfaceClass.StatusCallback {
                            override fun onSuccess(message: String) {
                                binding.loadingOverlay.visibility = View.GONE

                                DialogHelper.showSuccess(
                                    this@AddDosagePage,
                                    "Successful",
                                    "Vaccine is successfully added!"
                                ) {
                                    finish()
                                }


                            }

                            override fun onError(error: String) {
                                binding.loadingOverlay.visibility = View.GONE
                                Log.d("Failed adding dosage", error)
                                DialogHelper.showWarning(
                                    this@AddDosagePage,
                                    "Error",
                                    error
                                )
                            }
                        })
                }

            }

            override fun onFailure(error: String) {
                Log.d("Failed adding vaccine", error)
                Toast.makeText(this@AddDosagePage, error, Toast.LENGTH_SHORT).show()
            }
        })


    }


    /**
     * Add the doses on the list function
     */
    private fun addDoseToList() {

        binding.apply {
            val name = binding.doseNameEditText.text.toString().trim()
            val interval = binding.intervalNumberEditText.text.toString().trim()
            val unit = binding.intervalUnitSpinner.selectedItem.toString().trim()
            val desc = binding.descriptionEditText.text.toString().trim()

            if (name.isEmpty() || interval.isEmpty() || desc.isEmpty()) {
                Toast.makeText(
                    this@AddDosagePage,
                    "Please fill out all required fields.",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }


            val dose = Dose(null, name, interval.toDouble(), unit, desc)
            doseList.add(dose)

            Toast.makeText(
                this@AddDosagePage,
                "Dose added! (${doseList.size} total)",
                Toast.LENGTH_SHORT
            ).show()
            binding.doseNameEditText.text?.clear()
            binding.intervalNumberEditText.text?.clear()
            binding.descriptionEditText.text?.clear()

        }


    }


}