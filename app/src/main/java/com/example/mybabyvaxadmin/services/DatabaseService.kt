package com.example.iptfinal.services

import android.util.Log
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.mybabyvaxadmin.models.Baby
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
                        val babyId = babySnap.child("id").getValue(String::class.java) ?: continue
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
                                val visible =
                                    doseSnap.child("visible").getValue(Boolean::class.java) ?: true


                                if (!completed && !activeDoseFound && visible) {
                                    activeDoseFound = true

                                    if (date.isNotEmpty() && vaccineName.isNotEmpty() && doseName.isNotEmpty()) {
                                        val key = "$date|$vaccineName|$doseName"
                                        val existing = mergedMap[key]

                                        if (existing == null) {
                                            mergedMap[key] = MergedSchedule(
                                                date = date,
                                                vaccineName = vaccineName,
                                                doseName = doseName,
                                                babyIds = mutableListOf(babyId)
                                            )
                                        } else {
                                            val updatedIds = existing.babyIds.toMutableList()
                                            if (!updatedIds.contains(babyId)) {
                                                updatedIds.add(babyId)
                                            }
                                            mergedMap[key] = existing.copy(babyIds = updatedIds)
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

    fun fetchBabyById(babyId: String, callback: InterfaceClass.BabyCallback) {
        databaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundBaby: Baby? = null

                for (userSnap in snapshot.children) {
                    val babiesSnap = userSnap.child("babies")
                    for (babySnap in babiesSnap.children) {
                        val id = babySnap.child("id").getValue(String::class.java)
                        if (id == babyId) {
                            foundBaby = babySnap.getValue(Baby::class.java)
                            break
                        }
                    }
                    if (foundBaby != null) break
                }

                if (foundBaby != null) {
                    callback.onBabyLoaded(foundBaby)
                } else {
                    callback.onError("Baby not found with ID: $babyId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }


    fun fetchAllVaccines(callback: InterfaceClass.VaccineListCallback) {
        databaseVaccines.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vaccineList = mutableListOf<Vaccine>()

                for (vaccineSnap in snapshot.children) {
                    val vaccine = vaccineSnap.getValue(Vaccine::class.java)
                    if (vaccine != null) {
                        vaccineList.add(vaccine)
                    }
                }

                callback.onVaccinesLoaded(vaccineList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }


    fun fetchDosesByVaccineId(vaccineId: String, callback: InterfaceClass.DoseListCallback) {
        val dosesRef = databaseVaccines.child(vaccineId).child("doses")

        dosesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doseList = mutableListOf<Dose>()

                for (doseSnap in snapshot.children) {
                    val dose = doseSnap.getValue(Dose::class.java)
                    if (dose != null) {
                        doseList.add(dose)
                    }
                }

                callback.onDosesLoaded(doseList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }


    fun deleteVaccine(vaccineId: String, callback: InterfaceClass.StatusCallback) {
        databaseVaccines.child(vaccineId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(vaccineSnapshot: DataSnapshot) {
                    if (!vaccineSnapshot.exists()) {
                        callback.onError("Vaccine not found.")
                        return
                    }

                    val vaccineName = vaccineSnapshot.child("name").getValue(String::class.java)
                    if (vaccineName.isNullOrEmpty()) {
                        callback.onError("Vaccine name not found.")
                        return
                    }


                    databaseVaccines.child(vaccineId).removeValue()
                        .addOnSuccessListener {

                            databaseUsers.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(usersSnapshot: DataSnapshot) {
                                    for (userSnap in usersSnapshot.children) {
                                        val babiesSnap = userSnap.child("babies")
                                        for (babySnap in babiesSnap.children) {
                                            val schedulesSnap = babySnap.child("schedules")
                                            for (scheduleSnap in schedulesSnap.children) {
                                                val scheduleName =
                                                    scheduleSnap.child("vaccineName")
                                                        .getValue(String::class.java)
                                                if (scheduleName == vaccineName) {
                                                    databaseUsers.child(userSnap.key!!)
                                                        .child("babies")
                                                        .child(babySnap.key!!)
                                                        .child("schedules")
                                                        .child(scheduleSnap.key!!)
                                                        .removeValue()
                                                }
                                            }
                                        }
                                    }
                                    callback.onSuccess("Vaccine and related schedules deleted successfully.")
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    callback.onError("Failed to remove schedules: ${error.message}")
                                }
                            })
                        }
                        .addOnFailureListener { e ->
                            callback.onError("Failed to delete vaccine: ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onError("Failed to fetch vaccine: ${error.message}")
                }
            })
    }


    fun updateVaccineEverywhere(
        vaccineId: String,
        oldName: String,
        newName: String,
        newRoute: String,
        newType: String,
        newDescription: String,
        newSideEffects: String,
        callback: InterfaceClass.StatusCallback
    ) {
        val vaccineData = mapOf(
            "name" to newName,
            "route" to newRoute,
            "type" to newType,
            "description" to newDescription,
            "sideEffects" to newSideEffects,

            )

        val database = FirebaseDatabase.getInstance().reference


        database.child("vaccines").child(vaccineId).updateChildren(vaccineData)
            .addOnSuccessListener {
                Log.d("VaccineUpdate", "Vaccine updated in /vaccines")


                database.child("users").get().addOnSuccessListener { usersSnapshot ->
                    for (userSnap in usersSnapshot.children) {
                        val babiesSnap = userSnap.child("babies")
                        for (babySnap in babiesSnap.children) {
                            val schedulesSnap = babySnap.child("schedules")
                            for (scheduleSnap in schedulesSnap.children) {
                                val vaccineName =
                                    scheduleSnap.child("vaccineName").getValue(String::class.java)
                                if (vaccineName == oldName || scheduleSnap.key == oldName) {
                                    val scheduleRef = scheduleSnap.ref
                                    val updatedSchedule = mapOf(
                                        "vaccineName" to newName,
                                        "description" to newDescription,
                                        "route" to newRoute,
                                        "sideEffects" to newSideEffects,
                                        "vaccineType" to newType
                                    )


                                    val parentRef = scheduleRef.parent
                                    val oldData = scheduleSnap.value
                                    parentRef?.child(newName)?.setValue(oldData)
                                        ?.addOnSuccessListener {
                                            parentRef.child(oldName).removeValue()
                                            parentRef.child(newName).updateChildren(updatedSchedule)
                                            Log.d(
                                                "VaccineUpdate",
                                                "Renamed schedule $oldName → $newName for ${babySnap.key}"
                                            )
                                        }
                                }
                            }
                        }
                    }
                    callback.onSuccess("Vaccine and related baby schedules updated successfully.")
                }.addOnFailureListener {
                    callback.onError("Failed to update baby schedules: ${it.message}")
                }
            }
            .addOnFailureListener {
                callback.onError("Failed to update vaccine record: ${it.message}")
            }
    }


}
