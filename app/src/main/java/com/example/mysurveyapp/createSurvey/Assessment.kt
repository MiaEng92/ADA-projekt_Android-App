package com.example.mysurveyapp.createSurvey

import java.time.LocalDateTime


data class Assessment(
    val name: String,
    val application: String,
    val role: String,
    val region: String,
    val experience: String,
    val survey: String,
    val start: LocalDateTime? = null,
    val end: LocalDateTime? = null,
    val status: String = "Oprettet"
)