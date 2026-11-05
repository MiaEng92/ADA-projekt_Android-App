package com.example.mysurveyapp.network

// Det, backend'en svarer på GET api/regions: en liste af disse.
// Felterne skal hedde præcis det samme som i backend'ens Region (RegionId og RegionName).
data class RegionDto(
    val regionId: String,
    val regionName: String
)

// Det, backend'en svarer på GET api/experiences: en liste af disse.
// experienceValue er en engelsk kode, fx "LessThan1Year". Brug experienceLabel() til at vise den.
data class ExperienceDto(
    val experienceId: String,
    val experienceValue: String
)

// Rækkefølgen, erfaringerne skal vises i (backend'en sender dem i databasens rækkefølge)
private val experienceOrder = listOf("LessThan1Year", "From1To2Years", "From3To5Years", "MoreThan5Years")

fun List<ExperienceDto>.inNaturalOrder(): List<ExperienceDto> =
    sortedBy { dto ->
        val place = experienceOrder.indexOf(dto.experienceValue)
        if (place == -1) Int.MAX_VALUE else place   // ukendte værdier kommer til sidst
    }

// Teksten, brugeren ser, ud fra backend'ens kode
fun experienceLabel(value: String): String = when (value) {
    "LessThan1Year" -> "under 1 år"
    "From1To2Years" -> "1-2 år"
    "From3To5Years" -> "3-5 år"
    "MoreThan5Years" -> "over 5 år"
    else -> value   // en ny værdi, appen ikke kender, vises, som den er
}