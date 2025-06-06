package app.kelompok6.dosen.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.kelompok6.dosen.DetailKomponenSetoran
import app.kelompok6.dosen.SetoranItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetoranScreen(navController: NavController, nimParam: String? = null) {
    val context = LocalContext.current
    val setoranViewModel: SetoranViewModel = viewModel(factory = SetoranViewModel.getFactory(context))
    val setoranState by setoranViewModel.setoranState.collectAsState()
    val detailSetoranState by setoranViewModel.detailSetoranState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nim by remember { mutableStateOf(nimParam ?: "") }
    var selectedKomponenList by remember { mutableStateOf<List<DetailKomponenSetoran>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // Pastikan nimParam tidak null sebelum memanggil API
    LaunchedEffect(nimParam) {
        if (nimParam != null && nimParam.matches(Regex("\\d{11}"))) {
            setoranViewModel.fetchDetailSetoran(nimParam)
        }
    }

    // Handle success or error states untuk setoran
    LaunchedEffect(setoranState) {
        when (val state = setoranState) {
            is SetoranState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.data.message)
                    // Reset form fields setelah sukses
                    if (nimParam == null) nim = ""
                    selectedKomponenList = emptyList()
                    setoranViewModel.resetState()
                    navController.popBackStack()
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
                title = { Text("Validasi Setoran") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("<")
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // NIM field
                if (nimParam == null) {
                    OutlinedTextField(
                        value = nim,
                        onValueChange = { nim = it },
                        label = { Text("NIM Mahasiswa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("NIM: $nimParam", style = MaterialTheme.typography.bodyLarge)
                }

                // Dropdown untuk memilih komponen setoran
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedKomponenList.joinToString(", ") { it.nama },
                        onValueChange = { },
                        label = { Text("Komponen Setoran") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Text("▼")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        when (val state = detailSetoranState) {
                            is DetailSetoranState.Success -> {
                                state.data.data.setoran.detail
                                    .filter { !it.sudah_setor } // Hanya tampilkan yang belum disetor
                                    .forEach { komponen ->
                                        DropdownMenuItem(
                                            text = { Text(komponen.nama) },
                                            onClick = {
                                                if (komponen !in selectedKomponenList) {
                                                    selectedKomponenList = selectedKomponenList + komponen
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                            }
                            is DetailSetoranState.Loading -> {
                                DropdownMenuItem(
                                    text = { Text("Loading...") },
                                    onClick = { }
                                )
                            }
                            is DetailSetoranState.Error -> {
                                DropdownMenuItem(
                                    text = { Text("Error: ${state.message}") },
                                    onClick = { }
                                )
                            }
                            else -> {}
                        }
                    }
                }

                // List komponen yang dipilih
                if (selectedKomponenList.isNotEmpty()) {
                    Text("Komponen yang dipilih:", style = MaterialTheme.typography.titleMedium)
                    selectedKomponenList.forEach { komponen ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(komponen.nama)
                            IconButton(onClick = {
                                selectedKomponenList = selectedKomponenList - komponen
                            }) {
                                Text("X")
                            }
                        }
                    }
                }

                // Submit button
                Button(
                    onClick = {
                        val nimToUse = nimParam ?: nim.trim()
                        if (nimToUse.isNotBlank() && nimToUse.matches(Regex("\\d{11}")) && selectedKomponenList.isNotEmpty()) {
                            val setoranItems = selectedKomponenList.map { komponen ->
                                SetoranItem(
                                    id_komponen_setoran = komponen.id,
                                    nama_komponen_setoran = komponen.nama
                                )
                            }
                            setoranViewModel.submitSetoran(nimToUse, setoranItems)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("NIM harus 11 digit angka dan pilih setidaknya satu komponen setoran")
                            }
                        }
                    },
                    enabled = (nimParam != null || nim.isNotBlank()) &&
                            selectedKomponenList.isNotEmpty() &&
                            setoranState !is SetoranState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (setoranState is SetoranState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Submit Validasi")
                    }
                }

                // State untuk detail setoran
                when (val state = detailSetoranState) {
                    is DetailSetoranState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            "Memuat komponen setoran...",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is DetailSetoranState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = { nimParam?.let { setoranViewModel.fetchDetailSetoran(it) } },
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