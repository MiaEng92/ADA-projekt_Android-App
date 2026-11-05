package com.example.mysurveyapp.network

import java.time.LocalDateTime
import java.time.ZoneId

// Det, appen sender, når man opretter en måling.
// Felterne skal hedde præcis det samme som i backend'ens CreateAssessmentRequest.
data class CreateAssessmentRequest(
    val assessmentName: String,
    val startDate: String,
    val endDate: String,
    val applicationName: String,
    val surveyId: String,                 // NYT: id på den survey, målingen bruger
    val roleNames: List<String>,
    val regionIds: List<String>,
    val experienceIds: List<String>,      // ÆNDRET: id'er (før var det tal i "experiences")
    val respondentEmails: List<String>    // NYT: dem, der skal have surveyen
)

// Laver dato og tid om til det format, API'et vil have, fx 2026-10-20T12:20:00Z
// ("Z" betyder UTC-tid. Uden den kan databasen afvise tidspunktet.)
fun LocalDateTime.tilApiTid(): String =
    this.atZone(ZoneId.systemDefault()).toInstant().toString()