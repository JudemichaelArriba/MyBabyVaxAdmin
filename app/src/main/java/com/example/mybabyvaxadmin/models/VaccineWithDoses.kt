package com.example.mybabyvaxadmin.models

data class VaccineWithDoses(
    val vaccine: Vaccine,
    val doses: List<Dose> = emptyList()
)
