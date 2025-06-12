package app.kelompok6.dosen.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userName by dashboardViewModel.userName.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedAngkatan by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            userName?.let {
                Text(
                    text = "Selamat datang, $it!",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        loginViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                ) {
                    Text("Logout")
                }
            }
            when (val state = dashboardState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is DashboardState.Success -> {
                    val data = state.data.data
                    Text(
                        text = "Jumlah Mahasiswa PA: ${data.info_mahasiswa_pa.daftar_mahasiswa.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("NIP: ${data.nip}", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${data.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Angkatan Mahasiswa:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn {
                        data.info_mahasiswa_pa.ringkasan.forEach { ringkasan ->
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedAngkatan = if (selectedAngkatan == ringkasan.tahun) null else ringkasan.tahun
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Angkatan ${ringkasan.tahun}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${ringkasan.total} mahasiswa",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            if (selectedAngkatan == ringkasan.tahun) {
                                val studentsInAngkatan = data.info_mahasiswa_pa.daftar_mahasiswa
                                    .filter { it.angkatan == ringkasan.tahun }
                                items(studentsInAngkatan) { mahasiswa ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                            .clickable {
                                                navController.navigate("detail_setoran/${mahasiswa.nim}")
                                            }
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
                    }
                }
                is DashboardState.Error -> {
                    LaunchedEffect(state) {
                        scope.launch {
                            snackbarHostState.showSnackbar(state.message)
                        }
                    }
                }
                is DashboardState.Idle -> {
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}