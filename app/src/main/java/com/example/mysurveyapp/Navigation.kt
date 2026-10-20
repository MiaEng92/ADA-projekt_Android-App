package com.example.mysurveyapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mysurveyapp.createSurvey.CreateSurveyScreen
import com.example.mysurveyapp.frontpage.Frontpage
import com.example.mysurveyapp.login.CreateLoginPage
import com.example.mysurveyapp.survey.SurveyScreen
import com.example.mysurveyapp.assessmentScreen.AssessmentsScreen


@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            CreateLoginPage(
                onLoginClick = {
                    navController.navigate("welcome")
                }
            )
        }

        composable("welcome") {
            Frontpage(
                onFrontPageClick = {
                    navController.navigate("create_measurement")
                },
                onSurveyClick = {
                    navController.navigate("view_survey")
                },
                onAssessmentClick = {
                    navController.navigate("assessments")
                }
            )
        }

        composable("create_measurement") {
            CreateSurveyScreen()
        }

        composable("assessments") {
            AssessmentsScreen()
        }
        composable("view_survey") {
            SurveyScreen()
        }
    }
}