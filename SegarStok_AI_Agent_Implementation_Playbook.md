**Panduan eksekusi teknis, Agile Sprint, validasi, dan persiapan demo**  
Versi 1.0 · 20 November 2026

> Dokumen ini adalah instruksi kerja untuk Agen AI yang membantu membangun SegarStok dari proyek Android kosong sampai aplikasi MVP siap didemokan. Agen harus bekerja bertahap, menjaga aplikasi tetap dapat di-build, tidak melewati gerbang validasi, dan tidak menganggap tugas selesai hanya karena kode sudah ditulis.

---

## 0. Misi dan definisi produk

SegarStok adalah aplikasi Android untuk UMKM makanan/minuman yang mencatat stok **per batch**, memantau tanggal kedaluwarsa, mengingatkan pengguna, menghitung estimasi nilai modal stok berisiko, dan memberi rekomendasi tindakan penjualan berbasis aturan.

Fitur pembeda input tanggal adalah **ML Kit Text Recognition OCR + date parser**. OCR hanya mengekstrak teks dari label. Parser mengusulkan kandidat tanggal. Pengguna harus memilih dan mengonfirmasi atau mengoreksi kandidat sebelum tanggal disimpan. Input manual selalu tersedia sebagai fallback.

Fitur rekomendasi adalah **rule-based**, bukan AI generatif atau model prediksi. Rekomendasi tidak mengubah harga, stok, atau status secara otomatis.

### Keputusan produk yang harus dipertahankan

- Platform: Android, Kotlin, Android Studio.
- UI: Jetpack Compose direkomendasikan.
- Arsitektur: MVVM + Repository + domain/use cases.
- Database lokal: Room; MVP offline-first.
- OCR: Google ML Kit Text Recognition.
- Gambar: CameraX dan/atau Android Photo Picker.
- Pengingat: WorkManager + notifikasi lokal.
- Distribusi: Google Play Store dan penjualan langsung melalui APK.
- Monetisasi: pembelian satu kali; semua fitur MVP terbuka setelah pembelian.
- Asumsi lisensi awal: satu pembelian per usaha untuk satu perangkat utama. Ini kebijakan produk awal yang perlu dikonfirmasi sebelum rilis, bukan mekanisme DRM yang sudah tersedia.
- Dukungan: bug fix dan bantuan penggunaan dasar selama versi didukung; tidak menjanjikan dukungan seumur hidup.
- Tidak termasuk MVP: akun, backend, cloud sync/backup, multi-user, multi-cabang, POS, akuntansi, marketplace, chatbot, demand forecasting AI, MediaPipe, atau barcode yang diklaim membaca tanggal tanpa katalog.

### Alur nilai utama

1. Pengguna membuat catatan batch.
2. Pengguna memotret/memilih label.
3. ML Kit mengekstrak teks.
4. Parser mengidentifikasi kandidat tanggal dan konteksnya.
5. Pengguna mengonfirmasi atau mengoreksi tanggal.
6. Data disimpan ke Room.
7. Dashboard mengurutkan batch menurut urgensi.
8. Pengguna menerima reminder dan rekomendasi tindakan.
9. Pengguna memperbarui kuantitas/status saat produk terjual, dipakai, atau dibuang.

**Definisi batch:** kelompok unit produk dengan tanggal kedaluwarsa yang sama. Jika produk yang sama memiliki dua tanggal berbeda, catat sebagai dua batch terpisah.

---

## 1. Cara Agen AI wajib bekerja

### 1.1 Pola satu sesi pengerjaan

Setiap sesi Agen AI harus fokus pada satu Sprint atau satu irisan pekerjaan kecil yang dinyatakan jelas.

Urutan wajib:

1. **Inspect** — periksa struktur repo, branch, file, dependency, TODO, dan kondisi build saat ini. Jangan menimpa kode yang sudah ada tanpa membaca.
2. **Plan** — tulis tujuan sesi, file yang akan disentuh, dependensi, risiko, dan acceptance criteria.
3. **Implement** — kerjakan hanya scope yang disepakati. Gunakan pola arsitektur dan penamaan yang konsisten.
4. **Build** — jalankan Gradle build yang relevan.
5. **Test** — jalankan unit test/instrumentation/UI test yang relevan.
6. **Review** — periksa error, warning penting, nullability, lifecycle, permission, data migration, dan edge case.
7. **Report** — rangkum file berubah, implementasi, perintah yang dijalankan, hasil test/build, masalah tersisa, dan langkah berikutnya.

Agen tidak boleh menyatakan build/test berhasil jika perintah tidak benar-benar dijalankan. Jika environment tidak menyediakan Android SDK/emulator/perangkat, nyatakan batas validasi dan berikan langkah manual yang harus dijalankan developer.

### 1.2 Definition of Done untuk setiap task

Task dianggap selesai hanya jika:
- perubahan sesuai kebutuhan dan tidak memperluas scope tanpa persetujuan;
- kode dapat dikompilasi pada target yang tersedia;
- test relevan ditambahkan atau diperbarui;
- error dan empty state ditangani;
- tidak ada data contoh/placeholder yang masuk ke alur produksi secara tidak sengaja;
- UI dapat digunakan dan state loading/success/error jelas;
- dokumentasi teknis diperbarui jika keputusan atau konfigurasi berubah;
- hasil validasi dilaporkan secara jujur.

