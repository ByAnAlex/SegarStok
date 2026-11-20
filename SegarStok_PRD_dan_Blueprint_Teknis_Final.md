# SEGARSTOK — PRD DAN BLUEPRINT TEKNIS
Versi final 3.0 | 20 November 2026

## 1. Tujuan dan ringkasan
Dokumen ini menjadi acuan implementasi dan pengujian SegarStok oleh satu pengembang dalam 4–6 minggu. Aplikasi Android Kotlin offline-first mencatat stok makanan/minuman per batch, memantau tanggal, dan memberi rekomendasi strategi penjualan berbasis aturan. OCR ML Kit hanya menghasilkan teks; tanggal tidak boleh tersimpan sebelum konfirmasi pengguna.

## 2. Ruang lingkup
**MVP:** CRUD batch; nama/kategori/kuantitas/unit/tanggal/harga beli/harga jual normal opsional; scan kamera/galeri; ML Kit; parser; konfirmasi/koreksi/manual; dashboard dan sorting; reminder lokal; status batch; Expiry Loss Monitor; search/filter; Smart Action Recommendation; simulasi promo; pembelian satu kali yang membuka seluruh fitur MVP.

**Di luar MVP:** login, cloud sync/backup, multi-user, multi-cabang, marketplace, POS, akuntansi, prediksi permintaan AI, chatbot generatif, MediaPipe, katalog barcode eksternal, dan rekomendasi otomatis mengolah ulang makanan.

## 3. Pengguna dan user stories
Pengguna utama pemilik/pengelola UMKM satu lokasi dengan puluhan/ratusan batch; MVP satu pengguna lokal pada perangkat utama. Pengguna ingin menambah batch cepat, scan label, mengonfirmasi OCR, melihat batch terdekat, menerima pengingat, melihat modal berisiko, memperbarui jumlah/status, memperoleh saran tindakan, mensimulasikan diskon, dan mencari/filter stok.

## 4. Persyaratan fungsional
| ID | Kebutuhan | Kriteria |
| FR-01 | CRUD batch | Validasi dan simpan lokal |
| FR-02 | OCR | Kamera/galeri menghasilkan teks atau error tertangani |
| FR-03 | Parser | Nol/satu/beberapa kandidat; tidak menebak format ambigu |
| FR-04 | Konfirmasi | Kandidat tidak tersimpan sebelum dikonfirmasi |
| FR-05 | Manual | Date picker selalu tersedia |
| FR-06 | Dashboard | Ringkasan urgensi dan daftar prioritas |
| FR-07 | Sorting/filter | Tanggal, nama, status, kategori, urgensi |
| FR-08 | Status/kuantitas | AVAILABLE/SOLD_OR_USED/DISCARDED; tidak negatif |
| FR-09 | Loss Monitor | Qty × harga beli untuk AVAILABLE |
| FR-10 | Reminder | Lokal, deduplikasi, batch nonaktif dikecualikan |
| FR-11 | Recommendation | Aturan menurut hari, kategori, jumlah, status, harga |
| FR-12 | Promo calculator | Harga promo dan margin jika data tersedia |
| FR-13 | Entitlement | Pembelian valid membuka semua fitur MVP |
| FR-14 | Direct sale | Prosedur pencatatan pembelian/aktivasi APK |

## 5. Aturan domain dan Recommendation Engine
Status tanggal memakai LocalDate dan zona waktu lokal:
- EXPIRED: expiryDate < today.
- DUE_TODAY: sama dengan hari ini.
- NEAR_1_3: 1–3 hari.
- NEAR_4_7: 4–7 hari.
- ATTENTION_8_14: 8–14 hari.
- NORMAL: lebih dari 14 hari.
Ini kategori prioritas, bukan jaminan keamanan pangan.

Expiry Loss Monitor: `remainingValue = remainingQuantity × purchasePricePerUnit` untuk AVAILABLE. Harga kosong ditampilkan “belum diisi”, bukan nol. SOLD_OR_USED dan DISCARDED tidak dihitung.

**Input engine:** category (FROZEN_FOOD, BAKERY, PACKAGED_BEVERAGE, PACKAGED_SNACK, OTHER), daysLeft, remainingQuantity/unit, purchasePricePerUnit opsional, normalSellingPricePerUnit opsional, status, dan catatan/konfirmasi kondisi penyimpanan.

**Urutan keputusan:**
1. Jika status bukan AVAILABLE atau jumlah nol: jangan tampilkan saran penjualan.
2. Jika daysLeft < 0: hanya tampilkan hentikan penjualan dan keluarkan dari stok tersedia. Tidak ada diskon, konsumsi, atau olah ulang.
3. Jika daysLeft == 0: peringatan pemeriksaan label/kebijakan; jangan mendorong penundaan atau penjualan setelah tanggal.
4. Jika masih sebelum tanggal: saran berdasarkan ambang dan kategori.
5. Jika harga jual normal dan harga beli ada: simulasi promo; peringatkan margin kotor <= 0.
6. Saran tidak mengubah harga, kuantitas, atau status otomatis.

