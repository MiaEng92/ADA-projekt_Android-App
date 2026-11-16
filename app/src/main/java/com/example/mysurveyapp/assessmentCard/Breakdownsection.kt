package com.example.mysurveyapp.assessmentCard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.network.BreakdownDto
import com.example.mysurveyapp.network.BreakdownGroupDto
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.network.ScoreDto
import com.example.mysurveyapp.network.ThemeScoreDto
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import retrofit2.HttpException

private val NAME_COLUMN_WIDTH = 112.dp
private val GROUP_COLUMN_WIDTH = 76.dp
private val HEADER_HEIGHT = 56.dp
private val CELL_HEIGHT = 38.dp
private const val MIN_ANSWERS = 5          // under det antal vises ingen nedbrydning (anonymitet)

// Navnene, brugeren ser, ud fra den kode serveren bruger
private fun groupingLabel(code: String): String = when (code) {
    "region" -> "Region"
    "role" -> "Rolle"
    "experience" -> "Erfaring"
    else -> code
}

// Indholdet i kortet "Pr. gruppe": vælg inddeling, og se scoren pr. gruppe og tema som et varmekort.
// Hentes fra serveren igen, hver gang antallet af svar ændrer sig, så det følger med.
@Composable
fun BreakdownContent(assessmentId: String, answerCount: Int) {
    var grouping by remember { mutableStateOf("region") }
    var data by remember { mutableStateOf<BreakdownDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val inPreview = LocalInspectionMode.current

    LaunchedEffect(assessmentId, grouping, answerCount) {
        if (inPreview || answerCount < MIN_ANSWERS) return@LaunchedEffect
        try {
            data = RetrofitInstance.api.getBreakdown(assessmentId, grouping)
            errorMessage = null
        } catch (e: HttpException) {
            errorMessage = "Kunne ikke hente nedbrydningen (serveren svarede ${e.code()})."
        } catch (e: Exception) {
            errorMessage = "Kunne ikke hente nedbrydningen: ${e.message}"
        }
    }

    val current = data?.takeIf { it.groupBy == grouping }
    val options = data?.availableBy ?: listOf("region")

    when {
        inPreview -> Text("(Nedbrydningen hentes fra serveren)", fontSize = 14.sp)
        answerCount < MIN_ANSWERS -> Text(
            "Nedbrydninger vises, når mindst $MIN_ANSWERS har svaret.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> {
            // Vælg, hvad der skal grupperes efter
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    if (option == grouping) {
                        Button(onClick = { grouping = option }, contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Text(groupingLabel(option))
                        }
                    } else {
                        OutlinedButton(onClick = { grouping = option }, contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Text(groupingLabel(option))
                        }
                    }
                }
            }

            val error = errorMessage
            when {
                error != null -> Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                current == null -> CircularProgressIndicator()
                else -> {
                    BreakdownTable(current)
                    Text(
                        text = "Farven viser båndet. Grupper med under ${current.minGroup} besvarelser vises ikke, " +
                                "så ingen kan udpeges.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Temaerne som rækker og grupperne som kolonner. Kolonnerne kan scrolles sidelæns, hvis der er mange.
@Composable
fun BreakdownTable(data: BreakdownDto) {
    val rowLabels = listOf("ADA Score") +
            (data.groups.firstOrNull { !it.hidden }?.themes?.map { it.theme } ?: emptyList())

    Row(modifier = Modifier.fillMaxWidth()) {
        // Venstre: rækkernes navne. De står stille, mens kolonnerne scroller.
        Column(modifier = Modifier.width(NAME_COLUMN_WIDTH)) {
            Spacer(modifier = Modifier.height(HEADER_HEIGHT))
            rowLabels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .height(CELL_HEIGHT)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Højre: en kolonne pr. gruppe
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            data.groups.forEach { group ->
                GroupColumn(group = group, rowLabels = rowLabels)
            }
        }
    }
}

@Composable
private fun GroupColumn(group: BreakdownGroupDto, rowLabels: List<String>) {
    Column(modifier = Modifier.width(GROUP_COLUMN_WIDTH)) {
        // Overskrift: gruppens navn og antal. En skjult gruppe viser ikke antallet.
        Column(
            modifier = Modifier
                .height(HEADER_HEIGHT)
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = group.group,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (group.hidden) "få svar" else "n=${group.n}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        rowLabels.forEachIndexed { index, label ->
            val score: ScoreDto? =
                if (index == 0) group.adaScore
                else group.themes.firstOrNull { it.theme == label }?.let { it.toScore() }
            HeatCell(score = score, bold = index == 0)
        }
    }
}

// Én celle: båndets farve (lysnet) og tallet. En skjult gruppe er grå uden tal.
@Composable
private fun HeatCell(score: ScoreDto?, bold: Boolean) {
    val background = if (score == null) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        bandColor(score.band).copy(alpha = 0.45f)
    }
    Box(
        modifier = Modifier
            .height(CELL_HEIGHT)
            .fillMaxWidth()
            .padding(1.5.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (score == null) "" else "%.1f".format(score.score),
            fontSize = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Et tema har de samme felter som en score. Det her samler dem, så cellen kan bruge dem ens.
private fun ThemeScoreDto.toScore() = ScoreDto(score, ci95Low, ci95High, band, n)

// Eksempeldata, så du kan se tabellen i Android Studio uden at serveren kører
@Preview(showBackground = true, widthDp = 380)
@Composable
fun BreakdownTablePreview() {
    fun theme(name: String, score: Double, band: String) = ThemeScoreDto(name, score, null, null, band, 20)
    fun group(name: String, ada: Double, band: String, a: Double, b: Double) = BreakdownGroupDto(
        group = name, hidden = false, n = 20,
        adaScore = ScoreDto(ada, null, null, band, 20),
        themes = listOf(
            theme("Usability", a, "Marginal"),
            theme("Data Quality", b, if (b < 50) "Not Acceptable" else "Marginal")
        )
    )

    MySurveyAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BreakdownTable(
                BreakdownDto(
                    assessmentId = "1", groupBy = "region", availableBy = listOf("region", "experience"), minGroup = 5,
                    groups = listOf(
                        group("Middle East", 46.5, "Not Acceptable", 55.8, 27.5),
                        group("Nordics", 63.1, "Marginal", 67.8, 52.0),
                        BreakdownGroupDto("Americas", true, null, null, emptyList())
                    )
                )
            )
        }
    }
}