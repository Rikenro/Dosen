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
import kotlinx.coroutines.launch

@Composable
fun DetailSetoranScreen(navController: NavController, nim: String?) {
    val context = LocalContext.current
    val setoranViewModel: SetoranViewModel = viewModel(factory = SetoranViewModel.getFactory(context))
    val mahasiswaState by setoranViewModel.mahasiswaState.collectAsState()
    val detailSetoranState by setoranViewModel.detailSetoranState.collectAsState()
    val setoranState by setoranViewModel.setoranState.collectAsState()

    var selectedNim by remember { mutableStateOf<String?>(null) }
    var isSurahView by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val TAG = "DetailSetoranScreen"

    LaunchedEffect(Unit) {
        Log.d(TAG, "Memulai pengambilan daftar mahasiswa")
        setoranViewModel.fetchMahasiswaList()
    }

    LaunchedEffect(selectedNim) {
        selectedNim?.let {
            Log.d(TAG, "Memilih mahasiswa dengan NIM: $it")
            isSurahView = true
            setoranViewModel.fetchDetailSetoran(it)
        } ?: run {
            Log.d(TAG, "selectedNim direset ke null")
            isSurahView = false
        }
    }

    // Handle setoran state changes
    LaunchedEffect(setoranState) {
        when (val state = setoranState) {
            is SetoranState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Operasi berhasil")
                    setoranViewModel.resetState()
                }
            }
            is SetoranState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${state.message}")
                    setoranViewModel.resetState()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isSurahView) {
                            Log.d(TAG, "Kembali ke daftar mahasiswa")
                            selectedNim = null
                        } else {
                            Log.d(TAG, "Navigasi kembali ke layar sebelumnya")
                            navController.navigateUp()
                        }
                    }) {
                        Text("<")
                    }
                }
                Text(
                    text = if (isSurahView) "Hafalan Surah Mahasiswa" else "Daftar Mahasiswa",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isSurahView && selectedNim != null -> {
                        when (val state = detailSetoranState) {
                            is DetailSetoranState.Success -> {
                                val data = state.data.data
                                Log.d(TAG, "Menampilkan detail setoran untuk NIM: ${selectedNim}")
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // Informasi Mahasiswa
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Nama: ${data.info.nama}", style = MaterialTheme.typography.bodyLarge)
                                            Text("NIM: ${data.info.nim}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Email: ${data.info.email}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Angkatan: ${data.info.angkatan}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Semester: ${data.info.semester}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Dosen PA: ${data.info.dosen_pa.nama}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }

                                    // Daftar Surah
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Daftar Hafalan Surah", style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            if (data.setoran.detail.isEmpty()) {
                                                Text(
                                                    text = "Tidak ada hafalan surah.",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            } else {
                                                LazyColumn {
                                                    items(data.setoran.detail) { item ->
                                                        SurahItem(
                                                            item = item,
                                                            nim = selectedNim!!,
                                                            setoranViewModel = setoranViewModel,
                                                            isLoading = setoranState is SetoranState.Loading
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            is DetailSetoranState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            is DetailSetoranState.Error -> {
                                Text(
                                    text = "Error: ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            is DetailSetoranState.Idle -> {
                                Log.d(TAG, "DetailSetoranState dalam keadaan Idle")
                            }
                        }
                    }
                    else -> {
                        when (val state = mahasiswaState) {
                            is MahasiswaState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            is MahasiswaState.Success -> {
                                val mahasiswaList = state.data
                                if (mahasiswaList.isEmpty()) {
                                    Text(
                                        text = "Tidak ada mahasiswa yang tersedia.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    LazyColumn {
                                        items(mahasiswaList) { mahasiswa ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        Log.d(TAG, "Kartu mahasiswa diklik: ${mahasiswa.nim}")
                                                        selectedNim = mahasiswa.nim
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Nama: ${mahasiswa.nama}",
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Text(
                                                        text = "NIM: ${mahasiswa.nim}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = "Angkatan: ${mahasiswa.angkatan}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Progres Hafalan: ${mahasiswa.info_setoran.persentase_progres_setor}%",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    LinearProgressIndicator(
                                                        progress = { mahasiswa.info_setoran.persentase_progres_setor / 100f },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(8.dp),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    mahasiswa.info_setoran.tgl_terakhir_setor?.let {
                                                        Text(
                                                            text = "Terakhir Setor: $it",
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            is MahasiswaState.Error -> {
                                Text(
                                    text = "Error: ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Button(
                                    onClick = { setoranViewModel.fetchMahasiswaList() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Coba Lagi")
                                }
                            }
                            is MahasiswaState.Idle -> {
                                Log.d(TAG, "MahasiswaState dalam keadaan Idle")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahItem(
    item: DetailKomponenSetoran,
    nim: String,
    setoranViewModel: SetoranViewModel,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.sudah_setor)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nama,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.sudah_setor)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Label: ${item.label}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Status: ${if (item.sudah_setor) "Sudah Setor" else "Belum Setor"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    item.info_setoran?.let { info ->
                        Text(
                            text = "Tanggal Setoran: ${info.tgl_setoran.take(10)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Tanggal Validasi: ${info.tgl_validasi.take(10)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Dosen: ${info.dosen_yang_mengesahkan.nama}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Action Button
                if (item.sudah_setor) {
                    // Delete button for validated memorization
                    item.info_setoran?.let { info ->
                        Button(
                            onClick = {
                                setoranViewModel.deleteSetoranMahasiswa(
                                    nim = nim,
                                    idSetoran = info.id,
                                    idKomponenSetoran = item.id,
                                    namaKomponenSetoran = item.nama
                                )
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text("Hapus", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                } else {
                    // Add button for unvalidated memorization
                    Button(
                        onClick = {
                            setoranViewModel.postSetoranMahasiswa(
                                nim = nim,
                                idKomponenSetoran = item.id,
                                namaKomponenSetoran = item.nama
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Tambah", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}