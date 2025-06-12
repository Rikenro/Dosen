package navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.kelompok6.dosen.screen.DashboardScreen
import app.kelompok6.dosen.screen.DetailSetoranScreen
import app.kelompok6.dosen.screen.LoginScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val navController = rememberNavController()
    Scaffold(
        navController = navController
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("dashboard") {
                DashboardScreen(navController = navController)
            }
            composable("surah") {
                DetailSetoranScreen(navController = navController, nim = null)
            }
            composable("surah/{nim}") { backStackEntry ->
                val nim = backStackEntry.arguments?.getString("nim")
                DetailSetoranScreen(navController = navController, nim = nim)
            }
        }
    }
}

@Composable
fun Scaffold(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "dashboard" || currentRoute == "surah" || currentRoute?.startsWith("surah/") == true) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "surah" || currentRoute?.startsWith("surah/") == true,
                        onClick = {
                            navController.navigate("surah") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = "Surah") },
                        label = { Text("Surah") }
                    )
                }
            }
        },
        content = content
    )
}