package app.kelompok6.dosen.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout"
                    )
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
                    Text("Nama: ${data.nama}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Jumlah Mahasiswa PA: ${data.info_mahasiswa_pa.daftar_mahasiswa.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("NIP: ${data.nip}", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${data.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Angkatan Mahasiswa:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Calculate average progress for each angkatan
                    val angkatanWithProgress = data.info_mahasiswa_pa.ringkasan.map { ringkasan ->
                        val studentsInAngkatan = data.info_mahasiswa_pa.daftar_mahasiswa
                            .filter { it.angkatan == ringkasan.tahun }
                        val averageProgress = if (studentsInAngkatan.isNotEmpty()) {
                            studentsInAngkatan.map { it.info_setoran.persentase_progres_setor }.average().toInt()
                        } else {
                            0
                        }
                        Triple(ringkasan, averageProgress, ringkasan.total)
                    }

                    // Grid layout for angkatan cards
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(angkatanWithProgress) { (ringkasan, averageProgress, studentCount) ->
                            AngkatanCard(
                                angkatan = ringkasan.tahun,
                                progress = averageProgress,
                                studentCount = studentCount,
                                isSelected = selectedAngkatan == ringkasan.tahun,
                                onClick = {
                                    selectedAngkatan = if (selectedAngkatan == ringkasan.tahun) null else ringkasan.tahun
                                }
                            )
                        }
                    }

                    // Show detailed student list when an angkatan is selected
                    selectedAngkatan?.let { selectedYear ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Detail Mahasiswa Angkatan $selectedYear:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val studentsInAngkatan = data.info_mahasiswa_pa.daftar_mahasiswa
                            .filter { it.angkatan == selectedYear }

                        LazyColumn {
                            items(studentsInAngkatan) { mahasiswa ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left side - Student Information
                                        Column(
                                            modifier = Modifier
                                                .weight(1f) // Takes available space, pushing the chart to the right
                                        ) {
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

                                        // Right side - Circular Progress Chart
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 16.dp), // Space between student info and chart
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressChart(
                                                progress = mahasiswa.info_setoran.persentase_progres_setor,
                                                size = 80.dp
                                            )
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

@Composable
fun AngkatanCard(
    angkatan: String,
    progress: Int,
    studentCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f) // Make it slightly rectangular (a bit taller than square)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Progress Chart
            CircularProgressChart(
                progress = progress.toFloat(),
                size = 60.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Angkatan Text
            Text(
                text = "Angkatan $angkatan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            // Student Count
            Text(
                text = "$studentCount mahasiswa",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}