| Days left | Label | Arah saran |
| >14 | Rotasi normal | FEFO/pantau |
| 8–14 | Perhatian | Display/restock |
| 4–7 | Promosi | Diskon terbatas/bundling |
| 1–3 | Mendesak | Promosi segera hanya jika label dan penyimpanan dipatuhi |
| 0 | Hari H | Pemeriksaan ketat |
| <0 | Lewat tanggal | Hentikan penjualan |

Kategori: frozen food—FEFO/display/bundling/multi-unit; jangan thaw/refreeze. Bakery—promo hari berjalan/bundling/evaluasi produksi; jangan menyarankan olah ulang otomatis. Minuman kemasan—multi-unit/bundling/display; jangan buka/kemas ulang. Snack—diskon/bundling/penawaran grosir. OTHER—FEFO dan evaluasi generik. Definisi kuantitas “tinggi” dan diskon adalah parameter yang harus diuji lewat pilot, bukan angka universal.

**Kalkulator:** `promoPrice = normalSellingPrice × (1 − discountRate)`; `grossMargin = promoPrice − purchasePricePerUnit`. Contoh Rp15.000 normal, Rp10.000 beli, diskon 10% → Rp13.500 promo, Rp3.500 margin kotor/unit sebelum biaya lain. Harga jual kosong: jangan hitung promo. Harga beli kosong: jangan hitung margin. Margin nol/negatif: warning. Gunakan Long/BigDecimal untuk rupiah dan pembulatan tampilan yang konsisten.

**Guardrail pangan:** ikuti tanggal dan petunjuk penyimpanan label; batch lewat tanggal tidak mendapat rekomendasi jual/konsumsi/olah ulang; jangan memperpanjang tanggal akibat diskon, pemasakan, pembekuan, atau pengemasan ulang. Aplikasi tidak mengukur suhu/kondisi mikrobiologis dan tidak menjamin keamanan.

## 6. Persyaratan nonfungsional
Offline-first; foto diproses lokal; validasi domain; data tanggal konsisten; kuantitas tidak negatif; harga Rupiah integer; Room migration; daftar ratusan batch responsif; UI aksesibel; status tidak hanya dibedakan warna; izin kamera/notifikasi diminta saat relevan; jelaskan risiko kehilangan data lokal. Tetapkan minSdk/dependency setelah verifikasi dokumentasi saat implementasi.

## 7. Stack dan arsitektur
Kotlin, Android Studio, Jetpack Compose, MVVM + Repository, Room, Coroutines/Flow, ML Kit Text Recognition, CameraX atau Photo Picker, WorkManager, Android Notification APIs. Untuk Play Store gunakan mekanisme billing satu kali yang sesuai ketentuan terkini.

```text
Compose UI -> ViewModel -> Use cases/domain rules -> Repository -> Room DAO
Camera/Gallery -> InputImage -> ML Kit -> OCR text -> Date parser
 -> candidate date -> user confirmation -> ViewModel -> Room
Room -> Dashboard / Filter / Risk Monitor / Recommendation Engine
WorkManager -> Reminder Worker -> Notification
Purchase provider/direct activation -> Entitlement -> Feature access
```

## 8. Skema Room
Satu tabel stock_batches cukup untuk MVP; tambahkan kategori dan harga jual normal.

```kotlin
@Entity(tableName = "stock_batches")
data class StockBatchEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val productName: String,
  val category: String = "OTHER",
  val quantity: Double,
  val unit: String,
  val expiryEpochDay: Long,
  val purchasePricePerUnit: Long?,
  val normalSellingPricePerUnit: Long? = null,
  val status: String = "AVAILABLE",
  val dateEntryMethod: String? = null,
  val notes: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
```
Jika unit selalu utuh, gunakan Int. Harga disimpan sebagai Long Rupiah. Siapkan migration dari skema lama dan jangan menghapus data pengguna saat upgrade. Histori tindakan/rekomendasi dapat menjadi tabel fase berikut.

## 9. Package dan layar
Package: `data/local`, `data/repository`, `data/billing`, `data/license`, `domain/model`, `domain/usecase`, `domain/recommendation`, `ml`, `notifications`, `ui/dashboard`, `ui/stocklist`, `ui/addbatch`, `ui/batchdetail`, `ui/recommendation`, `ui/purchase`.

Layar: onboarding/purchase; dashboard; tambah batch; daftar stok; detail batch; rekomendasi; pengaturan. Layar rekomendasi menampilkan alasan, data pemicu, saran, simulasi diskon, warning margin, dan disclaimer.

## 10. Reminder
WorkManager memeriksa misalnya sekali sehari. Ambang awal 7 hari, 3 hari, hari H, dan setelah tanggal dengan frekuensi terbatas. Gunakan ID/tag stabil untuk deduplikasi. Batch nonaktif tidak menerima reminder. Setelah tanggal, notifikasi berupa peringatan hentikan penjualan/periksa stok, bukan promosi.

