package com.example.mysurveyapp.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.network.LoginRequest
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.network.TokenStore
import com.example.mysurveyapp.network.extractToken
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Composable
fun CreateLoginPage(
    onLoginClick: () -> Unit,
    // Åbner skærmen, hvor en medarbejder skriver sin kode fra invitationen
    onRespondentClick: () -> Unit = {}
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun login() {
        // Test-serveren har intet login, så der springer vi det over
        if (RetrofitInstance.USE_MOCK) {
            onLoginClick()
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // string() læser svaret og må ikke køre på hovedtråden
                val body = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.login(LoginRequest(email.trim(), password)).string()
                }
                val token = extractToken(body)
                if (token == null) {
                    errorMessage = "Login lykkedes, men jeg kunne ikke finde tokenet i svaret."
                } else {
                    TokenStore.token = token
                    onLoginClick()
                }
            } catch (e: HttpException) {
                errorMessage = if (e.code() == 400 || e.code() == 401) {
                    "Forkert e-mail eller kodeord."
                } else {
                    "Serveren svarede ${e.code()}."
                }
            } catch (e: Exception) {
                errorMessage = "Kunne ikke nå serveren: ${e.message}"
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Overskrift
            Text(
                text = "ADA-måling",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(
                    top = 40.dp,
                    bottom = 24.dp
                )
            )

            // 2. Kort med formularen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // 3. E-mail
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 4. Kodeord (vises som prikker)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kodeord") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    // 5. Login-knap
                    Button(
                        onClick = { login() },
                        enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isLoading) "Logger ind..." else "Login",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 6. Medarbejdere skal ikke logge ind. De bruger koden fra deres invitation.
            TextButton(
                onClick = onRespondentClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Jeg har fået en invitation til en survey")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateLoginPagePreview() {
    MySurveyAppTheme {
        CreateLoginPage(onLoginClick = {})
    }
}