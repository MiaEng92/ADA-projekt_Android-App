package com.example.mysurveyapp.network

import java.time.LocalDateTime
import java.time.ZoneId

// Det, appen sender, når man opretter en måling.
// Felterne skal hedde præcis det samme som i Swagger.
data class CreateAssessmentRequest(
    val assessmentName: String,
    val startDate: String,
    val endDate: String,
    val applicationName: String,
    val roleNames: List<String>,
    val regionIds: List<String>,
    val experiences: List<Int>
)

// Laver dato og tid om til det format, API'et vil have, fx 2026-10-20T12:20:00Z
// ("Z" betyder UTC-tid. Uden den kan databasen afvise tidspunktet.)
fun LocalDateTime.tilApiTid(): String =
    this.atZone(ZoneId.systemDefault()).toInstant().toString()