package com.example.iptfinal.services

import android.util.Log
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.mybabyvaxadmin.models.Dose
import com.example.mybabyvaxadmin.models.MergedSchedule

import com.example.mybabyvaxadmin.models.Users
import com.example.mybabyvaxadmin.models.Vaccine
import com.google.firebase.database.*

class DatabaseService {

    private val databasePuroks: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Locations/Monkayo/Union")
    private val databaseUsers: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")

    private val databaseVaccines: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("vaccines")

    fun fetchPuroks(callback: InterfaceClass.PurokCallback) {
        databasePuroks.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val purokList = mutableListOf<String>()
                for (purokSnap in snapshot.children) {
                    val purokName = purokSnap.getValue(String::class.java) ?: ""
                    purokList.add(purokName)
                    Log.d("DatabaseService", "Purok found: $purokName")
                }
                callback.onPuroksLoaded(purokList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching puroks: ${error.message}")
                callback.onError(error.message)
            }
        })
    }

    fun fetchUserById(uid: String, callback: InterfaceClass.UserCallback) {
        databaseUsers.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null) {
                        callback.onUserLoaded(user)
                    } else {
                        callback.onError("User data is empty")
                    }
                } else {
                    callback.onError("User not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseService", "Error fetching user: ${error.message}")
                callback.onError(error.message)
            }
        })
    }

    fun updateUser(uid: String, updatedUser: Users, callback: InterfaceClass.StatusCallback) {
        databaseUsers.child(uid).setValue(updatedUser)
            .addOnSuccessListener {
                callback.onSuccess("Profile updated successfully")
            }
            .addOnFailureListener { e ->
                callback.onError("Failed to update profile: ${e.message}")
            }
    }


    fun addVaccine(vaccine: Vaccine, callback: InterfaceClass.StatusCallbackWithId) {
        val vaccineId = databaseVaccines.push().key

        if (vaccineId == null) {
            callback.onFailure("Failed to generate vaccine ID.")
            return
        }

        vaccine.id = vaccineId
        databaseVaccines.child(vaccineId).setValue(vaccine)
            .addOnSuccessListener {
                callback.onSuccess("Vaccine saved successfully!", vaccineId)
            }
            .addOnFailureListener { e ->
                callback.onFailure("Failed to save vaccine: ${e.message}")
            }
    }


    fun addVaccineDosage(vaccineId: String, dose: Dose, callback: InterfaceClass.StatusCallback) {
        val doseId = databaseVaccines.child(vaccineId).child("doses").push().key

        if (doseId.isNullOrEmpty()) {
            callback.onError("Failed to generate dose ID.")
            return
        }

        val doseWithId = dose.copy(id = doseId)

        databaseVaccines.child(vaccineId).child("doses").child(doseId).setValue(doseWithId)
            .addOnSuccessListener {
                callback.onSuccess("Successfully added the dose.")
            }
            .addOnFailureListener { exception ->
                callback.onError("Failed to add dose: ${exception.message}")
            }
    }


    fun fetchAllBabySchedules(callback: InterfaceClass.MergedScheduleCallback) {
        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mergedMap = mutableMapOf<String, MergedSchedule>()

                for (userSnap in snapshot.children) {
                    val babiesSnap = userSnap.child("babies")
                    for (babySnap in babiesSnap.children) {
                        val babyName =
                            babySnap.child("fullName").getValue(String::class.java) ?: "Unknown"
                        val schedulesSnap = babySnap.child("schedules")

                        for (vaccineSnap in schedulesSnap.children) {
                            val vaccineName =
                                vaccineSnap.child("vaccineName").getValue(String::class.java) ?: ""
                            val dosesSnap = vaccineSnap.child("doses")

                            var activeDoseFound = false

                            for (doseSnap in dosesSnap.children) {
                                val completed =
                                    doseSnap.child("completed").getValue(Boolean::class.java)
                                        ?: false
                                val date = doseSnap.child("date").getValue(String::class.java) ?: ""
                                val doseName =
                                    doseSnap.child("doseName").getValue(String::class.java) ?: ""


                                if (!completed && !activeDoseFound) {
                                    activeDoseFound = true

                                    if (date.isNotEmpty() && vaccineName.isNotEmpty() && doseName.isNotEmpty()) {
                                        val key = "$date|$vaccineName|$doseName"
                                        val existing = mergedMap[key]

                                        if (existing == null) {
                                            mergedMap[key] = MergedSchedule(
                                                date = date,
                                                vaccineName = vaccineName,
                                                doseName = doseName,
                                                babyNames = mutableListOf(babyName)
                                            )
                                        } else {
                                            val updatedNames = existing.babyNames.toMutableList()
                                            updatedNames.add(babyName)
                                            mergedMap[key] = existing.copy(babyNames = updatedNames)
                                        }
                                    }
                                }


                                if (activeDoseFound) break
                            }
                        }
                    }
                }

                val mergedList = mergedMap.values.sortedBy { it.date }
                callback.onMergedSchedulesLoaded(mergedList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }


}
