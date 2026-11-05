package com.example.mysurveyapp.components

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Felt til at tilføje respondenternes e-mailadresser, én ad gangen.
// Adresserne vises som en liste, hvor hver kan fjernes igen.
// Brug:  EmailListInput(emails = respondentEmails, onEmailsChange = { respondentEmails = it })
@Composable
fun EmailListInput(
    emails: List<String>,
    onEmailsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun addEmail() {
        val email = input.trim()
        when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                errorMessage = "Skriv en gyldig e-mailadresse"
            emails.any { it.equals(email, ignoreCase = true) } ->
                errorMessage = "Adressen er allerede tilføjet"
            else -> {
                onEmailsChange(emails + email)
                input = ""
                errorMessage = null
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    errorMessage = null
                },
                label = { Text("Respondentens e-mail") },
                singleLine = true,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { addEmail() }) {
                Text("Tilføj")
            }
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Text("${emails.size} e-mailadresser tilføjet", fontSize = 12.sp)

        emails.forEach { email ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(email, modifier = Modifier.weight(1f))
                IconButton(onClick = { onEmailsChange(emails - email) }) {
                    Icon(Icons.Default.Close, contentDescription = "Fjern $email")
                }
            }
        }
    }
}