### 1.3 Aturan kualitas kode

- Hindari satu file raksasa; pisahkan UI, ViewModel, domain, data, OCR, parser, dan worker.
- Business rule jangan diletakkan hanya di composable.
- Gunakan `LocalDate` untuk tanggal kedaluwarsa; hindari menyimpan tanggal sebagai timestamp UTC yang dapat bergeser karena zona waktu.
- Harga Rupiah disimpan sebagai integer `Long`; jangan memakai `Float`/`Double` untuk uang.
- Kuantitas boleh `Double` bila unit pecahan diperlukan; validasi harus mencegah nilai negatif.
- Gunakan enum/sealed type atau konstanta terpusat untuk kategori/status/urgensi.
- Semua operasi database di luar main thread melalui coroutine/Room.
- Jangan log isi foto label atau data bisnis sensitif.
- Jangan meminta permission terlalu dini; minta saat pengguna memulai fitur terkait.
- Jangan membuat klaim bahwa produk aman dikonsumsi hanya karena tanggal belum lewat.
- Jangan menambahkan backend atau library AI lain tanpa kebutuhan yang disetujui.
- Jangan mengubah versi dependency secara acak. Verifikasi kompatibilitas Gradle, Kotlin, Compose, ML Kit, Room, dan WorkManager sebelum mengunci versi.

### 1.4 Struktur repository yang dituju

```text
app/
  src/main/
    java/<package>/
      core/
        common/
        design/
        navigation/
      data/
        local/
          dao/
          entity/
          database/
        repository/
      domain/
        model/
        repository/
        usecase/
        rules/
      feature/
        dashboard/
        stocklist/
        addbatch/
        batchdetail/
        recommendation/
        settings/
        purchase/
      ml/
        image/
        ocr/
        parser/
      notifications/
      MainActivity.kt
    res/
  src/test/
  src/androidTest/
```

Agen boleh menyesuaikan struktur bila repository yang sudah ada memakai konvensi lain, tetapi tanggung jawab tiap lapisan harus tetap terpisah.

---

## 2. Rencana Agile Sprint end-to-end

Asumsi: satu developer, sprint pendek 3–5 hari kerja (atau disesuaikan kalender), target total sekitar 4–6 minggu kerja efektif. Estimasi bukan janji. Sprint dapat diperpanjang jika gerbang validasi belum lolos.

| Sprint | Fokus | Hasil utama |
|---|---|---|
| 0 | Project bootstrap | Proyek Android kosong berhasil build |
| 1 | Arsitektur, design system, navigasi | Kerangka aplikasi dan layar dasar |
| 2 | Database Room dan CRUD batch | Data batch tersimpan dan dikelola |
| 3 | Alur kamera/galeri dan ML Kit OCR | Teks label berhasil diekstrak |
| 4 | Date parser dan konfirmasi OCR | Kandidat tanggal aman dan dapat dikoreksi |
| 5 | Dashboard, sorting, detail, filter | Pengguna melihat prioritas batch |
| 6 | Status stok dan Expiry Loss Monitor | Kuantitas/status dan nilai risiko konsisten |
| 7 | Smart Reminder | Notifikasi lokal terjadwal |
| 8 | Smart Action Recommendation | Saran tindakan berbasis aturan |
| 9 | Pembelian satu kali dan distribusi | Jalur Play dan APK langsung dirancang/diuji sesuai scope |
| 10 | QA, UX polish, demo readiness | MVP stabil dan skenario demo siap |

### Sprint 0 — Bootstrap proyek kosong

**Tujuan:** membuat fondasi yang dapat dibangun sebelum fitur ditambahkan.

**Pekerjaan Agen**
- Periksa kebutuhan proyek dan buat proyek Android Kotlin.
- Pilih Jetpack Compose, Material 3, min/target SDK yang sesuai dengan toolchain saat ini.
- Konfigurasikan Gradle, Kotlin, Compose compiler/plugin, namespace, applicationId, versionCode/versionName.
- Buat package dasar dan `MainActivity`.
- Buat konfigurasi debug/release dasar.
- Siapkan Git repository, `.gitignore`, README, dan aturan branch/commit sederhana.
- Pastikan aplikasi kosong dapat dijalankan di emulator/perangkat.

**Validasi / exit criteria**
- `./gradlew assembleDebug` berhasil.
- Aplikasi terbuka tanpa crash.
- Tidak ada dependency yang tidak kompatibel.
- README menjelaskan cara membuka dan menjalankan proyek.
- Catat versi Android Studio/JDK/Gradle/SDK yang dipakai.

**Jangan lanjut** bila build dasar gagal.

### Sprint 1 — Arsitektur, navigasi, design system, dan UI skeleton

**Tujuan:** membentuk kerangka UX sebelum logika fitur menumpuk.

