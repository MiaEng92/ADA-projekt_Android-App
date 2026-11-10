package com.example.mysurveyapp.createSurvey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.mysurveyapp.components.MultiSelectDropdownField
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

// Formularen er delt i tre trin, så hvert trin passer på en skærm
private val stepTitles = listOf("Om målingen", "Målgruppe", "Tid og respondenter")

@Composable
fun CreateSurveyScreen(
    // Kaldes, når målingen er oprettet. Navigation.kt kan bruge den til at åbne listen.
    onCreated: () -> Unit = {}
) {

    // Hvilket trin er vi på? 0, 1 eller 2
    var step by remember { mutableIntStateOf(0) }

    // Her gemmer vi teksten fra inputfelterne (de ligger her, så de bevares, når man går frem og tilbage)
    var assessmentName by remember { mutableStateOf("") }
    var applicationName by remember { mutableStateOf("") }
    var survey by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("") }            // det, brugeren skriver lige nu
    val roles = remember { mutableStateListOf<String>() }       // listen med gemte roller
    var selectedRoles by remember { mutableStateOf(setOf<String>()) }         // de roller, produktejeren har afkrydset
    var selectedRegions by remember { mutableStateOf(setOf<String>()) }       // de regioner, produktejeren har valgt
    var selectedExperiences by remember { mutableStateOf(setOf<String>()) }   // de erfaringer, produktejeren har valgt
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
            loadError = "Kunne ikke hente regioner og erfaring: ${e.message}"
        }
    }

    // Er det aktuelle trin udfyldt? Så må man gå videre.
    val start = startTime
    val end = endTime
    val step1Done = assessmentName.isNotBlank() && applicationName.isNotBlank() && survey.isNotBlank()
    val step2Done = roles.any { it in selectedRoles } && selectedRegions.isNotEmpty() && selectedExperiences.isNotEmpty()
    val step3Done = respondentEmails.isNotEmpty() && start != null && end != null && end.isAfter(start)
    val canContinue = when (step) {
        0 -> step1Done
        1 -> step2Done
        else -> step3Done && !isSending
    }
    val isLastStep = step == stepTitles.lastIndex

    // Sender målingen til API'et
    fun createAssessment() {
        // Slår de valgte navne op og finder deres id'er
        val roleNames = roles.filter { it in selectedRoles }
        val regionIds = regions.filter { it.regionName in selectedRegions }.map { it.regionId }
        val experienceIds = experiences
            .filter { experienceLabel(it.experienceValue) in selectedExperiences }.map { it.experienceId }
        val surveyId = surveyOptions.firstOrNull { it.first == survey }?.second

        if (start == null || end == null || roleNames.isEmpty() || regionIds.isEmpty() ||
            experienceIds.isEmpty() || surveyId == null
        ) {
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
                    // Alle de roller, regioner og erfaringer, produktejeren har valgt.
                    // Det er dem, respondenterne selv kan vælge imellem.
                    roleNames = roleNames,
                    regionIds = regionIds,
                    experienceIds = experienceIds,
                    respondentEmails = respondentEmails
                )
                val response = RetrofitInstance.api.createAssessment(request)
                if (response.isSuccessful) {
                    statusMessage = "Målingen er oprettet."
                    statusIsError = false

                    // Tømmer felterne og går tilbage til første trin
                    assessmentName = ""
                    applicationName = ""
                    survey = ""
                    roles.clear()
                    selectedRoles = emptySet()
                    selectedRegions = emptySet()
                    selectedExperiences = emptySet()
                    startTime = null
                    endTime = null
                    respondentEmails = emptyList()
                    step = 0

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

    Scaffold(containerColor = AdaBackground) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Overskrift
            Text(
                text = "Opret ADA-måling",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AdaText,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )

            // Hvor langt er jeg?
            Text(
                text = "Trin ${step + 1} af ${stepTitles.size} · ${stepTitles[step]}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { (step + 1).toFloat() / stepTitles.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Kort med det aktuelle trin
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        0 -> {
                            // Trin 1: Om målingen
                            OutlinedTextField(
                                value = assessmentName,
                                onValueChange = { assessmentName = it },
                                label = { Text("Målingens navn") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = applicationName,
                                onValueChange = { applicationName = it },
                                label = { Text("Applikationens navn") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            DropdownField(
                                label = "Vælg survey",
                                choiceOptions = surveyOptions.map { it.first },
                                chosen = survey,
                                onChoice = { survey = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        1 -> {
                            // Trin 2: Målgruppe. Det, du vælger her, er det, respondenterne selv kan vælge imellem.
                            Text(
                                text = "Respondenterne vælger selv blandt det, du vælger her. Vælg mindst ét af hver.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Roller: skriv dem ind én ad gangen, og afkryds dem, der skal med
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
                                        val new = roleInput.trim()                  // fjerner mellemrum i enderne
                                        if (new.isNotBlank() && new !in roles) {    // ikke tom, og ikke allerede i listen
                                            roles.add(new)
                                            selectedRoles = selectedRoles + new     // en ny rolle er afkrydset fra start
                                            roleInput = ""                          // tøm feltet
                                        }
                                    }
                                ) {
                                    Text("Tilføj")
                                }
                            }
                            MultiSelectDropdownField(
                                label = "Vælg roller",
                                choiceOptions = roles,
                                chosen = selectedRoles,
                                onChosenChange = { selectedRoles = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Regioner og erfaring: afkryds så mange, du vil (hentes fra backend'en)
                            MultiSelectDropdownField(
                                label = "Vælg regioner",
                                choiceOptions = regions.map { it.regionName },
                                chosen = selectedRegions,
                                onChosenChange = { selectedRegions = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            MultiSelectDropdownField(
                                label = "Vælg erfaringer",
                                choiceOptions = experiences.map { experienceLabel(it.experienceValue) },
                                chosen = selectedExperiences,
                                onChosenChange = { selectedExperiences = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            loadError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        else -> {
                            // Trin 3: Tid og respondenter
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

                            // En måling kan først åbnes af respondenterne, når startdatoen er nået
                            if (start != null && start.isAfter(LocalDateTime.now())) {
                                Text(
                                    text = "Målingen åbner først på startdatoen. Før da kan respondenterne ikke åbne surveyen.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (start != null && end != null && !end.isAfter(start)) {
                                Text(
                                    text = "Slutdatoen skal ligge efter startdatoen.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

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
                        }
                    }
                }
            }

            // Status efter afsendelse
            statusMessage?.let {
                Text(
                    text = it,
                    color = if (statusIsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            // Knapper: Tilbage og Næste (på sidste trin: Opret måling)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tilbage", fontSize = 16.sp)
                    }
                }
                Button(
                    onClick = { if (isLastStep) createAssessment() else step += 1 },
                    enabled = canContinue,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            isSending -> "Opretter..."
                            isLastStep -> "Opret måling"
                            else -> "Næste"
                        },
                        fontSize = 16.sp
                    )
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