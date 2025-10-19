package com.example.mybabyvaxadmin.models

data class MergedSchedule(
    val date: String = "",
    val vaccineName: String = "",
    val doseName: String = "",
    val babyIds: List<String> = emptyList()
)