**Pekerjaan Agen**
- Implementasikan struktur MVVM + Repository.
- Buat theme warna, typography, spacing, komponen tombol, text field, card, badge status, dialog konfirmasi.
- Buat navigation graph.
- Buat layar skeleton: Dashboard, Daftar Stok, Tambah Batch, Detail Batch, Rekomendasi, Pengaturan/Pembelian.
- Buat state UI standar: Loading, Success, Empty, Error.
- Pastikan layout responsif untuk ukuran layar Android umum dan keyboard tidak menutupi form.

**Validasi**
- Semua layar dapat dibuka dan kembali dengan benar.
- Navigasi tidak membuat layar duplikat yang tidak perlu.
- UI skeleton konsisten dan tidak ada tombol utama yang inert tanpa label “belum tersedia”.
- Accessibility dasar: content description, kontras, ukuran target sentuh.

### Sprint 2 — Room database dan CRUD batch

**Tujuan:** menyelesaikan sumber data lokal yang menjadi fondasi fitur lain.

**Model data minimum**
- `id`
- `productName`
- `category`
- `quantity`
- `unit`
- `expiryEpochDay` atau representasi tanggal lokal yang konsisten
- `purchasePricePerUnit` (opsional sesuai keputusan produk)
- `normalSellingPricePerUnit` (opsional untuk simulasi promo)
- `status` (AVAILABLE, SOLD_OR_USED, DISCARDED)
- `dateEntryMethod` (OCR atau MANUAL)
- `notes` opsional
- `createdAt`, `updatedAt`

**Pekerjaan Agen**
- Buat Entity, DAO, Room Database, TypeConverter bila diperlukan, Repository.
- Buat migration strategy sejak awal; jangan memakai destructive migration untuk data pengguna.
- Implementasikan tambah, baca, ubah, hapus batch.
- Buat validasi domain: nama wajib, kuantitas tidak negatif, tanggal wajib, harga tidak negatif.
- Hubungkan form tambah/edit dan detail batch ke ViewModel/Repository.
- Pastikan batch dengan tanggal berbeda tetap menjadi record terpisah.

**Validasi**
- Unit test validasi domain.
- CRUD berjalan dari UI.
- Data bertahan setelah aplikasi ditutup dan dibuka ulang.
- Edit tidak membuat duplikasi record.
- Kuantitas negatif dan harga negatif ditolak.
- Migration test ditambahkan saat schema berubah.

### Sprint 3 — Kamera/galeri dan integrasi ML Kit Text Recognition

**Tujuan:** mengambil gambar label dan menghasilkan teks OCR.

**Pekerjaan Agen**
- Tambahkan pemilihan gambar dari galeri menggunakan Photo Picker.
- Tambahkan capture kamera dengan CameraX bila diperlukan untuk UX MVP.
- Tangani permission kamera, cancel, gambar kosong, rotasi, ukuran gambar, dan lifecycle.
- Buat abstraction `TextRecognizer`/OCR service agar dapat diuji.
- Konversi gambar ke `InputImage`.
- Jalankan ML Kit Text Recognition.
- Ambil teks penuh dan, bila tersedia, struktur block/line untuk konteks.
- Tampilkan hasil OCR mentah untuk debugging internal; jangan menyimpannya sebagai tanggal otomatis.
- Tutup/release resource recognizer sesuai lifecycle.

**Validasi**
- Foto label jelas menghasilkan teks yang masuk akal.
- Foto blur/gelap/miring/pantulan menghasilkan pesan gagal atau teks parsial tanpa crash.
- Pengguna dapat membatalkan proses.
- Permission ditolak ditangani dengan instruksi yang jelas.
- OCR tidak menulis tanggal ke database.
- Test OCR memakai abstraction/mock untuk memverifikasi alur tanpa bergantung pada ML Kit saat unit test.

### Sprint 4 — Date Parser, klasifikasi konteks, dan konfirmasi manual

**Tujuan:** mengubah teks OCR menjadi kandidat tanggal secara konservatif. Ini Sprint penting dan tidak boleh disederhanakan menjadi satu regex.

#### 4.1 Prinsip parser

- Parser menerima teks OCR, bukan gambar.
- Parser mengembalikan **nol, satu, atau beberapa kandidat**; bukan satu tanggal yang langsung dipercaya.
- Kandidat menyertakan teks asli, tanggal hasil parse, format yang dikenali, konteks baris/label, dan tingkat keyakinan/penjelasan.
- Parser tidak boleh menebak bila format ambigu.
- Semua kandidat harus dikonfirmasi pengguna.
- Jika tidak ada kandidat, tampilkan input tanggal manual.
- Tanggal produksi (MFG/PROD/Manufactured) harus dibedakan dari tanggal kedaluwarsa (EXP/ED/Use by/Best before). Jika konteks tidak jelas, minta pengguna memilih.
- Jangan mengubah angka secara agresif. Normalisasi OCR hanya boleh menghasilkan kandidat tambahan, bukan mengganti bukti mentah.

#### 4.2 Format tanggal yang perlu didukung

