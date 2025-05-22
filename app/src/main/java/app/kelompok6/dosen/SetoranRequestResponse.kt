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

data class SetoranResponse(
    val response: Boolean,
    val message: String
)

data class DeleteSetoranRequest(
    val data_setoran: List<DeleteSetoranItem>
)

data class DeleteSetoranItem(
    val id: String? = null,
    val id_komponen_setoran: String,
    val nama_komponen_setoran: String
)
data class DeleteSetoranResponse(
    val response: Boolean,
    val message: String
)
