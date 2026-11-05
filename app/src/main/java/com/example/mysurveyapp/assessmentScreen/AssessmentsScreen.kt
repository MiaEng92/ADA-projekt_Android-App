package com.example.mysurveyapp.assessmentScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.assessmentCard.AssessmentSummaryCard
import com.example.mysurveyapp.network.AssessmentSummaryDto
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.ui.theme.AdaBackground
import com.example.mysurveyapp.ui.theme.AdaText
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentsScreen(
    // Kaldes med målingens id, når man trykker på et kort (Navigation.kt åbner detaljerne)
    onAssessmentClick: (String) -> Unit = {}
) {
    var assessments by remember { mutableStateOf<List<AssessmentSummaryDto>>(emptyList()) }
    var hasLoaded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val inPreview = LocalInspectionMode.current

    // Henter listen fra API'et og henter den igen hvert 10. sekund, så man kan følge med,
    // mens folk svarer. Løkken stopper af sig selv, når man forlader skærmen.
    LaunchedEffect(Unit) {
        if (inPreview) return@LaunchedEffect
        while (true) {
            try {
                assessments = RetrofitInstance.api.getAssessments()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Kunne ikke hente målinger lige nu"
            }
            hasLoaded = true
            delay(10_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = AdaBackground
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overskrift
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Igangværende målinger",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdaText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            errorMessage?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error) }
            }

            if (hasLoaded && assessments.isEmpty() && errorMessage == null) {
                item { Text("Du har ingen målinger endnu.") }
            }

            items(assessments, key = { it.assessmentId }) { assessment ->
                AssessmentSummaryCard(
                    summary = assessment,
                    onClick = { onAssessmentClick(assessment.assessmentId) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssessmentPreview() {
    MySurveyAppTheme {
        AssessmentsScreen()
    }
}