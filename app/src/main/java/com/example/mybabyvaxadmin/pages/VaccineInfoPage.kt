package com.example.mybabyvaxadmin.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.adapters.DoseAdapter
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.databinding.ActivityVaccineInfoPageBinding
import com.example.mybabyvaxadmin.models.Dose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

class VaccineInfoPage : AppCompatActivity() {
    private lateinit var binding: ActivityVaccineInfoPageBinding
    private lateinit var doseAdapter: DoseAdapter
    private val databaseService = DatabaseService()
    private val doseList = mutableListOf<Dose>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaccineInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.mainColor)

        binding.doseRecycle.layoutManager = LinearLayoutManager(this)
        doseAdapter = DoseAdapter(doseList)
        binding.doseRecycle.adapter = doseAdapter

        val vaccineId = intent.getStringExtra("vaccineId") ?: ""
        val vaccineName = intent.getStringExtra("vaccineName") ?: "-"
        val vaccineRoute = intent.getStringExtra("vaccineRoute") ?: "-"
        val vaccineType = intent.getStringExtra("vaccineType") ?: "-"
        val vaccineDescription = intent.getStringExtra("vaccineDescription") ?: "-"
        val vaccineSideEffects = intent.getStringExtra("vaccineSideEffects") ?: "-"
        val vaccineEligibleAge = intent.getDoubleExtra("vaccineEligibleAge", -1.0)

        binding.vaccineName.text = vaccineName
        binding.routeTv.text = vaccineRoute
        binding.typeTv.text = vaccineType
        binding.descriptionTv.text = vaccineDescription
        binding.sideEffectsTv.text = vaccineSideEffects
        binding.eligibleAgeTv.text = if (vaccineEligibleAge >= 0) "$vaccineEligibleAge" else "-"

        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditVaccineInfoPage::class.java)
            intent.putExtra("vaccineId", vaccineId)
            intent.putExtra("vaccineName", vaccineName)
            intent.putExtra("vaccineRoute", vaccineRoute)
            intent.putExtra("vaccineType", vaccineType)
            intent.putExtra("vaccineDescription", vaccineDescription)
            intent.putExtra("vaccineSideEffects", vaccineSideEffects)
            intent.putExtra("vaccineEligibleAge", vaccineEligibleAge)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener { finish() }

        if (vaccineId.isNotEmpty()) {
            fetchDoses(vaccineId)
        } else {
            Log.e("VaccineInfoPage", "Vaccine ID is empty, cannot load doses.")
        }

        binding.deleteButton.setOnClickListener {
            binding.progressBarDose.visibility = View.VISIBLE
            DialogHelper.showWarning(
                this,
                title = "Delete Vaccine",
                message = "Are you sure you want to delete this vaccine? This will also remove all related baby schedules.",
                onConfirm = {
                    databaseService.deleteVaccine(
                        vaccineId,
                        object : InterfaceClass.StatusCallback {
                            override fun onSuccess(message: String) {
                                binding.progressBarDose.visibility = View.GONE
                                DialogHelper.showSuccess(
                                    this@VaccineInfoPage,
                                    "Deleted",
                                    message ?: "Vaccine deleted successfully."
                                ) { finish() }
                            }

                            override fun onError(errorMessage: String) {
                                binding.progressBarDose.visibility = View.GONE
                                DialogHelper.showError(
                                    this@VaccineInfoPage,
                                    "Error",
                                    errorMessage ?: "Failed to delete vaccine."
                                )
                            }
                        })
                },
                onCancel = {}
            )
        }
    }

    private fun fetchDoses(vaccineId: String) {
        binding.progressBarDose.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val doses = withContext(Dispatchers.IO) {
                    suspendCancellableCoroutine<List<Dose>> { cont ->
                        databaseService.fetchDosesByVaccineId(
                            vaccineId,
                            object : InterfaceClass.DoseListCallback {
                                override fun onDosesLoaded(doses: List<Dose>) {
                                    cont.resume(doses) {}
                                }

                                override fun onError(errorMessage: String?) {
                                    cont.resumeWithException(
                                        Exception(
                                            errorMessage ?: "Failed to fetch doses"
                                        )
                                    )
                                }
                            })
                    }
                }
                doseList.clear()
                doseList.addAll(doses)
                doseAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("VaccineInfoPage", "Failed to load doses: ${e.message}")
            } finally {
                binding.progressBarDose.visibility = View.GONE
            }
        }
    }
}
