package com.example.mysurveyapp.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface AdaApiService {

    // RET STIEN: skriv den præcis, som den står ved POST i Swagger,
    // men uden "/" forrest. Stien her er kun et gæt.
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


}