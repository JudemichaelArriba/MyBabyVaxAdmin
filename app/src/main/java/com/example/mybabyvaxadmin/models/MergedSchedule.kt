package com.example.mybabyvaxadmin.models

data class MergedSchedule(
    val date: String = "",
    val vaccineName: String = "",
    val doseName: String = "",
    val babyNames: List<String> = emptyList()
)