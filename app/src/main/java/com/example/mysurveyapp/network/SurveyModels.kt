package com.example.mysurveyapp.network

// Det, backend'en svarer på GET api/respondents/survey?token=...
// Felterne skal hedde præcis det samme som i JSON fra backend'ens GetSurveyResponse.
data class SurveyDto(
    val surveyTitle: String,
    val surveyDescription: String?,
    val assessmentName: String,
    val themes: List<SurveyThemeDto>
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

// ET FORSLAG til de svar, der skal sendes tilbage. Backend'en har endnu ikke et endpoint til det.
data class AnswerDto(
    val questionId: String,
    val value: Int                   // råt svar 1-5, ikke vendt
)