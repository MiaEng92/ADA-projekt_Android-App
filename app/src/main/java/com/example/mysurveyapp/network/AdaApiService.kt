package com.example.mysurveyapp.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AdaApiService {

    // RET STIEN: skriv den præcis, som den står ved POST i Swagger,
    // men uden "/" forrest. Stien her er kun et gæt.
    @POST("api/assessments")
    suspend fun createAssessment(
        @Body request: CreateAssessmentRequest
    ): Response<ResponseBody>
}