Prioritaskan format numerik yang lazim dijumpai pada label:
- `dd/MM/yyyy` — contoh `25/12/2026`
- `dd-MM-yyyy` — contoh `25-12-2026`
- `dd.MM.yyyy` — contoh `25.12.2026`
- `yyyy-MM-dd` — contoh `2026-12-25`
- `dd MMM yyyy` — contoh `25 DEC 2026`, `25 DES 2026`
- `dd MMMM yyyy` — contoh `25 DESEMBER 2026`
- format dua digit tahun seperti `25/12/26` hanya jika aturan pivot year ditetapkan dan diuji; sebaiknya minta konfirmasi ekstra.
- Format `MM/yyyy` atau `MM/yy` tidak cukup untuk menentukan satu hari. Tandai sebagai kandidat bulan/tahun yang belum dapat disimpan sebagai tanggal lengkap kecuali produk menetapkan aturan yang jelas. Jangan otomatis mengasumsikan hari terakhir bulan.

Dukungan bulan teks minimal:
- Indonesia: JAN/JANUARI, FEB/FEBRUARI, MAR/MARET, APR/APRIL, MEI, JUN/JUNI, JUL/JULI, AGU/AGUSTUS, SEP/SEPTEMBER, OKT/OKTOBER, NOV/NOVEMBER, DES/DESEMBER.
- Inggris: JAN/JANUARY, FEB/FEBRUARY, MAR/MARCH, APR/APRIL, MAY, JUN/JUNE, JUL/JULY, AUG/AUGUST, SEP/SEPTEMBER, OCT/OCTOBER, NOV/NOVEMBER, DEC/DECEMBER.
- Parser harus case-insensitive dan menangani titik setelah singkatan bulan bila memungkinkan.

#### 4.3 Pipeline algoritma parser

Implementasikan tahap demi tahap:

1. **Preserve raw OCR:** simpan teks OCR sementara apa adanya untuk proses dan debugging; jangan hilangkan teks asli dari kandidat.
2. **Normalize:** ubah ke uppercase untuk pencarian label; normalisasi whitespace; samakan variasi dash; pertahankan pemisah tanggal `/`, `-`, `.`. Jangan menghapus seluruh tanda baca.
3. **Split context:** proses per baris dan jendela baris tetangga agar tanggal bisa dikaitkan dengan label di dekatnya.
4. **Detect date context:** cari kata kunci:
   - expiry: `EXP`, `EX`, `ED`, `USE BY`, `BEST BEFORE`, `BB`, `KEDALUWARSA`, `KADALUWARSA`, `BAIK DIGUNAKAN SEBELUM`.
   - production: `MFG`, `MFD`, `PROD`, `MFG DATE`, `TGL PRODUKSI`.
   - Jika kata kunci expiry dekat kandidat, naikkan prioritas kandidat expiry.
   - Jika kata kunci produksi dekat kandidat, klasifikasikan sebagai production date dan jangan tampilkan sebagai expiry default.
   - Jika kedua konteks ditemukan atau tidak jelas, tampilkan kandidat terpisah dengan label yang jelas.
5. **Pattern extraction:** gunakan regex terpisah untuk format numerik empat digit tahun, ISO, bulan teks, dan format dua digit tahun.
6. **OCR correction candidates:** bila OCR menghasilkan karakter mirip angka (contoh `O` vs `0`, `I/l` vs `1`, `S` vs `5`), buat varian koreksi terbatas hanya pada token yang tampak seperti tanggal. Jangan mengganti teks mentah. Beri keyakinan lebih rendah dan minta konfirmasi.
7. **Parse strict:** gunakan `LocalDate`/`DateTimeFormatter` dengan strict resolver. Validasi hari-bulan-tahun, leap year, jumlah hari per bulan.
8. **Resolve ambiguity:**
   - `03/04/2026` dapat berarti 3 April atau 4 Maret. Jangan memilih diam-diam. Untuk konteks Indonesia, tampilkan interpretasi yang didukung format lokal dan minta pengguna mengonfirmasi; jika tidak ada bukti format, tandai ambigu.
   - `2026/04/03` dapat diproses sebagai yyyy/MM/dd hanya bila pattern ini memang diaktifkan dan diuji.
   - tanggal tanpa tahun atau hanya bulan/tahun tidak boleh dilengkapi dengan tebakan.
9. **Deduplicate:** kandidat identik dari beberapa pattern digabung, tetapi simpan sumber baris dan format yang cocok.
10. **Rank, don't decide:** urutkan kandidat berdasarkan konteks expiry, format yang valid, kedekatan label, dan keyakinan parse. Ranking hanya membantu UI.
11. **Validate business plausibility:** tanggal terlalu jauh di masa lalu/masa depan dapat diberi warning untuk pemeriksaan, bukan otomatis dihapus. Jangan gunakan rentang umur simpan generik yang mengubah tanggal label.
12. **Return result:** `NoCandidate`, `Candidates(list)`, atau `Ambiguous(reason, candidates)`.

#### 4.4 Model hasil parser konseptual

