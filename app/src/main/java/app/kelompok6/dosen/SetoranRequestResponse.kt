package app.kelompok6.dosen

data class SetoranRequest(
    val data_setoran: List<SetoranItem>
)

data class SetoranItem(
    val id_komponen_setoran: String,
    val nama_komponen_setoran: String,
    val catatan: String? = null,
    val nilai: Int? = null
)

// Response class for setoran submission
data class SetoranResponse(
    val response: Boolean,
    val message: String
)

data class KomponenSetoran(
    val id_komponen_setoran: String,
    val nama_komponen_setoran: String
)
