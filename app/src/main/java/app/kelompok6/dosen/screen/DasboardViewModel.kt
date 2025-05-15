package app.kelompok6.dosen.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.kelompok6.dosen.TokenManager
import com.auth0.jwt.JWT
import app.kelompok6.dosen.PAResponse
import app.kelompok6.dosen.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DashboardViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val TAG = "DashboardViewModel"

    init {
        val idToken = tokenManager.getIdToken()
        if (idToken != null) {
            try {
                val decodedJwt = JWT.decode(idToken)
                Log.d(TAG, "JWT claims: ${decodedJwt.claims}")
                val name = decodedJwt.getClaim("name").asString()
                    ?: decodedJwt.getClaim("preferred_username").asString()
                    ?: "Unknown User"
                _userName.value = name
                Log.d(TAG, "Nama pengguna dari id_token: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menguraikan id_token: ${e.message}")
                _userName.value = null
            }
        } else {
            Log.e(TAG, "id_token tidak ditemukan")
            _userName.value = null
        }
    }

    fun fetchSetoranSaya() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    // Coba tanpa apikey terlebih dahulu (karena lebih sederhana)
                    Log.d(TAG, "Mengambil data setoran tanpa apikey, token: $token")
                    try {
                        val response = RetrofitClient.apiService.getPaSaya(
                            token = "Bearer $token"
                        )

                        if (response.isSuccessful) {
                            response.body()?.let { setoran ->
                                Log.d(TAG, "Data setoran berhasil diambil: ${setoran.message}")
                                _dashboardState.value = DashboardState.Success(setoran)
                            } ?: run {
                                Log.e(TAG, "Respons kosong dari server")
                                _dashboardState.value = DashboardState.Error("Respons kosong dari server")
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Tidak ada detail error"
                            Log.e(TAG, "Gagal mengambil data tanpa apikey, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")

                            // Jika tanpa apikey gagal, coba dengan apikey
                            if (response.code() == 403) {
                                Log.d(TAG, "Mencoba dengan apikey")
                                // Gunakan client_secret sebagai API key
                                val apiKeyResponse = RetrofitClient.apiService.getPaSayaWithApiKey(
                                    token = "Bearer $token",
                                    apiKey = RetrofitClient.getApiKey()
                                )

                                if (apiKeyResponse.isSuccessful) {
                                    apiKeyResponse.body()?.let { setoran ->
                                        Log.d(TAG, "Data setoran berhasil diambil dengan apikey: ${setoran.message}")
                                        _dashboardState.value = DashboardState.Success(setoran)
                                    } ?: run {
                                        Log.e(TAG, "Respons kosong dari server dengan apikey")
                                        _dashboardState.value = DashboardState.Error("Respons kosong dari server dengan apikey")
                                    }
                                } else {
                                    val apiKeyErrorBody = apiKeyResponse.errorBody()?.string() ?: "Tidak ada detail error"
                                    Log.e(TAG, "Gagal dengan apikey, kode: ${apiKeyResponse.code()}, pesan: ${apiKeyResponse.message()}, body: $apiKeyErrorBody")
                                    handleErrorResponse(apiKeyResponse.code(), apiKeyErrorBody, apiKeyResponse.message())
                                }
                            } else {
                                handleErrorResponse(response.code(), errorBody, response.message())
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Kesalahan saat memanggil API: ${e.message}", e)
                        _dashboardState.value = DashboardState.Error("Kesalahan saat memanggil API: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _dashboardState.value = DashboardState.Error("Token tidak ditemukan. Silakan login ulang.")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP: ${e.code()}, pesan: ${e.message()}")
                _dashboardState.value = DashboardState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: IOException) {
                Log.e(TAG, "Pengecualian jaringan: ${e.message}")
                _dashboardState.value = DashboardState.Error("Kesalahan jaringan: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian: ${e.message}", e)
                _dashboardState.value = DashboardState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String, message: String) {
        when (code) {
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
                                    // Coba ulang pengambilan data setelah refresh token
                                    fetchSetoranSaya()
                                } ?: run {
                                    Log.e(TAG, "Respons refresh kosong")
                                    _dashboardState.value = DashboardState.Error("Gagal memperbarui token: Respons kosong")
                                }
                            } else {
                                val refreshErrorBody = refreshResponse.errorBody()?.string() ?: "Tidak ada detail error"
                                Log.e(TAG, "Gagal refresh token, kode: ${refreshResponse.code()}, pesan: ${refreshResponse.message()}, body: $refreshErrorBody")
                                _dashboardState.value = DashboardState.Error("Gagal memperbarui token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Pengecualian saat refresh token: ${e.message}")
                            _dashboardState.value = DashboardState.Error("Gagal memperbarui token: ${e.message}")
                        }
                    } else {
                        Log.e(TAG, "Refresh token tidak ditemukan")
                        _dashboardState.value = DashboardState.Error("Refresh token tidak ditemukan. Silakan login ulang.")
                    }
                }
            }
            403 -> {
                Log.e(TAG, "Akses ditolak: $errorBody")
                _dashboardState.value = DashboardState.Error("Akses ditolak: Tidak diotorisasi (Kode: 403). Periksa role pengguna atau konfigurasi server.")
            }
            404 -> {
                Log.e(TAG, "Endpoint tidak ditemukan: $message")
                _dashboardState.value = DashboardState.Error("Endpoint tidak ditemukan (Kode: 404). Periksa URL atau konfigurasi server.")
            }
            else -> {
                _dashboardState.value = DashboardState.Error("Gagal mengambil data: $message (Kode: $code, Body: $errorBody)")
            }
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(TokenManager(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val data: PAResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}