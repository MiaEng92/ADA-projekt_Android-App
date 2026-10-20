package com.example.mysurveyapp.assessmentScreen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.assesmentCard.AssessmentCard
import com.example.mysurveyapp.createSurvey.AssessmentStorage
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = {Text(text = "")},
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = Color(0xFFF7F9FC)
    ){ innerPadding ->

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        // Overskrift
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Text(
                    text = "Igangværende målinger",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        items(AssessmentStorage.assessments) { assessment ->

            AssessmentCard(
                assessment = assessment
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
