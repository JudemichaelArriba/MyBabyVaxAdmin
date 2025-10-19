package com.example.mybabyvaxadmin.models

data class Schedule(
    var vaccineName: String? = null,
    var vaccineType: String? = null,
    var description: String? = null,
    var route: String? = null,
    var sideEffects: String? = null,
    var doses: List<DoseSchedule>? = null
)