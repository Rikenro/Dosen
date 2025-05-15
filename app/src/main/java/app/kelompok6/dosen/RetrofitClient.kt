package app.kelompok6.dosen

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Perbaikan URL BASE untuk konsistensi dengan endpoint
    private const val BASE_URL = "https://api.tif.uin-suska.ac.id/setoran-dev/v1/"
    private const val KC_URL = "https://id.tif.uin-suska.ac.id"
    // Tambahkan API key statis jika diperlukan
    private const val API_KEY = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Tambahkan timeout yang lebih lama untuk mengatasi masalah jaringan
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val kcApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(KC_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Getter untuk API key jika diperlukan
    fun getApiKey(): String = API_KEY
}