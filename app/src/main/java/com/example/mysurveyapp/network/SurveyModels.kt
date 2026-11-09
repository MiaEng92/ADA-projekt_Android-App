package com.example.mysurveyapp.network

// Det, backend'en svarer på GET api/respondents/survey?token=...
data class SurveyDto(
    val surveyTitle: String,
    val surveyDescription: String?,
    val assessmentName: String,
    val themes: List<SurveyThemeDto>,

    // FORSLAG til backend'en: målingens roller, regioner og erfaringer, så respondenten kan vælge blandt dem.
    // Det rigtige GET sender dem ikke endnu, så de er null, indtil backend'en tilføjer dem.
    val roles: List<RoleDto>? = null,
    val regions: List<RegionDto>? = null,
    val experiences: List<ExperienceDto>? = null
)

data class RoleDto(
    val roleId: String,
    val roleName: String
)

data class SurveyThemeDto(
    val themeId: String,
    val title: String,
    val description: String?,
    val questions: List<SurveyQuestionDto>
)

data class SurveyQuestionDto(
    val questionId: String,
    val questionText: String,        // [System] er allerede erstattet med applikationens navn
    val questionPolarity: String,    // vises ikke: svarene gemmes som de er, og vendingen sker i scoringen
    val questionType: String,
    val answerOptions: List<Int>     // 1, 2, 3, 4, 5
)

// Til POST api/respondents/survey/submit
// point er det rå svar 1-5. Backend'en afviser alt uden for 1-5 og vender selv reverse-udsagn.
data class AnswerDto(
    val questionId: String,
    val point: Int
)

// En valgfri kommentar til et tema (højst én pr. tema)
data class CommentDto(
    val themeId: String,
    val freeForm: String
)

// Hele svaret, som sendes til backend'en. Comments skal være en liste (gerne tom), aldrig null.
data class SubmitSurveyRequest(
    val token: String,
    val answers: List<AnswerDto>,
    val comments: List<CommentDto>,
    val roleId: String,
    val regionId: String,
    val experienceId: String
)