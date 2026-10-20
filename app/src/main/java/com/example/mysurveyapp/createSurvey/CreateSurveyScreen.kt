package com.example.mysurveyapp.createSurvey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.components.DateTimePickerButton
import com.example.mysurveyapp.components.DropdownField
import com.example.mysurveyapp.network.CreateAssessmentRequest
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen() {

    // Her gemmer vi teksten fra inputfelterne
    var assessmentName by remember { mutableStateOf("") }
    var applicationName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var survey by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var endTime by remember { mutableStateOf<LocalDateTime?>(null) }

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Overskrift
            item {
                Text(
                    text = "Opret ADA-måling",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(
                        top = 40.dp,
                        bottom = 24.dp
                    )
                )
            }
            // 2. Kort med formularen
            item {
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

                        // 3. Målings navn
                        OutlinedTextField(
                            value = assessmentName, //Teksten der vises i feltet
                            onValueChange = {
                                assessmentName = it
                            }, //Håndterer hvad der sker når brugeren skriver noget.
                            label = {
                                Text("Målingens navn")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true //Bruger kan kun skrive på en linje
                        )

                        // 4. Applikation
                        OutlinedTextField(
                            value = applicationName,
                            onValueChange = {
                                applicationName = it
                            },
                            label = {
                                Text("Applikations navn")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )


                        // 5. Roller
                        var roleInput by remember { mutableStateOf("") }            // det, brugeren skriver lige nu
                        val roles =
                            remember { mutableStateListOf<String>() }                      // listen med gemte roller

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = roleInput,
                                onValueChange = { roleInput = it },
                                label = { Text("Skriv en rolle") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val new =
                                        roleInput.trim()                    // fjerner mellemrum i enderne
                                    if (new.isNotBlank() && new !in roles) {       // ikke tom, og ikke allerede i listen
                                        roles.add(new)
                                        roleInput = ""                           // tøm feltet
                                    }
                                }
                            ) {
                                Text("Tilføj")
                            }
                        }
                        // Dropdown kommer her
                        DropdownField(
                            label = "Vælg rolle",
                            choiceOptions = roles,
                            chosen = role,
                            onChoice = { role = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 6. Region
                        val regionOptions = listOf(
                            "United Kingdom East",
                            "Continental Europe",
                            "United Kingdom West",
                            "Americas",
                            "Asia Pacific",
                            "Middle East",
                            "Africa",
                            "Nordics",
                            "Central Europe",
                            "Southern Europe"
                        )
                        // Dropdown kommer her
                        DropdownField(
                            label = "Vælg region",
                            choiceOptions = regionOptions,
                            chosen = region,
                            onChoice = { region = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 7. Erfaring
                        // Dropdown kommer her
                        DropdownField(
                            label = "Vælg erfaring",
                            choiceOptions = listOf("under 1 år", "1-2 år", "3-5 år", " over 5 år"),
                            chosen = experience,
                            onChoice = { experience = it },
                            modifier = Modifier.fillMaxWidth()
                        )


                        // 8. Survey
                        // Dropdown kommer her
                        DropdownField(
                            label = "Vælg survey",
                            choiceOptions = listOf("ADA-survey"),
                            chosen = survey,
                            onChoice = { survey = it },
                            modifier = Modifier.fillMaxWidth()
                        )


                        // 9. Start- og slutdato


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            DateTimePickerButton(
                                label = "Startdato",
                                value = startTime,
                                onValueChange = { startTime = it },
                                modifier = Modifier.weight(1f)
                            )

                            DateTimePickerButton(
                                label = "Slutdato",
                                value = endTime,
                                onValueChange = { endTime = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        val testScope = rememberCoroutineScope()
                        var testResult by remember { mutableStateOf("") }

                        Button(onClick = {
                            testScope.launch {
                                testResult = "Sender..."
                                testResult = try {
                                    val request = CreateAssessmentRequest(
                                        assessmentName = "Test fra appen",
                                        startDate = Instant.now().plusSeconds(3600).toString(),
                                        endDate = Instant.now().plusSeconds(7200).toString(),
                                        applicationName = "CRM",
                                        roleNames = listOf("IT-medarbejder"),
                                        regionIds = listOf("14829808-54e7-45d1-90c1-6e2b3aa2d007"),
                                        experiences = listOf(0)
                                    )
                                    val response = RetrofitInstance.api.createAssessment(request)
                                    if (response.isSuccessful) {
                                        "Det virkede! Kode ${response.code()}"
                                    } else {
                                        "Fejl ${response.code()}: ${response.errorBody()?.string()}"
                                    }
                                } catch (e: Exception) {
                                    "Kunne ikke nå serveren: ${e.message}"
                                }
                            }
                        }) {
                            Text("Test API")
                        }
                        Text(testResult)

                        // 10. Opret-knap
                        Button(
                            onClick = {
                                //opretter en ny måling
                                val newAssessment = Assessment(
                                    name = assessmentName,
                                    application = applicationName,
                                    role = role,
                                    region = region,
                                    experience = experience,
                                    survey = survey,
                                    start = startTime,
                                    end = endTime
                                )

                                // tilføjer måling til listen
                                AssessmentStorage.assessments.add(newAssessment)

                                //Tømmer felterne efter oprettelse
                                assessmentName = ""
                                applicationName = ""
                                role = ""
                                region = ""
                                experience = ""
                                survey = ""
                                startTime = null
                                endTime = null

                            },
                            enabled = assessmentName.isNotBlank() &&
                            applicationName.isNotBlank() &&
                            role.isNotBlank() &&
                            region.isNotBlank() &&
                            experience.isNotBlank() &&
                            survey.isNotBlank() &&
                            startTime != null &&
                            endTime != null &&
                            endTime!!.isAfter(startTime),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Opret måling",
                                fontSize = 16.sp
                            )
                        }

                    }


                }

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateSurveyPreview() {
    MySurveyAppTheme {
        CreateSurveyScreen()
    }

}