package app.kelompok6.dosen

import retrofit2.Response
import retrofit2.http.*
import app.kelompok6.dosen.AuthResponse
import app.kelompok6.dosen.PAResponse

interface ApiService {

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Response<AuthResponse>

    // Metode ini diperbaiki untuk konsistensi path dengan RetrofitClient BASE_URL
    @GET("dosen/pa-saya")
    suspend fun getPaSaya(
        @Header("Authorization") token: String
    ): Response<PAResponse>

    // Metode alternatif jika diperlukan dengan apikey
    @GET("dosen/pa-saya")
    suspend fun getPaSayaWithApiKey(
        @Header("Authorization") token: String,
        @Query("apikey") apiKey: String
    ): Response<PAResponse>
}