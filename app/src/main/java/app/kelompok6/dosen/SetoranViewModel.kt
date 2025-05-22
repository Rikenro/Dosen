package app.kelompok6.dosen.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.kelompok6.dosen.DeleteSetoranItem
import app.kelompok6.dosen.DeleteSetoranRequest
import app.kelompok6.dosen.DetailSetoranResponse
import app.kelompok6.dosen.RetrofitClient
import app.kelompok6.dosen.SetoranItem
import app.kelompok6.dosen.SetoranRequest
import app.kelompok6.dosen.SetoranResponse
import app.kelompok6.dosen.TokenManager
import com.auth0.jwt.JWT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Date

class SetoranViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _setoranState = MutableStateFlow<SetoranState>(SetoranState.Idle)
    val setoranState: StateFlow<SetoranState> = _setoranState

    private val _detailSetoranState = MutableStateFlow<DetailSetoranState>(DetailSetoranState.Idle)
    val detailSetoranState: StateFlow<DetailSetoranState> = _detailSetoranState

    private val TAG = "SetoranViewModel"

    private fun isTokenValid(token: String?): Boolean {
        if (token == null) return false
        return try {
            val decodedJwt = JWT.decode(token)
            val expiry = decodedJwt.expiresAt
            expiry?.after(Date()) == true
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memvalidasi token: ${e.message}", e)
            false
        }
    }

    fun fetchDetailSetoran(nim: String) {
        viewModelScope.launch {
            if (nim.isBlank()) {
                _detailSetoranState.value = DetailSetoranState.Error("NIM tidak valid.")
                return@launch
            }
            _detailSetoranState.value = DetailSetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null && isTokenValid(token)) {
                    Log.d(TAG, "Mengambil detail setoran untuk NIM: $nim dari URL: ${RetrofitClient.BASE_URL}mahasiswa/setoran/$nim")
                    val response = RetrofitClient.apiService.getDetailSetoran(
                        token = "Bearer $token",
                        nim = nim
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { detailResponse ->
                            if (detailResponse.response) {
                                Log.d(TAG, "Detail setoran berhasil diambil: ${detailResponse.message}")
                                _detailSetoranState.value = DetailSetoranState.Success(detailResponse)
                            } else {
                                Log.e(TAG, "Respons server gagal: ${detailResponse.message}")
                                _detailSetoranState.value = DetailSetoranState.Error(detailResponse.message)
                            }
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _detailSetoranState.value = DetailSetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Tidak ada detail error"
                        Log.e(TAG, "Gagal mengambil detail setoran, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message())
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan atau tidak valid")
                    _detailSetoranState.value = DetailSetoranState.Error("Token tidak ditemukan atau tidak valid. Silakan login ulang.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian: ${e.message}", e)
                _detailSetoranState.value = DetailSetoranState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    fun submitSetoran(nim: String, setoranItems: List<SetoranItem>) {
        viewModelScope.launch {
            if (nim.isBlank() || setoranItems.isEmpty()) {
                _setoranState.value = SetoranState.Error("NIM atau data setoran tidak valid.")
                return@launch
            }
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null && isTokenValid(token)) {
                    Log.d(TAG, "Mengirim validasi setoran untuk NIM: $nim dari URL: ${RetrofitClient.BASE_URL}mahasiswa/setoran/$nim")
                    val request = SetoranRequest(data_setoran = setoranItems)

                    val response = RetrofitClient.apiService.simpanSetoran(
                        token = "Bearer $token",
                        nim = nim,
                        request = request
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { setoranResponse ->
                            if (setoranResponse.response) {
                                Log.d(TAG, "Validasi setoran berhasil: ${setoranResponse.message}")
                                _setoranState.value = SetoranState.Success(setoranResponse)
                                fetchDetailSetoran(nim) // Refresh data setelah validasi
                            } else {
                                Log.e(TAG, "Respons server gagal: ${setoranResponse.message}")
                                _setoranState.value = SetoranState.Error(setoranResponse.message)
                            }
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Tidak ada detail error"
                        Log.e(TAG, "Gagal mengirim validasi setoran, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message())
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan atau tidak valid")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan atau tidak valid. Silakan login ulang.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    fun deleteSetoran(nim: String, idSetoran: String, idKomponenSetoran: String, namaKomponenSetoran: String) {
        viewModelScope.launch {
            if (nim.isBlank() || idSetoran.isBlank() || idKomponenSetoran.isBlank() || namaKomponenSetoran.isBlank()) {
                _setoranState.value = SetoranState.Error("Data tidak lengkap untuk membatalkan setoran.")
                return@launch
            }

            _setoranState.value = SetoranState.Loading

            try {
                val token = tokenManager.getAccessToken()
                if (token != null && isTokenValid(token)) {
                    Log.d(TAG, "Mengirim pembatalan setoran untuk NIM: $nim, ID Setoran: $idSetoran, ID Komponen: $idKomponenSetoran")

                    val request = DeleteSetoranRequest(
                        data_setoran = listOf(
                            DeleteSetoranItem(
                                id = idSetoran,
                                id_komponen_setoran = idKomponenSetoran,
                                nama_komponen_setoran = namaKomponenSetoran
                            )
                        )
                    )

                    Log.d(TAG, "Request body: $request")

                    val response = RetrofitClient.apiService.deleteSetoran(
                        token = "Bearer $token",
                        nim = nim,
                        id = idSetoran,
                        request = request
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { deleteResponse ->
                            if (deleteResponse.response) {
                                Log.d(TAG, "Pembatalan setoran berhasil: ${deleteResponse.message}")
                                _setoranState.value = SetoranState.Success(deleteResponse)
                                Log.d(TAG, "Melakukan refresh data setelah pembatalan")
                                fetchDetailSetoran(nim)
                            } else {
                                Log.e(TAG, "Respons server gagal: ${deleteResponse.message}")
                                _setoranState.value = SetoranState.Error(deleteResponse.message)
                            }
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Tidak ada detail error dari server"
                        Log.e(TAG, "Gagal membatalkan setoran, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        Log.e(TAG, "Response headers: ${response.headers()}")
                        Log.e(TAG, "Raw response: ${response.raw()}")
                        handleErrorResponse(response.code(), errorBody, response.message())
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan atau tidak valid")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan atau tidak valid. Silakan login ulang.")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP Exception saat membatalkan setoran: ${e.message}", e)
                Log.e(TAG, "HTTP Exception code: ${e.code()}")
                Log.e(TAG, "HTTP Exception response: ${e.response()?.errorBody()?.string()}")
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: IOException) {
                Log.e(TAG, "IOException saat membatalkan setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat membatalkan setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String, message: String) {
        val errorMessage = when (code) {
            400 -> {
                Log.e(TAG, "Permintaan tidak valid: $errorBody")
                "Permintaan tidak valid: $errorBody"
            }
            401 -> {
                Log.w(TAG, "Token tidak valid, mencoba refresh token")
                viewModelScope.launch {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken != null) {
                        try {
                            val refreshResponse = RetrofitClient.kcApiService.refreshToken(
                                clientId = "setoran-mobile-dev",
                                clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                                grantType = "refresh_token",
                                refreshToken = refreshToken
                            )
                            if (refreshResponse.isSuccessful) {
                                refreshResponse.body()?.let { auth ->
                                    Log.d(TAG, "Token berhasil diperbarui")
                                    tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                                    fetchDetailSetoran("")
                                } ?: run {
                                    Log.e(TAG, "Respons refresh kosong")
                                    _setoranState.value = SetoranState.Error("Gagal memperbarui token: Respons kosong")
                                    _detailSetoranState.value = DetailSetoranState.Error("Gagal memperbarui token: Respons kosong")
                                }
                            } else {
                                val refreshErrorBody = refreshResponse.errorBody()?.string() ?: "Tidak ada detail error"
                                Log.e(TAG, "Gagal refresh token, kode: ${refreshResponse.code()}, pesan: ${refreshResponse.message()}, body: $refreshErrorBody")
                                _setoranState.value = SetoranState.Error("Gagal memperbarui token: ${refreshResponse.message()}")
                                _detailSetoranState.value = DetailSetoranState.Error("Gagal memperbarui token: ${refreshResponse.message()}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Pengecualian saat refresh token: ${e.message}")
                            _setoranState.value = SetoranState.Error("Gagal memperbarui token: ${e.message}")
                            _detailSetoranState.value = DetailSetoranState.Error("Gagal memperbarui token: ${e.message}")
                        }
                    } else {
                        Log.e(TAG, "Refresh token tidak ditemukan")
                        _setoranState.value = SetoranState.Error("Refresh token tidak ditemukan. Silakan login ulang.")
                        _detailSetoranState.value = DetailSetoranState.Error("Refresh token tidak ditemukan. Silakan login ulang.")
                    }
                }
                "Token tidak valid. Sedang mencoba memperbarui token."
            }
            403 -> {
                Log.e(TAG, "Akses ditolak: $errorBody")
                "Akses ditolak: Tidak diotorisasi (Kode: 403)."
            }
            404 -> {
                Log.e(TAG, "Endpoint tidak ditemukan: $message")
                "Endpoint tidak ditemukan (Kode: 404)."
            }
            else -> {
                Log.e(TAG, "Kesalahan server: $message, Body: $errorBody")
                "Gagal memproses permintaan: $message (Kode: $code, Detail: $errorBody)"
            }
        }
        _setoranState.value = SetoranState.Error(errorMessage)
        _detailSetoranState.value = DetailSetoranState.Error(errorMessage)
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
    data class Success(val data: SetoranResponse) : SetoranState()
    data class Error(val message: String) : SetoranState()
}

sealed class DetailSetoranState {
    object Idle : DetailSetoranState()
    object Loading : DetailSetoranState()
    data class Success(val data: DetailSetoranResponse) : DetailSetoranState()
    data class Error(val message: String) : DetailSetoranState()
}