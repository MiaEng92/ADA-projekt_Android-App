package com.example.mysurveyapp.respondents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.components.DropdownField
import com.example.mysurveyapp.network.AnswerDto
import com.example.mysurveyapp.network.CommentDto
import com.example.mysurveyapp.network.ExperienceDto
import com.example.mysurveyapp.network.RegionDto
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.network.RoleDto
import com.example.mysurveyapp.network.SubmitSurveyRequest
import com.example.mysurveyapp.network.SurveyDto
import com.example.mysurveyapp.network.SurveyQuestionDto
import com.example.mysurveyapp.network.SurveyThemeDto
import com.example.mysurveyapp.network.experienceLabel
import com.example.mysurveyapp.ui.theme.AdaBackground
import com.example.mysurveyapp.ui.theme.AdaText
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

// Det, skærmen samler ind og sender til serveren
data class SurveySubmission(
    val answers: List<AnswerDto>,
    val comments: List<CommentDto>,
    val roleId: String,
    val regionId: String,
    val experienceId: String
)

// Sender svaret. Giver null, hvis det lykkedes, ellers en tekst, der kan vises for brugeren.
private suspend fun sendToServer(token: String, submission: SurveySubmission): String? {
    return try {
        val request = SubmitSurveyRequest(
            token = token,
            answers = submission.answers,
            comments = submission.comments,   // En liste, gerne tom. Backend'en afviser null
            roleId = submission.roleId,
            regionId = submission.regionId,
            experienceId = submission.experienceId
        )
        val response = RetrofitInstance.api.submitSurvey(request)
        if (response.isSuccessful) {
            null
        } else {
            val body = withContext(Dispatchers.IO) { response.errorBody()?.string() }
            errorTextFrom(body, response.code())
        }
    } catch (e: Exception) {
        "Kunne ikke nå serveren: ${e.message}"
    }
}

// Backend'en svarer med { "code": "...", "message": "..." }. Vi viser dens besked.
private fun errorTextFrom(body: String?, code: Int): String {
    val message = runCatching {
        if (body.isNullOrBlank()) {
            null
        } else {
            JsonParser.parseString(body).asJsonObject.get("message")
                ?.takeIf { it.isJsonPrimitive }?.asString
        }
    }.getOrNull()   // kunne svaret ikke læses som JSON, bliver beskeden null
    return if (message.isNullOrBlank()) "Svaret kunne ikke sendes (serveren svarede $code)." else message
}

// Skærm 1: medarbejderen skriver den kode, de har fået i sin invitation.
@Composable
fun EnterTokenScreen(
    onStart: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var token by remember { mutableStateOf("") }

    Scaffold(containerColor = AdaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Besvar survey",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AdaText,
                modifier = Modifier.padding(top = 40.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Skriv den kode, du har fået i din invitation.")
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Kode fra invitationen") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onStart(token.trim()) },
                        enabled = token.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start survey", fontSize = 16.sp)
                    }
                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tilbage")
                    }
                }
            }
        }
    }
}

// Skærm 2: henter surveyen med koden, viser den og sender svaret.
@Composable
fun RespondentSurveyScreen(
    token: String,
    onBackClick: () -> Unit
) {
    var survey by remember { mutableStateOf<SurveyDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        survey = null
        errorMessage = null
        try {
            survey = RetrofitInstance.api.getSurvey(token)
        } catch (e: HttpException) {
            // Backend'en svarer med den samme fejl, uanset om koden er forkert, brugt eller målingen er lukket
            errorMessage = "Koden virker ikke. Den kan være forkert, allerede brugt, eller målingen er ikke åben " +
                    "(serveren svarede ${e.code()})."
        } catch (e: Exception) {
            errorMessage = "Kunne ikke nå serveren lige nu: ${e.message}"
        }
    }

    Scaffold(containerColor = AdaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val loaded = survey
            val error = errorMessage
            when {
                error != null -> {
                    Text(
                        text = "Besvar survey",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdaText,
                        modifier = Modifier.padding(top = 40.dp, bottom = 24.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                            Button(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Tilbage", fontSize = 16.sp)
                            }
                        }
                    }
                }
                loaded != null -> SurveyContent(
                    survey = loaded,
                    onSubmit = { submission -> sendToServer(token, submission) },
                    onExit = onBackClick
                )
                else -> CircularProgressIndicator(modifier = Modifier.padding(top = 80.dp))
            }
        }
    }
}

