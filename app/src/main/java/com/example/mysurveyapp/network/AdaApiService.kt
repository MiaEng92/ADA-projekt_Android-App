package com.example.mysurveyapp.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AdaApiService {

    @POST("api/assessments")
    suspend fun createAssessment(
        @Body request: CreateAssessmentRequest
    ): Response<ResponseBody>

    // Liste over målinger med svarstatus og ADA Score
    @GET("api/assessments")
    suspend fun getAssessments(): List<AssessmentSummaryDto>

    // Detaljer for én måling: status, hvornår svarene kom ind og tema-scores
    @GET("api/assessments/{id}/details")
    suspend fun getDetails(@Path("id") id: String): AssessmentDetailsDto

    // Surveyen, som en medarbejder besvarer. Koden (token) står i invitationen.
    @GET("api/respondents/survey")
    suspend fun getSurvey(@Query("token") token: String): SurveyDto

    // Alle regioner med id og navn (til dropdownen i formularen)
    @GET("api/regions")
    suspend fun getRegions(): List<RegionDto>

    // Alle erfaringsintervaller med id og værdi (til dropdownen i formularen)
    @GET("api/experiences")
    suspend fun getExperiences(): List<ExperienceDto>

}