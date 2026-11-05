package com.example.mysurveyapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mysurveyapp.assessmentCard.AssessmentDetailsScreen
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
            AssessmentsScreen(
                onAssessmentClick = {
                    id -> navController.navigate("assessment/$id")
                }
            )
        }
        composable("view_survey") {
            SurveyScreen()
        }

        composable(
            route = "assessment/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AssessmentDetailsScreen(
                assessmentId = id,
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}