// Selve besvarelsen. Først en side "Om dig" (rolle, region, erfaring), derefter ét tema ad gangen.
// onSubmit giver null, hvis svaret blev sendt, ellers en fejltekst.
@Composable
fun SurveyContent(
    survey: SurveyDto,
    onSubmit: suspend (SurveySubmission) -> String?,
    onExit: () -> Unit
) {
    val themes = survey.themes
    val roles = survey.roles.orEmpty()
    val regions = survey.regions.orEmpty()
    val experiences = survey.experiences.orEmpty()

    // Backend'en sender ikke altid valgmulighederne endnu. Uden dem kan man se surveyen, men ikke sende den.
    val hasChoices = roles.isNotEmpty() && regions.isNotEmpty() && experiences.isNotEmpty()
    val introSteps = if (hasChoices) 1 else 0
    val totalSteps = introSteps + themes.size

    val answers = remember { mutableStateMapOf<String, Int>() }      // spørgsmåls-id -> svar 1-5
    val comments = remember { mutableStateMapOf<String, String>() }  // tema-id -> kommentar
    var role by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }
    var sending by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (themes.isEmpty()) {
        Text("Surveyen har ingen spørgsmål.", modifier = Modifier.padding(top = 40.dp))
    } else if (done) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tak for dit svar!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AdaText,
                modifier = Modifier.padding(top = 40.dp, bottom = 24.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Dit svar er sendt. Du kan lukke appen nu.")
                    Button(
                        onClick = onExit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Luk", fontSize = 16.sp)
                    }
                }
            }
        }
    } else {
        val onIntro = hasChoices && step == 0
        val theme = if (onIntro) null else themes[step - introSteps]
        val isLast = step == totalSteps - 1

        // De valgte navne slås op, så vi får id'erne, som backend'en vil have
        val roleId = roles.firstOrNull { it.roleName == role }?.roleId
        val regionId = regions.firstOrNull { it.regionName == region }?.regionId
        val experienceId = experiences
            .firstOrNull { experienceLabel(it.experienceValue) == experience }?.experienceId
        val introDone = roleId != null && regionId != null && experienceId != null

        val canContinue = when {
            theme == null -> introDone
            isLast -> theme.questions.all { answers.containsKey(it.questionId) } && introDone && !sending
            else -> theme.questions.all { answers.containsKey(it.questionId) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hvor langt er jeg?
            Text(
                text = if (theme == null) "Før du starter" else "Tema ${step - introSteps + 1} af ${themes.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (step + 1).toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth()
            )

            if (!hasChoices) {
                Text(
                    text = "Serveren sender ikke dine valgmuligheder (rolle, region og erfaring) endnu. " +
                            "Du kan se surveyen, men ikke sende dit svar.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (theme == null) {
                // Side 0: Om dig
                Text(text = "Om dig", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AdaText)
                Text(
                    text = "Vælg det, der passer bedst på dig. Det bruges til at vise analyse for grupper.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DropdownField(
                            label = "Din rolle",
                            choiceOptions = roles.map { it.roleName },
                            chosen = role,
                            onChoice = { role = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownField(
                            label = "Din region",
                            choiceOptions = regions.map { it.regionName },
                            chosen = region,
                            onChoice = { region = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownField(
                            label = "Din erfaring",
                            choiceOptions = experiences.map { experienceLabel(it.experienceValue) },
                            chosen = experience,
                            onChoice = { experience = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Temaets titel og beskrivelse
                Text(text = theme.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AdaText)
                // backend'en sender "" (ikke null), når et tema ingen beskrivelse har
                theme.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Ét kort med de tre udsagn, ligesom formularen er ét kort
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        theme.questions.forEachIndexed { index, question ->
                            if (index > 0) HorizontalDivider()
                            QuestionBlock(
                                question = question,
                                selected = answers[question.questionId],
                                onSelect = { answers[question.questionId] = it }
                            )
                        }

                        HorizontalDivider()
                        OutlinedTextField(
                            value = comments[theme.themeId] ?: "",
                            onValueChange = { comments[theme.themeId] = it },
                            label = { Text("Vil du tilføje noget om dette tema? (valgfrit)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // Knapper: Tilbage (eller Afbryd) og Næste (eller Send besvarelse)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { if (step == 0) onExit() else step -= 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (step == 0) "Afbryd" else "Tilbage", fontSize = 16.sp)
                }
                Button(
                    onClick = {
                        if (!isLast) {
                            step += 1
                        } else {
                            scope.launch {
                                val chosenRole = roleId ?: return@launch
                                val chosenRegion = regionId ?: return@launch
                                val chosenExperience = experienceId ?: return@launch
                                sending = true
                                errorMessage = null
                                val submission = SurveySubmission(
                                    answers = answers.map { (id, point) -> AnswerDto(id, point) },
                                    comments = comments
                                        .filterValues { it.isNotBlank() }
                                        .map { (id, text) -> CommentDto(id, text.trim()) },
                                    roleId = chosenRole,
                                    regionId = chosenRegion,
                                    experienceId = chosenExperience
                                )
                                val error = onSubmit(submission)
                                sending = false
                                if (error == null) done = true else errorMessage = error
                            }
                        }
                    },
                    // Først aktiv, når siden er udfyldt (og, på den sidste side, rolle, region og erfaring er valgt)
                    enabled = canContinue,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (sending) "Sender..." else if (isLast) "Send besvarelse" else "Næste",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// Ét udsagn med skalaen under
@Composable
fun QuestionBlock(
    question: SurveyQuestionDto,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(question.questionText, fontSize = 16.sp)
        LikertRow(
            options = question.answerOptions.ifEmpty { listOf(1, 2, 3, 4, 5) },
            selected = selected,
            onSelect = onSelect
        )
    }
}

// Fem knapper fra 1 til 5. Den valgte er fyldt ud.
@Composable
fun LikertRow(
    options: List<Int>,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { value ->
                if (selected == value) {
                    Button(
                        onClick = { onSelect(value) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("$value")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelect(value) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("$value")
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Helt uenig", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Helt enig", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Eksempeldata, så man kan se skærmen i Android Studio uden at serveren kører
private fun sampleSurvey(withChoices: Boolean) = SurveyDto(
    surveyTitle = "ADA",
    surveyDescription = "Standardiseret ADA-survey",
    assessmentName = "Lego (testdata)",
    themes = listOf(
        SurveyThemeDto(
            themeId = "1",
            title = "Enjoyment",
            description = "Hvor behageligt og positivt brugeren oplever arbejdet med systemet.",
            questions = listOf(
                SurveyQuestionDto("q1", "Jeg kan godt lide at arbejde i CRM.", "Positive", "Scale", listOf(1, 2, 3, 4, 5)),
                SurveyQuestionDto("q2", "CRM gør mit arbejde mere behageligt.", "Positive", "Scale", listOf(1, 2, 3, 4, 5)),
                SurveyQuestionDto("q3", "At bruge CRM er kedeligt og besværligt.", "Reverse", "Scale", listOf(1, 2, 3, 4, 5))
            )
        ),
        SurveyThemeDto("2", "Self-efficacy", "", emptyList())
    ),
    roles = if (withChoices) listOf(RoleDto("r1", "Sagsbehandler"), RoleDto("r2", "Leder")) else null,
    regions = if (withChoices) listOf(RegionDto("g1", "Nordics"), RegionDto("g2", "Americas")) else null,
    experiences = if (withChoices) listOf(ExperienceDto("e1", "LessThan1Year"), ExperienceDto("e2", "MoreThan5Years")) else null
)

@Preview(showBackground = true, heightDp = 900)
@Composable
fun SurveyIntroPreview() {
    MySurveyAppTheme {
        Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize()) {
            SurveyContent(survey = sampleSurvey(withChoices = true), onSubmit = { null }, onExit = {})
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun SurveyThemePreview() {
    MySurveyAppTheme {
        Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize()) {
            SurveyContent(survey = sampleSurvey(withChoices = false), onSubmit = { null }, onExit = {})
        }
    }
}