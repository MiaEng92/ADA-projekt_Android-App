package com.example.mysurveyapp.survey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme


fun Color.darken(factor: Float): Color {
    return Color(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha
    )
}

@Composable
fun SurveyScreen(

) {
    val surveyThemes = listOf(
        "Usability" to "Hvor let og intuitivt systemet er",
        "Self-efficacy" to "Brugerens tro på egen evne til at bruge systemet",
        "Enjoyment" to "Om det opleves positivt eller frustrerende at bruge",
        "Confirmation" to "Om systemet lever op til forventningerne.",
        "Organizational Support" to " Om der er hjælp, træning og opbakning omkring brugen.",
        "Job-Fit" to "Om systemet passer til de opgaver, brugeren har.",
        "Data Quality" to "Om data i systemet er korrekte og til at stole på.",
        "Process-fit" to "Om systemet passer til arbejdsgangene.",
        "Workarounds" to "I hvor høj grad folk arbejder uden om systemet.",
        "Switching Benefits" to " Oplevet gevinst ved det nye system frem for det gamle."

    )

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
                ) {

            // Overskrift"
            Text(
                text = "Survey indhold",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 40.dp, bottom = 24.dp)
            )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    items(count = surveyThemes.size) { index ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        val (title, description) = surveyThemes[index]
                        AccordionVariant(
                            title = title,
                            content = description,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }


            }
        }
    }

@Composable
fun AccordionVariant(
    title: String,
    content: String,
    modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "accordion-arrow"
    )

    Box(
        modifier = modifier
            // the background behind the parent container
            .background(
                color = MaterialTheme.colorScheme.primary.darken(0.7f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {

        Column {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded },
                color = MaterialTheme.colorScheme.primary
            )
            {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,   // <- var ""
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween()),
                exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween())
            ) {
                Text(
                    text = content,   // <- var ""
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }


    }


}

@Preview
@Composable
fun PreviewSurveyScreen () {
    MySurveyAppTheme {
        SurveyScreen()
    }

}