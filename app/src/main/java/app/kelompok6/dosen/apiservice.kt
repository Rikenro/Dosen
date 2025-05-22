package app.kelompok6.dosen

import retrofit2.Response
import retrofit2.http.*

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

    @GET("dosen/pa-saya")
    suspend fun getPaSaya(
        @Header("Authorization") token: String
    ): Response<PAResponse>

    @GET("dosen/pa-saya")
    suspend fun getPaSayaWithApiKey(
        @Header("Authorization") token: String,
        @Query("apikey") apiKey: String
    ): Response<PAResponse>

    @POST("mahasiswa/setoran/{nim}")
    suspend fun simpanSetoran(
        @Header("Authorization") token: String,
        @Path("nim") nim: String,
        @Body request: SetoranRequest
    ): Response<SetoranResponse>

    @GET("mahasiswa/setoran/{nim}")
    suspend fun getDetailSetoran(
        @Header("Authorization") token: String,
        @Path("nim") nim: String
    ): Response<DetailSetoranResponse>

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "mahasiswa/setoran/{nim}", hasBody = true)
    suspend fun deleteSetoran(
        @Header("Authorization") token: String,
        @Path("nim") nim: String,
        @Query("id") id: String,
        @Body request: DeleteSetoranRequest
    ): Response<SetoranResponse>
}