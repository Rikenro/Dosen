# 🎓 SetoranApp – Dosen

**SetoranApp** adalah aplikasi Android berbasis **Jetpack Compose** yang dirancang khusus untuk dosen guna memantau dan mengelola data setoran hafalan Al-Qur’an mahasiswa dengan efisien dan terorganisir.

---

## 🎯 Tujuan Aplikasi

Menyediakan solusi digital yang ringan dan praktis bagi dosen untuk:

- Melihat daftar setoran mahasiswa
- Menambahkan data setoran baru
- Menghapus data setoran yang salah atau tidak valid

---

## ✨ Fitur Utama

- 🔐 **Login Dosen**  
  Sistem autentikasi dosen menggunakan token login berbasis JWT.

- ➕ **Input Setoran**  
  Tambahkan data setoran mahasiswa dengan informasi nama, juz/ayat, dan tanggal.

- 🗑️ **Hapus Setoran**  
  Fitur hapus data setoran untuk koreksi kesalahan input.

- 📱 **Antarmuka Modern**  
  UI responsif dan sederhana menggunakan Jetpack Compose.

---

## 🔧 Teknologi yang Digunakan

| Teknologi         | Detail                                   |
|-------------------|------------------------------------------|
| Bahasa            | Kotlin                                   |
| UI Framework      | Jetpack Compose                          |
| Backend API       | RESTful API dengan autentikasi JWT       |
| HTTP Client       | Retrofit                                 |
| Dependency Inject | Hilt (jika digunakan)                    |
| Build Tool        | Gradle                                   |
| Target SDK        | Android 15 (API 35)                      |
| Minimum SDK       | Android 7 (API 24)                       |
| Java Version      | Java 11                                  |

---

## ⚙️ Prasyarat

Sebelum menjalankan proyek ini, pastikan Anda telah memiliki:

- **Android Studio** versi terbaru (dianjurkan Iguana)
- **JDK 11**
- **Kotlin Plugin** terbaru
- **Koneksi Internet** (untuk komunikasi API)
- Perangkat fisik/emulator dengan Versi Android minimum 7

---

## 🚀 Cara Menjalankan Proyek

1. **Clone Repository**
   ```bash
   git clone https://github.com/Rikenro/Dosen.git
   cd Dosen
Buka di Android Studio

Pilih menu File > Open

Arahkan ke folder Dosen

Sinkronisasi Gradle

Klik Sync Project with Gradle Files

Jalankan Aplikasi

Hubungkan perangkat atau buka emulator

Klik tombol Run ▶️

🧭 Panduan Penggunaan
Login sebagai Dosen

Masukkan akun dosen yang terdaftar

Lihat Daftar Setoran

Daftar nama mahasiswa dan setoran akan ditampilkan

Tambah Setoran

Klik tombol tambah pada surah

Hapus Setoran

Klik tombol hapus pada surah yang sudah di validasi

🤝 Kontribusi
Kontribusi sangat kami apresiasi! Berikut cara untuk ikut serta:

Fork Repository

Klik Fork di GitHub

Buat Branch Baru

bash
Copy
Edit
git checkout -b feature/nama-fitur
Lakukan Perubahan

Tambahkan fitur baru atau perbaikan bug

Commit dan Push

bash
Copy
Edit
git commit -m "Menambahkan fitur nama-fitur"
git push origin feature/nama-fitur
Buat Pull Request

Ajukan ke branch utama dengan deskripsi yang jelas

👨‍🏫 Kontak Developer
Email	12350112981@students.uin-suska.ac.id
GitHub	@Rikenro

Terima kasih telah menggunakan SetoranApp!
Silakan hubungi saya jika Anda ingin berdiskusi, memberi masukan, atau ikut membangun aplikasi ini 🙏
