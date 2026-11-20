# SegarStok

Aplikasi Android untuk UMKM makanan/minuman yang mencatat stok **per batch**, memantau tanggal kedaluwarsa, menghitung estimasi nilai modal berisiko, dan memberikan rekomendasi tindakan penjualan berbasis aturan.

## Stack Teknis
- **Bahasa:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Arsitektur:** Clean Architecture / MVVM + Repository + Domain Use Cases
- **Database Lokal:** Room (MVP Offline-First)
- **OCR:** Google ML Kit Text Recognition
- **Pengingat:** WorkManager + Notifikasi Lokal
- **Build System:** Gradle Kotlin DSL (`.gradle.kts`)
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target/Compile SDK:** 34 (Android 14)

## Versi Toolchain Teruji
- **Android Studio:** 2025.3.4
- **JDK:** 17
- **Gradle:** 8.7+
- **Android Gradle Plugin (AGP):** 8.5.2
- **Kotlin:** 2.0.21

## Cara Menjalankan Proyek
1. Buka folder proyek di Android Studio.
2. Pastikan JDK diatur ke JDK 17 (`File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`).
3. Jalankan Gradle Sync.
4. Jalankan aplikasi di emulator atau perangkat fisik (Android 8.0+ / API 26+).
5. Untuk kompilasi via terminal, jalankan:
   ```bash
   ./gradlew assembleDebug
   ```
