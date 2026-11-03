package com.example.mysurveyapp.assesmentCard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.network.AssessmentSummaryDto
import com.example.mysurveyapp.network.ScoreDto
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import com.example.mysurveyapp.ui.theme.ScoreGreen
import com.example.mysurveyapp.ui.theme.ScoreRed
import com.example.mysurveyapp.ui.theme.ScoreYellow
import kotlin.math.roundToInt

// Farven for et bånd. Selve farverne står i ui/theme/Color.kt.
// Hvilket bånd et tal hører til (under 50 / 50-70 / over 70) afgør serveren, ikke appen.
fun bandColor(band: String): Color = when (band) {
    "Not Acceptable" -> ScoreRed
    "Marginal" -> ScoreYellow
    else -> ScoreGreen
}

// Det store tal med farve, og teksten ved siden af
@Composable
fun AdaScoreBox(ada: ScoreDto) {
    val textColor = if (ada.band == "Marginal") Color.Black else Color.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bandColor(ada.band)
        ) {
            Text(
                text = "%.0f".format(ada.score),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        Column {
            Text("ADA Score: ${ada.band}", fontWeight = FontWeight.SemiBold)
            if (ada.ci95Low != null && ada.ci95High != null) {
                Text(
                    "95 % interval: ${"%.1f".format(ada.ci95Low)} - ${"%.1f".format(ada.ci95High)}",
                    fontSize = 12.sp
                )
            }
            Text("Baseret på ${ada.n} svar", fontSize = 12.sp)
        }
    }
}

// Kortet for én måling: hvor mange har svaret, og den samlede ADA Score
@Composable
fun AssessmentSummaryCard(
    summary: AssessmentSummaryDto,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val percent = (summary.responseRate * 100).roundToInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            // hvis der er en onClick, kan hele kortet trykkes på
            modifier = (if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = summary.assessmentName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Applikation: ${summary.applicationName}")

            // Svarstatus
            Text(
                text = "${summary.respondedCount} af ${summary.invitedCount} har svaret ($percent %)",
                fontWeight = FontWeight.Medium
            )
            LinearProgressIndicator(
                progress = { summary.responseRate.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // ADA Score (først når nok har svaret, ellers sender backend'en ingenting)
            val ada = summary.adaScore
            if (ada == null) {
                Text(
                    "ADA Score vises, når nok har svaret.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                AdaScoreBox(ada)
            }
        }
    }
}

// Eksempeldata, så du kan se kortet i Android Studio uden at backend'en er klar
@Preview(showBackground = true)
@Composable
fun AssessmentSummaryCardPreview() {
    MySurveyAppTheme {
        AssessmentSummaryCard(
            AssessmentSummaryDto(
                assessmentId = "1",
                assessmentName = "Lego",
                applicationName = "CRM",
                status = "Running",
                invitedCount = 120,
                respondedCount = 38,
                responseRate = 38 / 120.0,
                adaScore = ScoreDto(65.4, 63.1, 67.7, "Marginal", 38)
            )
        )
    }
}