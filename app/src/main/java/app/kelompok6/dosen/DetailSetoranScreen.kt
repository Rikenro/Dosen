package app.kelompok6.dosen.screen

import android.util.Log
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
import app.kelompok6.dosen.DetailKomponenSetoran
import app.kelompok6.dosen.SetoranItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSetoranScreen(navController: NavController, nim: String?) {
    val context = LocalContext.current
    val setoranViewModel: SetoranViewModel = viewModel(factory = SetoranViewModel.getFactory(context))
    val detailSetoranState by setoranViewModel.detailSetoranState.collectAsState()
    val setoranState by setoranViewModel.setoranState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedKomponen by remember { mutableStateOf<List<DetailKomponenSetoran>>(emptyList()) }

    LaunchedEffect(nim) {
        if (nim != null) {
            Log.d("DetailSetoranScreen", "Fetching detail for NIM: $nim")
            setoranViewModel.fetchDetailSetoran(nim)
        }
    }

    // Handle setoran state untuk menampilkan pesan sukses/gagal
    LaunchedEffect(setoranState) {
        when (val state = setoranState) {
            is SetoranState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.data.message)
                    selectedKomponen = emptyList()
                    setoranViewModel.resetState()
                    nim?.let { setoranViewModel.fetchDetailSetoran(it) } // Refresh status surah
                }
            }
            is SetoranState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail Setoran Mahasiswa") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("<")
                    }
                },
                actions = {
                    // Tombol Simpan hanya muncul jika ada surah yang dipilih
                    if (selectedKomponen.isNotEmpty()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    if (snackbarHostState.showSnackbar(
                                            message = "Yakin ingin memvalidasi ${selectedKomponen.size} surah?",
                                            actionLabel = "Ya"
                                        ) == SnackbarResult.ActionPerformed && nim != null
                                    ) {
                                        val setoranItems = selectedKomponen.map { komponen ->
                                            SetoranItem(
                                                id_komponen_setoran = komponen.id,
                                                nama_komponen_setoran = komponen.nama
                                            )
                                        }
                                        setoranViewModel.submitSetoran(nim, setoranItems) // Gunakan nim dari parameter
                                    }
                                }
                            },
                            enabled = setoranState !is SetoranState.Loading,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (setoranState is SetoranState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Simpan")
                            }
                        }
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
                when (val state = detailSetoranState) {
                    is DetailSetoranState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is DetailSetoranState.Success -> {
                        val data = state.data.data
                        Log.d("DetailSetoranScreen", "Data received: ${data.setoran.detail.size} components, " +
                                "Unvalidated: ${data.setoran.detail.count { !it.sudah_setor }}")

                        // Informasi Mahasiswa
                        Text("Nama: ${data.info.nama}", style = MaterialTheme.typography.titleLarge)
                        Text("NIM: ${data.info.nim}", style = MaterialTheme.typography.bodyLarge)
                        Text("Email: ${data.info.email}", style = MaterialTheme.typography.bodyLarge)
                        Text("Angkatan: ${data.info.angkatan}", style = MaterialTheme.typography.bodyLarge)
                        Text("Semester: ${data.info.semester}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Informasi Dosen PA
                        Text("Dosen PA:", style = MaterialTheme.typography.titleMedium)
                        Text("Nama: ${data.info.dosen_pa.nama}", style = MaterialTheme.typography.bodyLarge)
                        Text("NIP: ${data.info.dosen_pa.nip}", style = MaterialTheme.typography.bodyLarge)
                        Text("Email: ${data.info.dosen_pa.email}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Ringkasan Setoran
                        Text("Ringkasan Setoran:", style = MaterialTheme.typography.titleMedium)
                        Text("Total Wajib Setor: ${data.setoran.info_dasar.total_wajib_setor}")
                        Text("Total Sudah Setor: ${data.setoran.info_dasar.total_sudah_setor}")
                        Text("Total Belum Setor: ${data.setoran.info_dasar.total_belum_setor}")
                        Text("Progres: ${data.setoran.info_dasar.persentase_progres_setor}%")
                        data.setoran.info_dasar.tgl_terakhir_setor?.let {
                            Text("Terakhir Setor: $it")
                        } ?: Text("Terakhir Setor: ${data.setoran.info_dasar.terakhir_setor}")
                        Spacer(modifier = Modifier.height(16.dp))

                        // Daftar Surah untuk Validasi
                        Text("Daftar Surah untuk Validasi:", style = MaterialTheme.typography.titleMedium)
                        val surahsToValidate = data.setoran.detail.filter { !it.sudah_setor }
                        if (surahsToValidate.isEmpty()) {
                            Text(
                                "Tidak ada surah yang dapat divalidasi (semua sudah disetor).",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn {
                                items(surahsToValidate) { komponen ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                Log.d("DetailSetoranScreen", "Clicked: ${komponen.nama}, Selected: ${selectedKomponen.contains(komponen)}")
                                                selectedKomponen = if (komponen in selectedKomponen) {
                                                    selectedKomponen - komponen
                                                } else {
                                                    selectedKomponen + komponen
                                                }
                                                Log.d("DetailSetoranScreen", "Selected components: ${selectedKomponen.size}")
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (komponen in selectedKomponen) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Nama: ${komponen.nama}")
                                                Text("Nama Arab: ${komponen.nama_arab}")
                                                Text("Kategori: ${komponen.label}")
                                            }
                                            Text(
                                                text = if (komponen in selectedKomponen) "Dipilih" else "Pilih",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is DetailSetoranState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = { nim?.let { setoranViewModel.fetchDetailSetoran(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                    else -> {}
                }
            }
        }
    )
}