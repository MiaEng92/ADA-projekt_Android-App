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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.components.DateTimePickerButton
import com.example.mysurveyapp.components.DropdownField
import com.example.mysurveyapp.components.EmailListInput
import com.example.mysurveyapp.network.CreateAssessmentRequest
import com.example.mysurveyapp.network.ExperienceDto
import com.example.mysurveyapp.network.RegionDto
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.network.experienceLabel
import com.example.mysurveyapp.network.inNaturalOrder
import com.example.mysurveyapp.network.surveyOptions
import com.example.mysurveyapp.network.tilApiTid
import com.example.mysurveyapp.ui.theme.AdaBackground
import com.example.mysurveyapp.ui.theme.AdaText
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen(
    // Kaldes, når målingen er oprettet. Navigation.kt kan bruge den til at åbne listen.
    onCreated: () -> Unit = {}
) {

    // Her gemmer vi teksten fra inputfelterne
    var assessmentName by remember { mutableStateOf("") }
    var applicationName by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("") }            // det, brugeren skriver lige nu
    val roles = remember { mutableStateListOf<String>() }       // listen med gemte roller
    var role by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var survey by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var endTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var respondentEmails by remember { mutableStateOf(listOf<String>()) }

    // Lister, der hentes fra backend'en
    var regions by remember { mutableStateOf<List<RegionDto>>(emptyList()) }
    var experiences by remember { mutableStateOf<List<ExperienceDto>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Sending af målingen
    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val inPreview = LocalInspectionMode.current

    // Henter regioner og erfaring, når skærmen åbner
    LaunchedEffect(Unit) {
        if (inPreview) return@LaunchedEffect
        try {
            regions = RetrofitInstance.api.getRegions().sortedBy { it.regionName }
            experiences = RetrofitInstance.api.getExperiences().inNaturalOrder()
            loadError = null
        } catch (e: Exception) {
            loadError = "Kunne ikke hente regioner og erfaring. Tjek, at serveren kører."
        }
    }

    // Kan målingen oprettes? Alt skal være udfyldt, og slutdatoen skal ligge efter startdatoen.
    val start = startTime
    val end = endTime
    val canCreate = assessmentName.isNotBlank() &&
            applicationName.isNotBlank() &&
            role.isNotBlank() &&
            region.isNotBlank() &&
            experience.isNotBlank() &&
            survey.isNotBlank() &&
            respondentEmails.isNotEmpty() &&
            start != null &&
            end != null &&
            end.isAfter(start) &&
            !isSending

    // Sender målingen til API'et
    fun createAssessment() {
        // Slår de valgte navne op og finder deres id'er
        val regionId = regions.firstOrNull { it.regionName == region }?.regionId
        val experienceId = experiences
            .firstOrNull { experienceLabel(it.experienceValue) == experience }?.experienceId
        val surveyId = surveyOptions.firstOrNull { it.first == survey }?.second

        if (start == null || end == null || regionId == null || experienceId == null || surveyId == null) {
            statusMessage = "Noget mangler, eller listerne er ikke hentet endnu."
            statusIsError = true
            return
        }

        scope.launch {
            isSending = true
            statusMessage = null
            try {
                val request = CreateAssessmentRequest(
                    assessmentName = assessmentName.trim(),
                    startDate = start.tilApiTid(),
                    endDate = end.tilApiTid(),
                    applicationName = applicationName.trim(),
                    surveyId = surveyId,
                    // Den valgte rolle. Skal målgruppen have ALLE de roller, du har tilføjet,
                    // så skriv roles.toList() i stedet for listOf(role).
                    roleNames = listOf(role),
                    regionIds = listOf(regionId),
                    experienceIds = listOf(experienceId),
                    respondentEmails = respondentEmails
                )
                val response = RetrofitInstance.api.createAssessment(request)
                if (response.isSuccessful) {
                    statusMessage = "Målingen er oprettet."
                    statusIsError = false

                    // Tømmer felterne efter oprettelse
                    assessmentName = ""
                    applicationName = ""
                    role = ""
                    region = ""
                    experience = ""
                    survey = ""
                    startTime = null
                    endTime = null
                    respondentEmails = emptyList()

                    onCreated()
                } else {
                    statusMessage = "Fejl ${response.code()}: ${response.errorBody()?.string()}"
                    statusIsError = true
                }
            } catch (e: Exception) {
                statusMessage = "Kunne ikke nå serveren: ${e.message}"
                statusIsError = true
            }
            isSending = false
        }
    }

    Scaffold(
        containerColor = AdaBackground
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
                    color = AdaText,
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
                                    val new = roleInput.trim()                     // fjerner mellemrum i enderne
                                    if (new.isNotBlank() && new !in roles) {       // ikke tom, og ikke allerede i listen
                                        roles.add(new)
                                        roleInput = ""                             // tøm feltet
                                    }
                                }
                            ) {
                                Text("Tilføj")
                            }
                        }
                        DropdownField(
                            label = "Vælg rolle",
                            choiceOptions = roles,
                            chosen = role,
                            onChoice = { role = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 6. Region (hentes fra backend'en)
                        DropdownField(
                            label = "Vælg region",
                            choiceOptions = regions.map { it.regionName },
                            chosen = region,
                            onChoice = { region = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 7. Erfaring (hentes fra backend'en, og koden vises som dansk tekst)
                        DropdownField(
                            label = "Vælg erfaring",
                            choiceOptions = experiences.map { experienceLabel(it.experienceValue) },
                            chosen = experience,
                            onChoice = { experience = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 8. Survey
                        DropdownField(
                            label = "Vælg survey",
                            choiceOptions = surveyOptions.map { it.first },
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

                        // 10. Respondenternes e-mailadresser
                        Text(
                            text = "Respondenter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        EmailListInput(
                            emails = respondentEmails,
                            onEmailsChange = { respondentEmails = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Fejl og status
                        loadError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        statusMessage?.let {
                            Text(
                                text = it,
                                color = if (statusIsError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        }

                        // 11. Opret-knap
                        Button(
                            onClick = { createAssessment() },
                            enabled = canCreate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isSending) "Opretter..." else "Opret måling",
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