```kotlin
data class DateCandidate(
    val date: LocalDate,
    val rawText: String,
    val matchedFormat: String,
    val context: DateContext, // EXPIRY, PRODUCTION, UNKNOWN
    val confidence: CandidateConfidence,
    val sourceLine: String,
    val warnings: List<String>
)

enum class DateContext { EXPIRY, PRODUCTION, UNKNOWN }
enum class CandidateConfidence { HIGH, MEDIUM, LOW }

sealed interface DateParseResult {
    data object NoCandidate : DateParseResult
    data class Candidates(val items: List<DateCandidate>) : DateParseResult
    data class Ambiguous(
        val reason: String,
        val items: List<DateCandidate>
    ) : DateParseResult
}
```

Ini rancangan model; sesuaikan implementasi dengan versi Kotlin yang dipakai.

#### 4.5 UX konfirmasi

- Tampilkan foto/teks hasil OCR bila membantu.
- Tampilkan kandidat tanggal dengan label konteks, misalnya “Kemungkinan tanggal kedaluwarsa” atau “Tanggal produksi — bukan tanggal kedaluwarsa”.
- Pengguna memilih kandidat lalu menekan “Gunakan tanggal”.
- Pengguna dapat mengedit tanggal melalui date picker.
- Jika parser tidak menemukan kandidat, langsung sediakan input manual.
- Simpan `dateEntryMethod = OCR_CONFIRMED` atau `MANUAL` (nama enum final konsisten).
- Simpan hanya tanggal yang sudah dikonfirmasi.
- Jika pengguna membatalkan, jangan mengubah data batch.

#### 4.6 Test matrix parser wajib

| Input OCR | Hasil yang diharapkan |
|---|---|
| `EXP 25/12/2026` | Kandidat expiry 25 Des 2026 |
| `ED: 25-12-2026` | Kandidat expiry |
| `Best Before 25 DEC 2026` | Kandidat expiry |
| `Baik digunakan sebelum 25 Desember 2026` | Kandidat expiry |
| `MFG 01/10/2026` | Kandidat production, bukan expiry |
| `MFG 01/10/2026` + `EXP 01/10/2027` | Dua kandidat dengan konteks berbeda |
| `03/04/2026` tanpa label | Ambigu; minta konfirmasi |
| `2026-12-25` | Kandidat ISO |
| `31/02/2026` | Tidak valid |
| `29/02/2024` | Valid |
| `29/02/2025` | Tidak valid |
| `25/12/26` | Kandidat low/medium hanya bila pivot year diaktifkan |
| `12/2026` | Tidak menjadi tanggal lengkap secara otomatis |
| teks tanpa angka tanggal | NoCandidate |
| `EXP 2S/12/2026` | Koreksi OCR opsional sebagai kandidat confidence rendah; raw text tetap ditampilkan |

Tambahkan unit test untuk variasi spasi, titik, huruf kecil/besar, karakter OCR, beberapa kandidat dalam satu baris, tanggal duplikat lintas baris, dan locale perangkat berbeda.

**Exit criteria**
- Parser mendukung format yang diprioritaskan dan mendokumentasikan format yang belum didukung.
- Semua test matrix lulus.
- Kandidat tidak pernah tersimpan tanpa konfirmasi.
- Tanggal produksi tidak diperlakukan otomatis sebagai tanggal kedaluwarsa.
- Manual fallback berfungsi meskipun OCR/parse gagal.

### Sprint 5 — Dashboard, sorting, detail batch, pencarian, filter

**Tujuan:** menjadikan data batch berguna dan mudah dipahami.

**Pekerjaan Agen**
- Buat ringkasan jumlah batch tersedia, perhatian, mendesak, hari H, dan lewat tanggal.
- Urutkan batch berdasarkan tanggal paling dekat terlebih dahulu.
- Buat kategori urgensi dari satu domain rule yang dipakai bersama:
  - lebih dari 14 hari: NORMAL;
  - 8–14 hari: ATTENTION;
  - 4–7 hari: NEAR;
  - 1–3 hari: URGENT;
  - 0 hari: DUE_TODAY;
  - kurang dari 0: EXPIRED.
- Buat daftar batch dengan nama, kategori, sisa jumlah, tanggal, dan status.
- Buat layar detail dan edit.
- Tambahkan search nama produk dan filter kategori/status/urgensi.
- Sediakan empty state dan state tanpa hasil filter.

**Validasi**
- Unit test batas tanggal: 15, 14, 8, 7, 4, 3, 1, 0, -1 hari.
- Urutan tanggal benar.
- Perubahan tanggal langsung memperbarui dashboard.
- Search/filter dapat dikombinasikan.
- Status selain AVAILABLE tidak dihitung sebagai stok tersedia.
- Uji perubahan tanggal ketika hari lokal berganti.

### Sprint 6 — Status batch dan Expiry Loss Monitor

**Tujuan:** memastikan jumlah stok dan nilai modal berisiko konsisten.

