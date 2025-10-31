package com.example.mybabyvaxadmin.models





data class Baby(
    var id: String? = null,
    var parentId: String? = null,
    var fullName: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var birthPlace: String? = null,
    var bloodType: String? = null,
    var heightAtBirth: Int? = null,
    var weightAtBirth: Int? = null,
    var profileImageUrl: String? = null,
    var schedules: Map<String, Schedule>? = null
)