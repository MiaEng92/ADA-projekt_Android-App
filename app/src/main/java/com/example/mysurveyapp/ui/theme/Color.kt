package com.example.mysurveyapp.ui.theme

import androidx.compose.ui.graphics.Color

// Hovedfarver
val AdaBlue = Color(0xFF1F4E79)            // knapper, overskrifter, ikoner
val AdaBlueLight = Color(0xFFD6E4F5)       // lyse flader i samme farve
val AdaBlueDark = Color(0xFF0B2542)        // tekst på lyse blå flader
val AdaSlate = Color(0xFF4F6D8F)           // sekundær farve
val AdaTeal = Color(0xFF0F8B8D)            // lille accentfarve

// Baggrunde og tekst
val AdaBackground = Color(0xFFF7F9FC)
val AdaText = Color(0xFF1E293B)
val AdaCard = Color(0xFFE8EDF4)            // kort
val AdaOutline = Color(0xFF7A8AA0)         // kanter på tekstfelter
val AdaError = Color(0xFFBA1A1A)

// Til målingskort (status)
val StatusDraft = Color(0xFF6B7280)        // grå
val StatusRunning = Color(0xFF2E7D32)      // grøn
val StatusDone = Color(0xFF1F4E79)         // blå

// Til ADA Score (fra kravspecifikationen: under 50 / 50-70 / over 70)
val ScoreRed = Color(0xFFD32F2F)
val ScoreYellow = Color(0xFFF9A825)
val ScoreGreen = Color(0xFF2E7D32)