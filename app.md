# Support Life

Aplikasi mobile Android untuk mencatat, memantau, dan mengelola tabungan serta catatan tanggal secara lokal di perangkat tanpa koneksi internet. Hadir dengan tema **Dark Mode Elegan OLED** (`#09090B` Deep Obsidian, `#18181B` Dark Zinc, aksen `#8B5CF6` Electric Violet & `#10B981` Mint Neon, teks `#F4F4F5` Off-White), ikon vektor custom dua cincin, serta transisi animasi yang ringan.

---

## Dua Mode Tabungan Terpisah

- **Solo (Tabungan Pribadi)**: Penyimpanan transaksi, target tabungan, dan riwayat aktivitas tersendiri untuk tabungan pribadi.
- **Duo (Tabungan Bersama)**: Penyimpanan tersendiri untuk tabungan pasangan, lengkap dengan bilah pembagian kontribusi dan filter per pasangan.

---

## Fitur Utama

- **Ringkasan Saldo & Mode Privasi**: Menampilkan saldo, total setor, total tarik, kontribusi masing-masing pasangan (mode Duo) beserta keterangan `Setor` dan penanda merah untuk nilai minus, serta tombol **Mode Privasi** untuk menyembunyikan nominal.
- **Target Tabungan (`SavingsGoal`)**: Tambah, ubah, dan hapus beberapa target tabungan lengkap dengan bilah progres animasi, persentase, kategori, dan tenggat opsional.
- **Riwayat Transaksi & Filter**: Pencatatan transaksi `Setor` dan `Tarik` dengan tanggal, kategori, dan keterangan, dilengkapi pencarian teks, filter pasangan (mode Duo), tipe, kategori, serta aksi ubah/hapus.
- **Grafik Tren Keuangan**: Kartu terpadu **Tren Keuangan** dengan tab **Akumulasi Saldo** (kurva halus per transaksi kronologis) dan **Setor vs Tarik** (grafik batang mingguan), serta filter rentang waktu (`Semua`, `1 Thn`, `6 Bln`).
- **Ekspor & Impor Offline**: Ekspor laporan ke **PDF** (`PdfDocument`) dan **CSV**, serta impor massal transaksi dari file **CSV** (maksimal 500 baris per impor dengan validasi tanggal dan nominal).
- **Kalender Note**: Tandai tanggal penting, acara, atau pengingat pribadi dengan penanda warna (Acara, Pengingat, Penting) langsung di kalender bulanan, lengkap dengan tab navigasi khusus.
- **Catatan (`PersonalNote`)**: Catatan bebas untuk ide, daftar, atau pengingat pribadi dengan fitur sematkan (*pin*), pencarian, filter label warna, serta ringkasan di Beranda.
- **Keamanan PIN Offline**: Kunci PIN 6 digit dengan hash **SHA-256 + salt**, verifikasi *constant-time* (`MessageDigest.isEqual`), dan jeda kunci **60 detik** setelah **5 percobaan salah**.
- **Pengaturan & Riwayat Aktivitas (`AuditLog`)**: Halaman pengaturan untuk mengubah label pemilik (judul aplikasi terkunci sebagai `Support Life`), mengatur Mode Privasi dan PIN, melihat jumlah data per mode, mengekspor/mengimpor data, melihat log aktivitas, dan mereset data mode aktif.

---

## Tech Stack

- **Bahasa & UI**: Kotlin, Jetpack Compose, Material 3 (`compileSdk 35`, `minSdk 26`)
- **Penyimpanan & Dokumen**: `SharedPreferences` + `org.json`, `android.graphics.pdf.PdfDocument`, Storage Access Framework (`CreateDocument` / `OpenDocument`)
- **Grafik & Ikon**: Custom `Canvas` (`BalanceAreaChart`, `SetorTarikBarChart`, dan ikon vektor domain di `CustomIcons.kt`)
- **Pengujian**: JUnit 4 (`TabunganLogicTest.kt`)

---

## Struktur Proyek Utama

- `app/src/main/java/com/yanto/catatanuang/MainActivity.kt`: Shell aplikasi, navigasi tab, pemilih mode Solo/Duo, launcher SAF ekspor/impor, dan wiring state.
- `app/src/main/java/com/yanto/catatanuang/model/Models.kt`: Model data (`SavingsMode`, `SavingsTransaction`, `SavingsGoal`, `CoupleProfile`, `AuditEntry`, `SummaryStats`).
- `app/src/main/java/com/yanto/catatanuang/data/TabunganStore.kt`: Logika perhitungan saldo, seri grafik, validasi/parsing CSV, keamanan PIN (`PinRateLimiter`), ekspor PDF, dan penyimpanan lokal.
- `app/src/main/java/com/yanto/catatanuang/ui/theme/Theme.kt`: Token warna Oranye, Biru, Merah, Putih dan tipografi.
- `app/src/main/java/com/yanto/catatanuang/ui/components/`: Komponen UI (`CustomIcons.kt`, `Charts.kt`, `SharedComponents.kt`, `Modals.kt`).
- `app/src/main/java/com/yanto/catatanuang/ui/screens/`: Layar aplikasi (`HomeScreen.kt`, `SavingsHubScreen.kt`, `HistoryScreen.kt`, `GoalsScreen.kt`, `AnalyticsScreen.kt`, `CalendarScreen.kt`, `NotesScreen.kt`, `SettingsScreen.kt`, `PinLockScreen.kt`, `SplashScreen.kt`).
- `app/src/test/java/com/yanto/catatanuang/TabunganLogicTest.kt`: Unit test logika perhitungan, PIN, CSV, dan seri grafik.

---

## Perintah Build & Pengujian

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```
