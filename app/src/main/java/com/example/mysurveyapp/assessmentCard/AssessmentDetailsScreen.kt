package com.example.mysurveyapp.assessmentCard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.network.AssessmentDetailsDto
import com.example.mysurveyapp.network.RetrofitInstance
import com.example.mysurveyapp.network.ScoreDto
import com.example.mysurveyapp.network.ThemeScoreDto
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme
import java.time.Instant
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

// Skærmen, der åbnes, når man trykker på en måling.
// Den henter data igen hvert 5. sekund, så man kan følge med, mens folk svarer.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentDetailsScreen(
    assessmentId: String,
    onBackClick: () -> Unit
) {
    var details by remember { mutableStateOf<AssessmentDetailsDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(assessmentId) {
        while (true) {
            try {
                details = RetrofitInstance.api.getDetails(assessmentId)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Kunne ikke hente målingen lige nu"
            }
            delay(5_000)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(details?.assessmentName ?: "Måling") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbage")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            val loaded = details
            if (loaded != null) {
                AssessmentDetailsContent(loaded)
            } else if (errorMessage == null) {
                CircularProgressIndicator()
            }
        }
    }
}

// Selve indholdet, adskilt fra hentningen, så det kan ses i preview uden en server
@Composable
fun AssessmentDetailsContent(details: AssessmentDetailsDto) {
    val percent = (details.responseRate * 100).roundToInt()

    SectionCard("Svarstatus") {
        Text(
            text = "${details.respondedCount} af ${details.invitedCount} har svaret ($percent %)",
            fontWeight = FontWeight.Medium
        )
        LinearProgressIndicator(
            progress = { details.responseRate.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        val ada = details.adaScore
        if (ada == null) {
            Text(
                "ADA Score vises, når mindst 5 har svaret.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            AdaScoreBox(ada)
        }
    }

    SectionCard("Svar over tid") {
        if (details.completedAt.isEmpty()) {
            Text(
                "Ingen har svaret endnu.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ResponsesOverTimeChart(createdAt = details.createdAt, completedAt = details.completedAt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Oprettet", fontSize = 12.sp)
                Text("${details.completedAt.size} svar indtil nu", fontSize = 12.sp)
            }
        }
    }

    SectionCard("Temaer") {
        if (details.themes.isEmpty()) {
            Text(
                "Temaerne vises, når mindst 5 har svaret.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ThemeBars(details.themes)
            Text(
                "De lodrette streger markerer 50 og 70, grænserne mellem båndene.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

// Kurve over, hvor mange der har svaret, som tiden går (akserne justerer sig selv)
@Composable
fun ResponsesOverTimeChart(
    createdAt: String,
    completedAt: List<String>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    val start = remember(createdAt) { Instant.parse(createdAt).toEpochMilli() }
    val times = remember(completedAt) { completedAt.map { Instant.parse(it).toEpochMilli() } }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(160.dp)) {
        val end = maxOf(times.lastOrNull() ?: start, start + 1000L)
        val span = (end - start).toFloat()
        val count = maxOf(times.size, 1).toFloat()

        // akserne
        drawLine(axisColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
        drawLine(axisColor, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2f)

        // kurven: starter i 0 og stiger med hver besvarelse
        val path = Path()
        path.moveTo(0f, size.height)
        times.forEachIndexed { i, t ->
            val x = (t - start).toFloat() / span * size.width
            val y = size.height - (i + 1) / count * size.height
            path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// Én vandret søjle pr. tema, farvet efter bånd. Laveste tema står øverst.
@Composable
fun ThemeBars(themes: List<ThemeScoreDto>) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val thresholdColor = MaterialTheme.colorScheme.outline

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        themes.sortedBy { it.score }.forEach { item ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.theme, fontSize = 14.sp)
                    Text("%.1f".format(item.score), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)) {
                    val radius = CornerRadius(7f, 7f)
                    val fraction = (item.score / 100.0).toFloat().coerceIn(0f, 1f)

                    drawRoundRect(color = backgroundColor, size = size, cornerRadius = radius)
                    drawRoundRect(
                        color = bandColor(item.band),
                        size = Size(size.width * fraction, size.height),
                        cornerRadius = radius
                    )
                    for (threshold in listOf(50f, 70f)) {
                        val x = size.width * threshold / 100f
                        drawLine(thresholdColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
                    }
                }
            }
        }
    }
}

// Eksempeldata, så du kan se skærmen i Android Studio uden at serveren kører
@Preview(showBackground = true, heightDp = 1000)
@Composable
fun AssessmentDetailsPreview() {
    val start = Instant.parse("2026-10-27T10:00:00Z")
    val sample = AssessmentDetailsDto(
        assessmentId = "1",
        assessmentName = "Lego (testdata)",
        applicationName = "CRM",
        status = "Running",
        invitedCount = 120,
        respondedCount = 30,
        responseRate = 30 / 120.0,
        adaScore = ScoreDto(65.4, 61.2, 69.7, "Marginal", 30),
        createdAt = start.toString(),
        completedAt = List(30) { i -> start.plusSeconds((i * i * 4L) + 5).toString() },
        themes = listOf(
            ThemeScoreDto("Usability", 70.1, 63.0, 77.0, "Acceptable", 30),
            ThemeScoreDto("Self-efficacy", 79.9, 74.0, 85.0, "Acceptable", 30),
            ThemeScoreDto("Enjoyment", 59.0, 53.0, 65.0, "Marginal", 30),
            ThemeScoreDto("Data Quality", 48.5, 42.0, 55.0, "Not Acceptable", 30)
        )
    )
    MySurveyAppTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AssessmentDetailsContent(sample)
        }
    }
}