package com.example.mysurveyapp.frontpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysurveyapp.ui.theme.MySurveyAppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Frontpage(onFrontPageClick: () -> Unit, onSurveyClick: () -> Unit, onAssessmentClick: () -> Unit) {
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Overskrift
            Text(
                text = "Velkommen Mia!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(
                    top = 40.dp,
                    bottom = 24.dp
                )
            )

            // 2. Kort med formularen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            )
            {
                Column(modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 3. Opret-måling knap
                    Button(
                        onClick = {
                            onFrontPageClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Opret måling",
                            fontSize = 16.sp
                        )
                    }


                    // 4. Igangværende måling knap
                    Button(
                        onClick = {
                            onAssessmentClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Igangværende målinger",
                            fontSize = 16.sp
                        )
                    }

                    // 5. Se eksempel knap
                    Button(
                        onClick = {
                            onSurveyClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Se eksempel på survey",
                            fontSize = 16.sp
                        )
                    }

                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun FrontpagePreview() {
    MySurveyAppTheme {
        Frontpage(
            onFrontPageClick = {}, onSurveyClick = {}, onAssessmentClick = {})
    }

}