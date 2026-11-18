# SocialTrack — POCO F5

MVP Android untuk memantau durasi penggunaan Instagram, TikTok, YouTube, dan Facebook serta membuat estimasi perpindahan video pendek.

## Fitur
- Dark dashboard.
- Durasi penggunaan harian melalui Android UsageStatsManager.
- Penghitung video pendek berbasis Accessibility Service.
- Penyimpanan lokal melalui SharedPreferences.

## Catatan penting
Penghitung video adalah estimasi. Service menilai perubahan feed dan teks/label antarmuka yang terlihat. Update UI aplikasi sosial dapat mengubah hasil deteksi. Versi ini belum mengklaim dapat mengetahui apakah video benar-benar ditonton sampai selesai.

## Build APK
1. Buka folder proyek ini dengan Android Studio (versi yang mendukung Android Gradle Plugin 8.7.3).
2. Tunggu Gradle Sync dan instal Android SDK Platform 35 jika diminta.
3. Pilih **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK debug akan muncul di `app/build/outputs/apk/debug/app-debug.apk`.

## Pengaturan pada POCO F5
1. Instal APK.
2. Buka SocialTrack, pilih **Aktifkan akses penggunaan aplikasi**, lalu izinkan SocialTrack.
3. Pilih **Aktifkan penghitung video**, lalu aktifkan SocialTrack di daftar Accessibility.
4. Buka kembali SocialTrack.

MIUI/HyperOS dapat membatasi proses latar belakang. Jika penghitung berhenti, izinkan autostart dan atur penghemat baterai SocialTrack ke tanpa batasan bila tersedia.

## Privasi
Data statistik disimpan lokal di perangkat. Accessibility Service digunakan untuk mengamati elemen antarmuka feed; jangan aktifkan jika tidak nyaman memberikan akses tersebut. Proyek ini tidak mengirim data ke server.


## Build APK otomatis melalui GitHub Actions
1. Buat repository GitHub baru dan upload seluruh isi folder proyek ini.
2. Pastikan workflow `.github/workflows/build-apk.yml` ikut terunggah.
3. Buka tab **Actions**, pilih **Build SocialTrack APK**, lalu tekan **Run workflow** (atau push ke branch `main`).
4. Setelah job selesai, unduh artifact **SocialTrack-debug-apk** dari halaman run tersebut.
5. Ekstrak ZIP artifact dan instal `app-debug.apk` di POCO F5.
