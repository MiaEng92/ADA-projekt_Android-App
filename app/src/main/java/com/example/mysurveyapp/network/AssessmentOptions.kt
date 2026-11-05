package com.example.mysurveyapp.network

// Hver linje er et par: navnet, brugeren ser, og id'et, API'et vil have.
//
// Regioner og erfaring står ikke her mere: appen henter dem selv fra
// GET api/regions og GET api/experiences.

// Surveyens id er fast i SeedData, så det kan stå her.
// (Backend'en har endnu ikke et endpoint til surveys, så det er den eneste liste, der er tilbage.)
val surveyOptions = listOf(
    "ADA-survey" to "22222222-2222-2222-2222-222222222222"
)