**Pekerjaan Agen**
- Implementasikan status AVAILABLE, SOLD_OR_USED, DISCARDED.
- Implementasikan pengurangan kuantitas untuk penjualan/pemakaian sebagian.
- Cegah jumlah negatif; jika jumlah menjadi nol, tandai selesai sesuai keputusan UX.
- Buat Expiry Loss Monitor:
  `remainingValue = remainingQuantity × purchasePricePerUnit`.
- Kelompokkan nilai batch mendekati tanggal dan sudah lewat tanggal secara terpisah.
- Jika harga beli kosong, tampilkan “harga belum diisi”, bukan nilai Rp0.
- Pastikan batch nonaktif tidak dihitung sebagai stok tersedia.
- Jangan menyebut nilai modal berisiko sebagai kerugian aktual.

**Validasi**
- Test rumus dengan nilai bulat dan jumlah pecahan bila didukung.
- Uji harga kosong.
- Uji kuantitas nol, pengurangan sebagian, dan status.
- Uji perubahan status dari AVAILABLE ke SOLD_OR_USED/DISCARDED.
- Dashboard, daftar, dan monitor menampilkan angka konsisten.

### Sprint 7 — Smart Reminder dan notifikasi lokal

**Tujuan:** mengingatkan pengguna secara berkala tanpa spam.

**Pekerjaan Agen**
- Implementasikan Worker melalui WorkManager.
- Atur jadwal periodik yang realistis dan toleran keterlambatan.
- Ambang awal reminder dapat berupa H-7, H-3, H-1, hari H, dan peringatan setelah tanggal dengan frekuensi terbatas.
- Gunakan notification channel.
- Buat ID/tag notifikasi stabil untuk mencegah duplikasi.
- Abaikan batch nonaktif atau jumlah nol.
- Setelah tanggal, notifikasi hanya meminta pengguna memeriksa dan menghentikan penjualan; jangan menawarkan promo.
- Minta permission notifikasi pada versi Android yang memerlukannya dan jelaskan manfaatnya.

**Validasi**
- Worker dapat dijadwalkan dan dijalankan pada perangkat uji.
- Deduplikasi bekerja.
- Batch nonaktif tidak diberi notifikasi.
- Permission ditolak tidak menyebabkan crash.
- Uji Doze/battery optimization sebatas perangkat tersedia.
- Jelaskan bahwa WorkManager bukan alarm presisi menit.

### Sprint 8 — Smart Action Recommendation dan simulasi promo

**Tujuan:** memberi saran operasional yang transparan dan relevan, bukan mengubah stok otomatis.

**Input rule engine**
- kategori produk;
- sisa hari sampai tanggal;
- jumlah tersisa dan unit;
- harga beli per unit (opsional);
- harga jual normal per unit (opsional);
- status batch;
- informasi kondisi penyimpanan/kemasan jika disediakan pengguna.

**Urutan keputusan wajib**
1. Jika status bukan AVAILABLE atau jumlah nol: jangan berikan rekomendasi penjualan.
2. Jika tanggal sudah lewat: hanya rekomendasikan hentikan penjualan dan keluarkan dari stok tersedia.
3. Jika hari H: tampilkan peringatan untuk memeriksa label/kebijakan; jangan menyarankan menunda penjualan melewati tanggal.
4. Jika belum lewat: rekomendasikan tindakan menurut urgensi dan kategori.
5. Jika harga beli dan harga jual normal tersedia: tampilkan simulasi diskon/margin.
6. Jika margin promo nol/negatif: warning jelas.
7. Tidak ada harga jual normal: jangan mengarang harga promo.
8. Rekomendasi tidak mengubah harga, kuantitas, atau status tanpa aksi eksplisit pengguna.

**Aturan awal**
- >14 hari: FEFO/rotasi normal.
- 8–14 hari: prioritaskan display dan evaluasi restock.
- 4–7 hari: diskon terbatas, bundling, tawarkan ke pelanggan.
- 1–3 hari: promosi segera hanya jika petunjuk label dan penyimpanan dipatuhi.
- 0 hari: pemeriksaan ketat.
- <0 hari: hentikan penjualan; keluarkan dari stok tersedia.

**Heuristik kategori**
- Frozen food: FEFO, display, bundling, penawaran multi-unit; jangan sarankan thaw/refreeze.
- Bakery: promo hari berjalan/bundling/evaluasi produksi; pengolahan ulang bukan saran umum.
- Minuman kemasan: multi-unit/bundling/display; jangan sarankan membuka atau mengemas ulang.
- Snack/makanan kemasan: diskon terbatas, bundling, penawaran grosir.
- Other: FEFO dan evaluasi generik.

**Kalkulasi**
- `promoPrice = normalSellingPrice × (1 − discountRate)`
- `grossMargin = promoPrice − purchasePricePerUnit`
- Contoh: harga normal Rp15.000, beli Rp10.000, diskon 10% → promo Rp13.500, margin kotor Rp3.500/unit sebelum biaya lain.
- Simpan Rupiah integer; lakukan pembulatan dengan aturan konsisten.
- Jelaskan bahwa margin kotor belum mengurangi biaya operasional.

