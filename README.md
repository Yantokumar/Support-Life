# Catatan Uang

Aplikasi Android pencatatan keuangan offline yang dibangun dengan Kotlin dan Jetpack Compose.

## Fitur

- Ringkasan saldo, pemasukan, dan pengeluaran bulan berjalan.
- Pencatatan pemasukan, pengeluaran, dan setoran tabungan.
- Riwayat dengan filter berdasarkan jenis transaksi.
- Target tabungan dan ringkasan pengeluaran per kategori.
- Data disimpan lokal di perangkat menggunakan `SharedPreferences`.

## Menjalankan dari IntelliJ IDEA

Android Studio dan emulator tidak wajib. IntelliJ IDEA dapat digunakan untuk mengedit proyek dan menjalankan perintah Gradle. Pada `Settings > Build, Execution, Deployment > Build Tools > Gradle`, pilih JDK 21 di `Gradle JVM` (lokasi JDK: `%LOCALAPPDATA%\Programs\Temurin\jdk-21`). SDK Android berada di `%LOCALAPPDATA%\Android\Sdk`; `local.properties` menunjuk ke lokasi tersebut.

Setelah Gradle selesai sinkronisasi, jalankan `app > Tasks > build > assembleDebug` di jendela Gradle, atau jalankan perintah berikut di Terminal IntelliJ dari folder proyek. Untuk memasang langsung, hubungkan ponsel Android dengan USB debugging aktif:

```powershell
.\gradlew.bat assembleDebug
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

APK juga bisa disalin ke ponsel dan dipasang secara manual dari `app\build\outputs\apk\debug\app-debug.apk`. Proyek menggunakan Android SDK 35 dan mendukung Android 8.0 (API 26) ke atas.
