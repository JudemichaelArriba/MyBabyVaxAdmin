package com.example.mybabyvaxadmin.pages

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mybabyvaxadmin.services.DatabaseService
import com.example.mybabyvaxadmin.databinding.ActivityEditVaccineInfoPageBinding
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.interfaces.InterfaceClass
import com.example.mybabyvaxadmin.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditVaccineInfoPage : AppCompatActivity() {

    private lateinit var binding: ActivityEditVaccineInfoPageBinding

    private var vaccineId = ""
    private var initialName = ""
    private var initialRoute = ""
    private var initialType = ""
    private var initialDescription = ""
    private var initialSideEffects = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditVaccineInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.mainColor)

        spinnersValues()

        vaccineId = intent.getStringExtra("vaccineId") ?: ""
        initialName = intent.getStringExtra("vaccineName") ?: ""
        initialRoute = intent.getStringExtra("vaccineRoute") ?: ""
        initialType = intent.getStringExtra("vaccineType") ?: ""
        initialDescription = intent.getStringExtra("vaccineDescription") ?: ""
        initialSideEffects = intent.getStringExtra("vaccineSideEffects") ?: ""

        binding.vaccineNameEditText.setText(initialName)
        binding.descriptionEditText.setText(initialDescription)
        binding.routeAutoCompleteTextView.setText(initialRoute, false)
        binding.typeAutoCompleteTextView.setText(initialType, false)
        binding.sideEffectsEditText.setText(initialSideEffects)

        binding.saveButton.isEnabled = false
        binding.saveButton.alpha = 0.5f

        binding.backButton.setOnClickListener { finish() }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkIfChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.vaccineNameEditText.addTextChangedListener(watcher)
        binding.descriptionEditText.addTextChangedListener(watcher)
        binding.routeAutoCompleteTextView.addTextChangedListener(watcher)
        binding.typeAutoCompleteTextView.addTextChangedListener(watcher)
        binding.sideEffectsEditText.addTextChangedListener(watcher)

        binding.saveButton.setOnClickListener {
            val newName = binding.vaccineNameEditText.text.toString().trim()
            val newRoute = binding.routeAutoCompleteTextView.text.toString().trim()
            val newType = binding.typeAutoCompleteTextView.text.toString().trim()
            val newDescription = binding.descriptionEditText.text.toString().trim()
            val newSideEffects = binding.sideEffectsEditText.text.toString().trim()

            binding.progressOverlay.visibility = View.VISIBLE

            DialogHelper.showWarning(
                context = this,
                title = "Confirm Changes",
                message = "Are you sure you want to save these changes?",
                onConfirm = {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val dbService = DatabaseService()
                        dbService.updateVaccineEverywhere(
                            vaccineId = vaccineId,
                            oldName = initialName,
                            newName = newName,
                            newRoute = newRoute,
                            newType = newType,
                            newDescription = newDescription,
                            newSideEffects = newSideEffects,
                            callback = object : InterfaceClass.StatusCallback {
                                override fun onSuccess(message: String) {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        binding.progressOverlay.visibility = View.GONE
                                        DialogHelper.showSuccess(
                                            this@EditVaccineInfoPage,
                                            "Success",
                                            message
                                        ) {
                                            finish()
                                        }
                                    }
                                }

                                override fun onError(error: String) {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        binding.progressOverlay.visibility = View.GONE
                                        DialogHelper.showError(
                                            this@EditVaccineInfoPage,
                                            "Error",
                                            error
                                        )
                                    }
                                }
                            }
                        )
                    }
                },
                onCancel = {
                    binding.progressOverlay.visibility = View.GONE
                }
            )
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

        val routeAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, routeOptions)
        val typeAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, typeOptions)

        binding.routeAutoCompleteTextView.setAdapter(routeAdapter)
        binding.typeAutoCompleteTextView.setAdapter(typeAdapter)
    }

    private fun checkIfChanged() {
        val currentName = binding.vaccineNameEditText.text.toString().trim()
        val currentRoute = binding.routeAutoCompleteTextView.text.toString().trim()
        val currentType = binding.typeAutoCompleteTextView.text.toString().trim()
        val currentDescription = binding.descriptionEditText.text.toString().trim()
        val currentSideEffects = binding.sideEffectsEditText.text.toString().trim()

        val hasChanged = currentName != initialName ||
                currentRoute != initialRoute ||
                currentType != initialType ||
                currentDescription != initialDescription ||
                currentSideEffects != initialSideEffects

        binding.saveButton.isEnabled = hasChanged
        binding.saveButton.alpha = if (hasChanged) 1f else 0.5f
    }
}
