package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.ActivityAddVaccinePageBinding
import com.example.mybabyvaxadmin.models.Vaccine


class AddVaccinePage : AppCompatActivity() {
    private val databaseServices = DatabaseService()
    private lateinit var binding: ActivityAddVaccinePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddVaccinePageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        spinnersValues()

        binding.saveButton.setOnClickListener {
            setupSaveButton()
        }
    }


    // Populates all the spinners for admin to choose
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
        val scheduleOptions =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

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
            scheduleSpinner.adapter = ArrayAdapter(
                this@AddVaccinePage,
                android.R.layout.simple_spinner_dropdown_item,
                scheduleOptions
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
        val schedule = binding.scheduleSpinner.selectedItem.toString()
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
            eligibleAge = eligibleAge.toIntOrNull(),
            ageUnit = ageUnit,
            schedule = schedule,
            hasDosage = hasDosage
        )

        /** database call form the databaseservice class to add a vaccine
         *
         */
        databaseServices.addVaccine(vaccine, object : InterfaceClass.StatusCallbackWithId {
            override fun onSuccess(message: String, id: String) {
                Toast.makeText(this@AddVaccinePage, message, Toast.LENGTH_SHORT)
                    .show()

                if (hasDosage) {
                    val intent = Intent(this@AddVaccinePage, AddDosagePage::class.java)
                    intent.putExtra("vaccineId", vaccine.id)
                    intent.putExtra("vaccineName", name)
                    startActivity(intent)
                } else {
                    finish()
                }
            }

            override fun onFailure(error: String) {
                Toast.makeText(this@AddVaccinePage, error, Toast.LENGTH_SHORT).show()
            }


        })


    }
}