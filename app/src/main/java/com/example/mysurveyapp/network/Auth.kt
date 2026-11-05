package com.example.mysurveyapp.network

import com.google.gson.JsonParser

// Det, appen sender til POST api/auth/login (felterne hedder det samme i Swagger)
data class LoginRequest(
    val email: String,
    val password: String
)

// Her gemmes tokenet, mens appen er åben. Det forsvinder, når appen lukkes (bevidst: det er ikke gemt på telefonen).
object TokenStore {
    @Volatile
    var token: String? = null
}

// Finder tokenet i svaret fra login. Jeg ved ikke præcis, hvordan svaret ser ud,
// så den kan læse både { "token": "..." } (og lignende navne) og selve teksten.
fun extractToken(body: String): String? {
    val text = body.trim()
    if (text.isEmpty()) return null

    // Svaret er selve tokenet (evt. med anførselstegn om)
    if (!text.startsWith("{")) return text.trim('"')

    // Svaret er JSON: led efter et felt, der ligner et token
    return try {
        val names = setOf("token", "accesstoken", "access_token", "jwt", "jwttoken", "bearertoken")
        JsonParser.parseString(text).asJsonObject.entrySet()
            .firstOrNull { it.key.lowercase() in names && it.value.isJsonPrimitive }
            ?.value?.asString
    } catch (e: Exception) {
        null
    }
}