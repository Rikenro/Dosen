package app.kelompok6.dosen

data class DetailSetoranResponse(
    val response: Boolean,
    val message: String,
    val data: DetailSetoranData
)

data class DetailSetoranData(
    val info: MahasiswaInfo,
    val setoran: SetoranDetails
)

data class MahasiswaInfo(
    val nama: String,
    val nim: String,
    val email: String,
    val angkatan: String,
    val semester: Int,
    val dosen_pa: DosenPA
)

data class DosenPA(
    val nip: String,
    val nama: String,
    val email: String
)

data class SetoranDetails(
    val log: List<SetoranLog>,
    val info_dasar: InfoDasar,
    val ringkasan: List<RingkasanSetoran>,
    val detail: List<DetailKomponenSetoran>
)

data class SetoranLog(
    val id: Int,
    val keterangan: String,
    val aksi: String,
    val ip: String,
    val user_agent: String,
    val timestamp: String,
    val nim: String,
    val dosen_yang_mengesahkan: DosenPA
)

data class InfoDasar(
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Float,
    val tgl_terakhir_setor: String?,
    val terakhir_setor: String
)

data class RingkasanSetoran(
    val label: String,
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Float
)

data class DetailKomponenSetoran(
    val id: String?, // ID komponen setoran
    val id_setoran: String?, // ID operasi setoran
    val id_komponen_setoran: String,
    val nama: String,
    val nama_arab: String,
    val label: String,
    val sudah_setor: Boolean,
    val info_setoran: InfoSetoranDetail?
)

data class InfoSetoranDetail(
    val id: String?, // Sama dengan id_setoran
    val tgl_setoran: String,
    val tgl_validasi: String,
    val dosen_yang_mengesahkan: DosenPA
)