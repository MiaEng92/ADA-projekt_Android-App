package com.example.mysurveyapp.assesmentCard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.createSurvey.Assessment
import java.time.format.DateTimeFormatter

@Composable
fun AssessmentCard(
    assessment: Assessment
) {
    val format = DateTimeFormatter.ofPattern("dd/MM/yyy HH.mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // Navn til venstre, status til højre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assessment.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Lille "badge" med status
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = assessment.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }

            Text(text = "Applikation: ${assessment.application}")
            Text(text = "Rolle: ${assessment.role}")
            Text(text = "Region: ${assessment.region}")
            Text(text = "Erfaring: ${assessment.experience}")
            Text(text = "Survey: ${assessment.survey}")

            // Vises kun, hvis datoen er valgt
            assessment.start?.let { Text(text = "Start: ${it.format(format)}") }
            assessment.end?.let { Text(text = "Slut: ${it.format(format)}") }
        }
    }
}