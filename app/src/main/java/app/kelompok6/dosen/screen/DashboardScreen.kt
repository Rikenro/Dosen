package app.kelompok6.dosen.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userName by dashboardViewModel.userName.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Setoran") },
                actions = {
                    TextButton(onClick = {
                        loginViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                userName?.let {
                    Text(
                        text = "Selamat datang, $it!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                when (val state = dashboardState) {
                    is DashboardState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                        )
                    }
                    is DashboardState.Success -> {
                        val data = state.data.data
                        Text("NIP: ${data.nip}", style = MaterialTheme.typography.bodyLarge)
                        Text("Email: ${data.email}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ringkasan Mahasiswa:", style = MaterialTheme.typography.titleMedium)
                        LazyColumn {
                            items(data.info_mahasiswa_pa.ringkasan) { ringkasan ->
                                Text("${ringkasan.tahun}: ${ringkasan.total} mahasiswa")
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Daftar Mahasiswa:", style = MaterialTheme.typography.titleMedium)
                            }
                            items(data.info_mahasiswa_pa.daftar_mahasiswa) { mahasiswa ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Nama: ${mahasiswa.nama}", style = MaterialTheme.typography.bodyMedium)
                                        Text("NIM: ${mahasiswa.nim}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Angkatan: ${mahasiswa.angkatan}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Semester: ${mahasiswa.semester}", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Progres Setoran: ${mahasiswa.info_setoran.persentase_progres_setor}%",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        mahasiswa.info_setoran.tgl_terakhir_setor?.let {
                                            Text("Terakhir Setor: $it", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is DashboardState.Error -> {
                        LaunchedEffect(state) {
                            scope.launch {
                                snackbarHostState.showSnackbar(state.message)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    )
}