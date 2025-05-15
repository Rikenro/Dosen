package app.kelompok6.dosen

data class PAResponse(
    val response: Boolean,
    val message: String,
    val data: DosenData
)

data class DosenData(
    val nip: String,
    val nama: String,
    val email: String,
    val info_mahasiswa_pa: InfoMahasiswaPA
)

data class InfoMahasiswaPA(
    val ringkasan: List<RingkasanMahasiswa>,
    val daftar_mahasiswa: List<Mahasiswa>
)

data class RingkasanMahasiswa(
    val tahun: String,
    val total: Int
)

data class Mahasiswa(
    val email: String,
    val nim: String,
    val nama: String,
    val angkatan: String,
    val semester: Int,
    val info_setoran: InfoSetoran
)

data class InfoSetoran(
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Double,
    val tgl_terakhir_setor: String?, // null jika belum ada
    val terakhir_setor: String
)