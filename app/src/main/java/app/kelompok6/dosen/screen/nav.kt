package app.kelompok6.dosen.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("setoran") {
            SetoranScreen(navController = navController)
        }
        composable(
            route = "setoran/{nim}",
            arguments = listOf(
                navArgument("nim") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim")
            SetoranScreen(navController = navController, nimParam = nim)
        }
        composable(
            route = "detail_setoran/{nim}",
            arguments = listOf(
                navArgument("nim") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim")
            DetailSetoranScreen(navController = navController, nim = nim)
        }
    }
}