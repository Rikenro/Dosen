package app.kelompok6.dosen.screen

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.kelompok6.dosen.DetailKomponenSetoran
import app.kelompok6.dosen.Mahasiswa
import kotlinx.coroutines.launch

@Composable
fun DetailSetoranScreen(navController: NavController, nim: String?) {
    val context = LocalContext.current
    val setoranViewModel: SetoranViewModel = viewModel(factory = SetoranViewModel.getFactory(context))
    val mahasiswaState by setoranViewModel.mahasiswaState.collectAsState()
    val detailSetoranState by setoranViewModel.detailSetoranState.collectAsState()
    val setoranState by setoranViewModel.setoranState.collectAsState()

    var selectedNim by remember { mutableStateOf<String?>(null) }
    var selectedAngkatan by remember { mutableStateOf<String?>(null) }
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
                                    // Group students by angkatan and sort descending (newest first)
                                    val groupedMahasiswa = mahasiswaList
                                        .groupBy { it.angkatan }
                                        .toSortedMap(reverseOrder())

                                    LazyColumn {
                                        groupedMahasiswa.forEach { (angkatan, mahasiswaInAngkatan) ->
                                            // Header for each angkatan (clickable card)
                                            item {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .clickable {
                                                            selectedAngkatan = if (selectedAngkatan == angkatan) null else angkatan
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Angkatan $angkatan",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Text(
                                                            text = "${mahasiswaInAngkatan.size} mahasiswa",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }

                                            // Show students only if this angkatan is selected
                                            if (selectedAngkatan == angkatan) {
                                                items(mahasiswaInAngkatan.sortedBy { it.nama }) { mahasiswa ->
                                                    MahasiswaCardWithChart(
                                                        mahasiswa = mahasiswa,
                                                        onCardClick = {
                                                            Log.d(TAG, "Kartu mahasiswa diklik: ${mahasiswa.nim}")
                                                            selectedNim = mahasiswa.nim
                                                        }
                                                    )
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
fun MahasiswaCardWithChart(
    mahasiswa: Mahasiswa,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Student info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mahasiswa.nama,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "NIM: ${mahasiswa.nim}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Angkatan: ${mahasiswa.angkatan}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Semester: ${mahasiswa.semester}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Progres Setoran: ${mahasiswa.info_setoran.persentase_progres_setor}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                mahasiswa.info_setoran.tgl_terakhir_setor?.let {
                    Text(
                        text = "Terakhir Setor: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right side - Circular Progress Chart
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressChart(
                    progress = mahasiswa.info_setoran.persentase_progres_setor,
                    size = 80.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${mahasiswa.info_setoran.persentase_progres_setor}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        mahasiswa.info_setoran.persentase_progres_setor >= 80 ->
                            MaterialTheme.colorScheme.primary
                        mahasiswa.info_setoran.persentase_progres_setor >= 50 ->
                            MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
fun CircularProgressChart(
    progress: Float,
    size: Dp,
    strokeWidth: Dp = 8.dp
) {
    val progressColor = when {
        progress >= 80 -> MaterialTheme.colorScheme.primary
        progress >= 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.toPx()
            val radius = (canvasSize - strokeWidth.toPx()) / 2
            val center = Offset(canvasSize / 2, canvasSize / 2)

            // Draw background circle
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Draw progress arc
            val sweepAngle = (progress / 100f) * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f, // Start from top
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center text with progress percentage
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = progressColor
        )
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