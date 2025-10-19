package com.example.mybabyvaxadmin.models

data class DoseSchedule(
    var doseName: String? = null,
    var date: String? = null,
    var completed: Boolean = false,
    var interval: String? = null,
    var visible: Boolean = false
)