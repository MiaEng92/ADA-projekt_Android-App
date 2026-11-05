package com.example.mysurveyapp.network

// En score med usikkerhed. ci95Low/ci95High er tomme, hvis der er under 2 svar.
data class ScoreDto(
    val score: Double,
    val ci95Low: Double?,
    val ci95High: Double?,
    val band: String,      // "Not Acceptable", "Marginal" eller "Acceptable"
    val n: Int             // antal besvarelser bag tallet
)

// Det, appen får tilbage om én måling.
// Felterne skal hedde præcis det samme som i JSON fra backend'en.
data class AssessmentSummaryDto(
    val assessmentId: String,
    val assessmentName: String,
    val applicationName: String,
    val status: String,
    val invitedCount: Int,      // hvor mange der har fået surveyen
    val respondedCount: Int,    // hvor mange der har svaret
    val responseRate: Double,   // 0.0 - 1.0, fx 0.3167 = 32 %
    val adaScore: ScoreDto?     // tom, så længe for få har svaret
)

// Ét tema med score og usikkerhed
data class ThemeScoreDto(
    val theme: String,
    val score: Double,
    val ci95Low: Double?,
    val ci95High: Double?,
    val band: String,
    val n: Int
)

// Det, detaljeskærmen får: det samme som summary, plus hvornår svarene kom ind og temaerne
data class AssessmentDetailsDto(
    val assessmentId: String,
    val assessmentName: String,
    val applicationName: String,
    val status: String,
    val invitedCount: Int,
    val respondedCount: Int,
    val responseRate: Double,
    val adaScore: ScoreDto?,
    val createdAt: String,            // fx 2026-10-27T13:05:00Z
    val completedAt: List<String>,    // ét tidspunkt pr. besvarelse, ældste først
    val themes: List<ThemeScoreDto>   // tom, så længe for få har svaret
)