**Guardrail keamanan pangan**
- Ikuti tanggal dan petunjuk penyimpanan pada label.
- Produk lewat tanggal tidak boleh mendapat saran jual, konsumsi, atau olah ulang.
- Jangan menyarankan tanggal diperpanjang karena dimasak, dibekukan, didiskon, atau dikemas ulang.
- Aplikasi tidak mengukur suhu, kondisi mikrobiologis, atau keamanan pangan.
- Jangan menjanjikan produk aman hanya karena tanggal belum lewat.

**Validasi**
- Unit test seluruh batas hari.
- Test setiap kategori.
- Test batch nonaktif dan quantity nol.
- Test expired tidak pernah menghasilkan promo.
- Test harga kosong, margin positif, nol, negatif.
- UI menampilkan alasan rekomendasi dan disclaimer.
- Pastikan tidak ada mutasi otomatis pada harga/qty/status.

### Sprint 9 — Pembelian satu kali, lisensi, dan distribusi

**Tujuan:** menyiapkan cara pengguna mendapatkan aplikasi tanpa mengubah MVP offline-first menjadi layanan server.

**Pekerjaan Agen**
- Buat layar informasi/pembelian dan entitlement state.
- Semua fitur MVP harus terbuka setelah pembelian valid.
- Untuk Google Play: rancang integrasi pembelian satu kali mengikuti dokumentasi Google Play Billing terbaru, termasuk status pembelian, acknowledge, restore/re-query, refund/revoke, dan error handling.
- Jangan menganggap screenshot pembayaran sebagai bukti transaksi.
- Untuk penjualan langsung: dokumentasikan alur demo, pembayaran, penerbitan APK, bukti transaksi, versi aplikasi, dan bantuan instalasi.
- Kebijakan awal yang disarankan: satu lisensi per usaha untuk satu perangkat utama; prosedur pindah perangkat harus tertulis.
- Kode aktivasi lokal hanya sarana administrasi sederhana dan bukan perlindungan anti-pembajakan kuat.
- Jangan membuat backend untuk validasi lisensi lintas perangkat pada MVP kecuali keputusan produk berubah.
- Pisahkan entitlement Play dan entitlement direct agar sumber pembelian tidak tercampur.
- Siapkan signing config dan keystore dengan aman; jangan commit secret/keystore ke repository publik.
- Buat release APK/AAB sesuai jalur distribusi.

**Validasi**
- Uji state belum membeli, pembelian sukses, pending, gagal, restore, refund/revoke sesuai jalur integrasi.
- Jalur demo direct dapat dipasang pada perangkat uji dan seluruh fitur MVP terbuka.
- Pastikan debug build tidak dianggap sebagai pembelian produksi.
- Uji fresh install dan upgrade.
- Dokumentasikan kebijakan dukungan: bug fix dan bantuan penggunaan selama versi didukung; tidak ada janji dukungan seumur hidup.
- Verifikasi aturan Play dan versi library saat implementasi karena dapat berubah.

### Sprint 10 — QA menyeluruh, UX polish, dokumentasi, demo readiness

**Tujuan:** menutup celah antarfitur dan menyiapkan aplikasi untuk dipresentasikan.

**Pekerjaan Agen**
- Jalankan regression test seluruh alur utama.
- Perbaiki crash, state hilang, loading yang menggantung, validasi yang tidak konsisten, dan layout overflow.
- Uji rotasi/perubahan lifecycle jika didukung.
- Uji izin kamera/notifikasi ditolak.
- Uji tanggal berubah ketika aplikasi lama tidak dibuka.
- Uji database migration dan data dummy demo.
- Tambahkan onboarding singkat dan bantuan cara scan label.
- Tambahkan halaman tentang aplikasi, kebijakan data lokal, dan batasan keamanan pangan.
- Siapkan data demo yang realistis dan tidak menggunakan data pribadi pelanggan.
- Siapkan README, panduan instalasi, catatan versi, known issues, dan skrip demo.
- Buat release build, cek ukuran, signing, dan install ulang pada perangkat demo.

**Validasi final**
- Build release berhasil.
- Install fresh berhasil.
- Tidak ada crash pada seluruh skenario demo.
- Data batch bertahan setelah aplikasi ditutup.
- OCR → parser → konfirmasi → simpan berfungsi.
- Manual entry berfungsi saat OCR gagal.
- Dashboard, sorting, detail, filter, status, monitor, reminder, dan rekomendasi berfungsi.
- Batch expired tidak mendapat saran promosi.
- Pembelian/aktivasi pada jalur demo sesuai kebijakan.
- Semua keterbatasan diketahui dan disampaikan saat demo.

---

## 3. Rangkaian test end-to-end wajib

Gunakan data contoh yang jelas ditandai sebagai data demo:

| Produk | Kategori | Qty | Tanggal | Harga beli | Harga jual normal |
|---|---|---:|---|---:|---:|
| Nugget ayam 500 g | FROZEN_FOOD | 12 pack | H-5 dari tanggal uji | Rp10.000 | Rp15.000 |
| Roti isi | BAKERY | 5 pcs | H-1 | Rp8.000 | Rp12.000 |
| Teh botol | PACKAGED_BEVERAGE | 20 botol | H-10 | Rp3.000 | Rp5.000 |
| Keripik | PACKAGED_SNACK | 30 pack | H-20 | Rp4.000 | Rp7.000 |
| Sosis | FROZEN_FOOD | 8 pack | H+2 (lewat) | Rp12.000 | Rp18.000 |

