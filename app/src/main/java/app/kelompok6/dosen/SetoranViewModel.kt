package app.kelompok6.dosen.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.kelompok6.dosen.ApiService
import app.kelompok6.dosen.DetailSetoranResponse
import app.kelompok6.dosen.SetoranRequest
import app.kelompok6.dosen.SetoranResponse
import app.kelompok6.dosen.SetoranItem
import app.kelompok6.dosen.Mahasiswa
import app.kelompok6.dosen.RetrofitClient
import app.kelompok6.dosen.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SetoranViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _setoranState = MutableStateFlow<SetoranState>(SetoranState.Idle)
    val setoranState: StateFlow<SetoranState> = _setoranState.asStateFlow()
    private val _detailSetoranState = MutableStateFlow<DetailSetoranState>(DetailSetoranState.Idle)
    val detailSetoranState: StateFlow<DetailSetoranState> = _detailSetoranState.asStateFlow()
    private val _mahasiswaState = MutableStateFlow<MahasiswaState>(MahasiswaState.Idle)
    val mahasiswaState: StateFlow<MahasiswaState> = _mahasiswaState.asStateFlow()
    private val TAG = "SetoranViewModel"

    private val apiService: ApiService = RetrofitClient.apiService

    fun fetchMahasiswaList() {
        viewModelScope.launch {
            _mahasiswaState.value = MahasiswaState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Mengambil daftar mahasiswa, token: $token")
                    val response = apiService.getPaSaya(
                        token = "Bearer $token"
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { paResponse ->
                            Log.d(TAG, "Daftar mahasiswa berhasil diambil: ${paResponse.message}")
                            _mahasiswaState.value = MahasiswaState.Success(paResponse.data.info_mahasiswa_pa.daftar_mahasiswa)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _mahasiswaState.value = MahasiswaState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal mengambil daftar mahasiswa, kode: ${response.code()}, body: $errorBody")
                        _mahasiswaState.value = MahasiswaState.Error("Gagal mengambil data: ${response.message()}")
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _mahasiswaState.value = MahasiswaState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (fetch mahasiswa): ${e.code()}, pesan: ${e.message()}")
                _mahasiswaState.value = MahasiswaState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil daftar mahasiswa: ${e.message}", e)
                _mahasiswaState.value = MahasiswaState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun fetchDetailSetoran(nim: String) {
        viewModelScope.launch {
            _detailSetoranState.value = DetailSetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Mengambil detail setoran, NIM: $nim, token: $token")
                    val response = apiService.getDetailSetoran(
                        token = "Bearer $token",
                        nim = nim
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Detail setoran berhasil diambil: ${setoran.message}")
                            _detailSetoranState.value = DetailSetoranState.Success(setoran)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _detailSetoranState.value = DetailSetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal mengambil detail setoran, kode: ${response.code()}, body: $errorBody")
                        _detailSetoranState.value = DetailSetoranState.Error("Gagal mengambil data: ${response.message()}")
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _detailSetoranState.value = DetailSetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (fetch detail): ${e.code()}, pesan: ${e.message()}")
                _detailSetoranState.value = DetailSetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil detail setoran: ${e.message}", e)
                _detailSetoranState.value = DetailSetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun postSetoranMahasiswa(nim: String, idKomponenSetoran: String?, namaKomponenSetoran: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Menambahkan setoran mahasiswa, NIM: $nim, id_komponen_setoran: $idKomponenSetoran, nama_komponen_setoran: $namaKomponenSetoran")
                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                idKomponenSetoran = idKomponenSetoran.toString(),
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        )
                    )
                    val response = apiService.simpanSetoran(
                        token = "Bearer $token",
                        nim = nim,
                        request = request
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Setoran berhasil ditambahkan: ${setoran.message}")
                            _setoranState.value = SetoranState.Success(null)
                            fetchMahasiswaList()
                            fetchDetailSetoran(nim)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal menambahkan setoran, kode: ${response.code()}, body: $errorBody")
                        _setoranState.value = SetoranState.Error("Gagal menambahkan setoran: ${errorBody ?: response.message()}")
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (post setoran): ${e.code()}, pesan: ${e.message()}")
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat menambahkan setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun deleteSetoranMahasiswa(nim: String, idSetoran: String?, idKomponenSetoran: String?, namaKomponenSetoran: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    // Validasi bahwa idSetoran tidak null atau kosong
                    if (idSetoran.isNullOrBlank()) {
                        Log.e(TAG, "ID Setoran kosong atau null")
                        _setoranState.value = SetoranState.Error("ID Setoran tidak valid")
                        return@launch
                    }

                    Log.d(TAG, "Menghapus setoran mahasiswa, NIM: $nim, id_setoran: $idSetoran, id_komponen_setoran: $idKomponenSetoran, nama_komponen_setoran: $namaKomponenSetoran")

                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                id = idSetoran, // Pastikan field id diisi
                                idSetoran = idSetoran,
                                idKomponenSetoran = idKomponenSetoran ?: "",
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        )
                    )

                    val response = apiService.deleteSetoran(
                        token = "Bearer $token",
                        nim = nim,
                        id = idSetoran,
                        request = request
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Setoran berhasil dihapus: ${setoran.message}")
                            _setoranState.value = SetoranState.Success(null)
                            fetchMahasiswaList()
                            fetchDetailSetoran(nim)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal menghapus setoran, kode: ${response.code()}, body: $errorBody")
                        _setoranState.value = SetoranState.Error("Gagal menghapus setoran: ${errorBody ?: response.message()}")
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (delete setoran): ${e.code()}, pesan: ${e.message()}")
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat menghapus setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun resetState() {
        _setoranState.value = SetoranState.Idle
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SetoranViewModel::class.java)) {
                        return SetoranViewModel(TokenManager(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class SetoranState {
    object Idle : SetoranState()
    object Loading : SetoranState()
    data class Success(val data: SetoranResponse?) : SetoranState()
    data class Error(val message: String) : SetoranState()
}

sealed class DetailSetoranState {
    object Idle : DetailSetoranState()
    object Loading : DetailSetoranState()
    data class Success(val data: DetailSetoranResponse) : DetailSetoranState()
    data class Error(val message: String) : DetailSetoranState()
}

sealed class MahasiswaState {
    object Idle : MahasiswaState()
    object Loading : MahasiswaState()
    data class Success(val data: List<Mahasiswa>) : MahasiswaState()
    data class Error(val message: String) : MahasiswaState()
}