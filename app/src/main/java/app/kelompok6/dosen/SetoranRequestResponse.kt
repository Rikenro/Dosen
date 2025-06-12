package app.kelompok6.dosen

import com.google.gson.annotations.SerializedName

data class SetoranRequest(
    @SerializedName("data_setoran")
    val dataSetoran: List<SetoranItem>,
    @SerializedName("tgl_setoran")
    val tglSetoran: String? = null
)

data class SetoranItem(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("id_setoran")
    val idSetoran: String? = null, // Untuk DELETE
    @SerializedName("id_komponen_setoran")
    val idKomponenSetoran: String,
    @SerializedName("nama_komponen_setoran")
    val namaKomponenSetoran: String,
    @SerializedName("catatan")
    val catatan: String? = null,
    @SerializedName("nilai")
    val nilai: Int? = null
)

data class SetoranResponse(
    @SerializedName("response")
    val response: Boolean,
    @SerializedName("message")
    val message: String
)

data class DeleteSetoranRequest(
    @SerializedName("data_setoran")
    val dataSetoran: List<DeleteSetoranItem>
)

data class DeleteSetoranItem(
    @SerializedName("id_setoran")
    val idSetoran: String,
    @SerializedName("id_komponen_setoran")
    val idKomponenSetoran: String,
    @SerializedName("nama_komponen_setoran")
    val namaKomponenSetoran: String
)