Tanggal relatif harus dibuat dari tanggal uji, jangan hardcode tanggal lama. Pastikan produk H+2 tidak pernah mendapat rekomendasi promo.

### Skenario demo utama (5–7 menit)

1. Buka Dashboard dan jelaskan prioritas stok.
2. Tambah batch Nugget Ayam.
3. Scan label (atau gunakan foto uji) dan tampilkan hasil OCR.
4. Tunjukkan parser memberi kandidat tanggal.
5. Konfirmasi tanggal; tunjukkan pengguna dapat mengoreksi.
6. Simpan batch dan lihat tampil pada Dashboard.
7. Buka detail; ubah jumlah tersisa.
8. Tunjukkan Expiry Loss Monitor.
9. Buka rekomendasi batch H-5 dan simulasi diskon/margin.
10. Buka batch yang sudah lewat tanggal dan tunjukkan guardrail hentikan penjualan.
11. Tunjukkan pengaturan reminder dan status pembelian/aktivasi sesuai build demo.

Siapkan foto label cadangan dan input manual sebagai backup demo jika kamera/perangkat bermasalah.

---

## 4. Definition of Done keseluruhan MVP

MVP hanya dinyatakan siap demo jika seluruh butir berikut terpenuhi:

- [ ] Proyek Android dapat dibangun dari repository bersih.
- [ ] UI utama lengkap dan navigasi berfungsi.
- [ ] CRUD batch berjalan dengan Room.
- [ ] Data bertahan setelah aplikasi ditutup.
- [ ] Kamera/galeri dapat memberikan gambar ke OCR.
- [ ] ML Kit menghasilkan teks atau error yang ditangani.
- [ ] Parser mendukung format tanggal MVP, menguji format ambigu, dan tidak menebak.
- [ ] Kandidat tanggal harus dikonfirmasi sebelum disimpan.
- [ ] Input tanggal manual selalu tersedia.
- [ ] Tanggal produksi dibedakan dari tanggal kedaluwarsa.
- [ ] Dashboard sorting, filter, dan pencarian berfungsi.
- [ ] Status dan kuantitas tidak menghasilkan stok negatif.
- [ ] Expiry Loss Monitor menghitung nilai modal berisiko secara benar.
- [ ] Reminder lokal tidak berulang tak terkendali.
- [ ] Recommendation Engine menjelaskan alasan saran.
- [ ] Kalkulator promo menangani harga kosong dan margin nol/negatif.
- [ ] Batch lewat tanggal tidak mendapat rekomendasi penjualan.
- [ ] Tidak ada perubahan harga/status otomatis tanpa tindakan pengguna.
- [ ] Jalur distribusi demo tersedia dan kebijakan lisensi/support dijelaskan.
- [ ] Build release terpasang pada perangkat demo.
- [ ] README, panduan demo, dan known issues tersedia.

---

## 5. Format laporan Agen AI di akhir setiap Sprint

Agen harus selalu mengakhiri pengerjaan dengan format:

```text
SPRINT:
STATUS: DONE / PARTIAL / BLOCKED

1. Tujuan sprint
2. Implementasi yang selesai
3. File yang dibuat/diubah
4. Keputusan teknis yang diambil
5. Perintah build/test yang dijalankan
6. Hasil build
7. Hasil test (jumlah lulus/gagal atau alasan tidak dapat dijalankan)
8. Validasi manual yang masih diperlukan
9. Bug/risiko yang diketahui
10. Sisa pekerjaan
11. Rekomendasi Sprint berikutnya
```

Jika ada test gagal, Agen harus memperbaiki atau menjelaskan penyebab dan dampaknya. Jangan menandai Sprint DONE jika acceptance criteria utamanya belum terpenuhi.

---

## 6. Instruksi awal saat dokumen ini diberikan kepada Agen AI

Mulai dari **Sprint 0 saja**. Jangan langsung menghasilkan seluruh aplikasi dalam satu jawaban atau membuat semua fitur sekaligus.

Langkah pertama Agen:
1. Periksa repository/workspace yang tersedia.
2. Laporkan apakah repository benar-benar kosong atau sudah berisi proyek.
3. Tanyakan hanya keputusan teknis yang benar-benar menghalangi bootstrap; untuk hal lain gunakan keputusan dalam dokumen ini sebagai default dan catat asumsi.
4. Buat proyek dasar.
5. Jalankan build.
6. Laporkan hasil dan tunggu persetujuan sebelum memulai Sprint 1.

**Prinsip utama:** satu Sprint = satu lingkup kerja yang selesai, diuji, dan dapat diverifikasi. Jaga aplikasi tetap buildable di setiap tahap. Jangan mengorbankan konfirmasi OCR, integritas data, atau guardrail keamanan pangan demi mengejar banyak fitur.
"""