package app.kelompok6.dosen.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.kelompok6.dosen.TokenManager
import app.kelompok6.dosen.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    private val TAG = "LoginViewModel"

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                Log.d(TAG, "Memulai login dengan username: $username")
                val response = RetrofitClient.kcApiService.login(
                    clientId = "setoran-mobile-dev",
                    clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                    grantType = "password",
                    username = username,
                    password = password,
                    scope = "openid profile email"
                )
                if (response.isSuccessful) {
                    response.body()?.let { auth ->
                        tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                        Log.d(TAG, "Login berhasil, access_token: ${auth.access_token}")
                        _loginState.value = LoginState.Success
                    } ?: run {
                        Log.e(TAG, "Respons kosong dari server")
                        _loginState.value = LoginState.Error("Respons kosong dari server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Tidak ada detail error"
                    Log.e(TAG, "Login gagal, kode: ${response.code()}, body: $errorBody")
                    _loginState.value = LoginState.Error("Login gagal: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Kesalahan jaringan: ${e.message}")
                _loginState.value = LoginState.Error("Kesalahan jaringan: ${e.message}")
            } catch (e: HttpException) {
                Log.e(TAG, "Kesalahan HTTP: ${e.message}")
                _loginState.value = LoginState.Error("Kesalahan HTTP: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Kesalahan: ${e.message}")
                _loginState.value = LoginState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    fun logout() {
        tokenManager.clearTokens()
        _loginState.value = LoginState.Idle
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                        return LoginViewModel(TokenManager(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}