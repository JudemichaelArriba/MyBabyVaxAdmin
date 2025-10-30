package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.mybabyvaxadmin.services.DatabaseService
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.ActivityAddVaccinePageBinding
import com.example.mybabyvaxadmin.interfaces.InterfaceClass
import com.example.mybabyvaxadmin.models.Vaccine


class AddVaccinePage : AppCompatActivity() {
    private val databaseServices = DatabaseService()
    private lateinit var binding: ActivityAddVaccinePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddVaccinePageBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AddVaccinePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        spinnersValues()
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveButton.setOnClickListener {
            setupSaveButton()
        }
    }



    private fun spinnersValues() {
        val routeOptions = listOf("Injection", "Oral", "Intranasal", "Topical")
        val typeOptions = listOf(
            "Live Attenuated",
            "Inactivated",
            "Toxoid",
            "Subunit",
            "mRNA",
            "Vector-based",
            "Other"
        )
        val ageUnitOptions = listOf("Days", "Weeks", "Months", "Years")


        binding.apply {

            routeAutoCompleteTextView.setAdapter(
                ArrayAdapter(this@AddVaccinePage, android.R.layout.simple_list_item_1, routeOptions)
            )
            typeAutoCompleteTextView.setAdapter(
                ArrayAdapter(this@AddVaccinePage, android.R.layout.simple_list_item_1, typeOptions)
            )
            ageUnitSpinner.adapter = ArrayAdapter(
                this@AddVaccinePage,
                android.R.layout.simple_spinner_dropdown_item,
                ageUnitOptions
            )

        }

    }


    /**
     * Save button for the vaccine however if the admin checked the dosage box it will redirect to the
     * add dosage but the vaccine is already added
     */
    private fun setupSaveButton() {

        val name = binding.vaccineNameEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val route = binding.routeAutoCompleteTextView.text.toString().trim()
        val type = binding.typeAutoCompleteTextView.text.toString().trim()
        val sideEffects = binding.sideEffectsEditText.text.toString().trim()
        val eligibleAge = binding.eligibleAgeEditText.text.toString().trim()
        val ageUnit = binding.ageUnitSpinner.selectedItem.toString()

        val hasDosage = binding.hasDosageCheckBox.isChecked

        if (name.isEmpty() || description.isEmpty() || route.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val vaccine = Vaccine(
            id = null,
            name = name,
            description = description,
            route = route,
            type = type,
            sideEffects = sideEffects,
            eligibleAge = eligibleAge.toDouble(),
            ageUnit = ageUnit,
            hasDosage = hasDosage
        )



        if (hasDosage) {
            val intent = Intent(this@AddVaccinePage, AddDosagePage::class.java)
            intent.putExtra("vaccineName", vaccine.name)
            intent.putExtra("vaccineDescription", vaccine.description)
            intent.putExtra("vaccineRoute", vaccine.route)
            intent.putExtra("vaccineType", vaccine.type)
            intent.putExtra("vaccineSideEffects", vaccine.sideEffects)
            intent.putExtra("vaccineEligibleAge", vaccine.eligibleAge)
            intent.putExtra("vaccineAgeUnit", vaccine.ageUnit)

            startActivity(intent)
        } else {
            /** database call form the databaseservice class to add a vaccine
             *
             */
            databaseServices.addVaccine(vaccine, object : InterfaceClass.StatusCallbackWithId {
                override fun onSuccess(message: String, id: String) {
                    Toast.makeText(this@AddVaccinePage, message, Toast.LENGTH_SHORT)
                        .show()

                    finish()
                }

                override fun onFailure(error: String) {
                    Toast.makeText(this@AddVaccinePage, error, Toast.LENGTH_SHORT).show()
                }


            })


        }


    }
}