## 11. Pembelian, lisensi, distribusi
Pembelian satu kali membuka semua fitur MVP. **Google Play:** ikuti mekanisme billing satu kali, validasi purchase state, acknowledge/restore/re-query dan refund sesuai dokumentasi/kebijakan terkini; jangan log token sensitif. **Penjualan langsung:** rekomendasi awal satu pembelian per usaha untuk satu perangkat utama; catat transaksi, versi, dan perangkat. Kode aktivasi lokal hanya administrasi, bukan DRM kuat. Siapkan prosedur ganti perangkat. Tidak ada server entitlement/cloud untuk MVP. Jika kontrol lisensi lintas perangkat yang kuat diperlukan, backend dan biaya berulang menjadi fase tersendiri.

**Dukungan:** bug fix dan bantuan penggunaan dasar selama versi didukung; pembaruan kompatibilitas yang wajar; tanpa janji dukungan seumur hidup. Fitur mayor/cloud/multi-user/integrasi dapat dijual terpisah. Tampilkan ketentuan sebelum pembelian.

## 12. Privasi dan integritas
Tidak perlu akun/server. Jangan log foto atau data bisnis berlebihan. Foto tidak diunggah. Validasi di domain/ViewModel. Jelaskan data lokal dapat hilang bila aplikasi/perangkat dihapus/rusak. Siapkan Room migration. Jangan menyatakan pangan aman hanya berdasarkan tanggal.

## 13. Pengujian
Unit test: parser valid/invalid/ambigu/multiple; batas 14/8/7/4/3/1/0/-1; rules per kategori; status nonaktif/jumlah nol; expired tidak pernah dapat promo; kalkulasi margin positif/nol/negatif/harga kosong; risk value; kuantitas negatif.

UI/perangkat: tambah manual; scan dan konfirmasi; OCR gagal; beberapa kandidat; edit tanggal/kategori/harga; kurangi kuantitas; status; rekomendasi; promo; search/filter; izin kamera/notifikasi ditolak; aplikasi restart; foto blur/gelap/miring/pantulan; pembelian/restore/aktivasi untuk jalur yang diuji.

## 14. Acceptance criteria
- Batch valid tersimpan dan bertahan setelah aplikasi ditutup.
- OCR menghasilkan teks atau error tertangani.
- Kandidat tidak disimpan sebelum konfirmasi; manual fallback berfungsi.
- Batch tanggal berbeda dicatat terpisah.
- Dashboard sorting tanggal terdekat.
- Batch nonaktif tidak dihitung sebagai stok tersedia dan tidak mendapat saran jual.
- Expiry Loss Monitor memakai harga dan kuantitas valid.
- Reminder tidak duplikatif; batch nonaktif dikecualikan.
- Rekomendasi menyebut alasan dan tidak menjanjikan keamanan.
- Batch expired tidak menerima saran jual/diskon/olah ulang.
- Margin nol/negatif memunculkan warning; harga kosong tidak ditebak.
- Tidak ada perubahan harga/status otomatis.
- Pembelian/aktivasi valid membuka seluruh fitur MVP.
- Batasan offline, lisensi, dan dukungan dijelaskan.

## 15. Roadmap 4–6 minggu
| Minggu | Pekerjaan | Definition of done |
| 1 | Setup, Compose, Room, MVVM, CRUD | Batch tersimpan/edit/hapus/persisten |
| 2 | Kamera/galeri, ML Kit, parser, konfirmasi | OCR tidak auto-save; manual fallback |
| 3 | Status, sorting, Loss Monitor, filter | Dashboard dan nilai risiko konsisten |
| 4 | Reminder, Recommendation Engine, promo | Unit test rules dan kalkulator lolos |
| 5 | UX, pilot, perbaikan, prototipe purchase | Masukan pengguna dan jalur demo siap |
| 6 | Regression, dokumentasi, demo | Alur demo dan batasan siap |

Jika hanya empat minggu, tunda riwayat/CSV/integrasi distribusi penuh; jangan mengorbankan konfirmasi OCR, integritas data, guardrail expired, dan dashboard.

## 16. Risiko dan keputusan sebelum rilis
Risiko: OCR salah (konfirmasi/manual); parser ambigu (jangan menebak); rekomendasi salah (pilot); diskon di bawah modal (warning); salah penggunaan pangan (guardrail); notifikasi terhambat OS (uji izin); data lokal hilang (jelaskan batas); APK disalin (jangan klaim DRM).

Sebelum rilis: harga final; aturan refund Play; prosedur ganti perangkat; periode dukungan; ambang kuantitas tinggi/diskon; format tanggal yang didukung; minSdk/target SDK/dependency; kebijakan privasi dan ketentuan penggunaan.
