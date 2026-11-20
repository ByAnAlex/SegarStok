# SEGARSTOK — PENJELASAN PRODUK DAN RENCANA BISNIS
Versi final 3.0 | 20 November 2026

## 1. Ringkasan eksekutif
SegarStok adalah aplikasi Android berbasis Kotlin untuk membantu UMKM makanan dan minuman mencatat stok per batch, memantau tanggal kedaluwarsa, dan menentukan tindakan penjualan yang perlu dipertimbangkan sebelum tanggal pada label terlewati. Pembeda utamanya adalah pemindaian teks label dengan Google ML Kit Text Recognition. Hasil OCR diproses parser tanggal, lalu wajib diperiksa dan dikonfirmasi atau dikoreksi pengguna. Jika OCR gagal atau ambigu, input manual tetap tersedia.

Konsep final menambahkan **Smart Action Recommendation**, rekomendasi strategi penjualan berbasis aturan transparan menurut kategori produk, sisa hari, kuantitas, harga beli, dan harga jual normal bila diisi. Contoh saran: FEFO/prioritas display, diskon terbatas, bundling, penawaran beberapa unit, dan evaluasi pembelian berikutnya. Aplikasi tidak mengubah harga secara otomatis dan bukan sistem prediksi AI.

Model bisnis adalah **pembelian satu kali di muka**; seluruh fitur MVP terbuka setelah pembelian. Distribusi dilakukan melalui Google Play Store dan penjualan langsung ke UMKM melalui demo serta instalasi APK. MVP tetap offline-first tanpa akun, server, cloud sync, atau biaya langganan fungsi inti.

**Posisi produk:** aplikasi pengelolaan stok dan rekomendasi penjualan produk makanan/minuman untuk UMKM.

## 2. Latar belakang dan masalah
Penjual frozen food, toko makanan kemasan, bakery rumahan, dan katering kecil dapat mengelola stok dengan ingatan, buku, spreadsheet, atau aplikasi inventori umum. Ketika produk yang sama datang pada waktu berbeda, tanggal per batch dapat terlewat. Pencatatan manual juga memakan waktu. Akibatnya pemilik terlambat memprioritaskan batch, menawarkan promosi, mengurangi pembelian berikutnya, atau mengeluarkan produk yang sudah melewati tanggal.

SegarStok tidak menggantikan POS, akuntansi, ERP, inspeksi mutu, atau sistem keamanan pangan. Fokusnya adalah pencatatan batch dan dukungan keputusan operasional terkait tanggal.

## 3. Konsep batch dan alur
Batch adalah kelompok unit produk dengan tanggal kedaluwarsa sama. Sepuluh bungkus bertanggal sama dapat dicatat sebagai satu batch dan satu label representatif dipindai. Tanggal berbeda harus dibuat sebagai batch terpisah.

Alur: isi data batch → scan label kamera/galeri → ML Kit mengekstrak teks → parser mencari kandidat → pengguna memilih dan mengonfirmasi/mengoreksi → simpan ke Room → dashboard mengurutkan urgensi → pengguna melihat rekomendasi → kuantitas/status diperbarui setelah terjual, dipakai, atau dibuang.

OCR bukan bukti bahwa teks yang terbaca pasti tanggal kedaluwarsa. Tanggal produksi dan kedaluwarsa bisa muncul berdekatan; keputusan akhir tetap pada pengguna.

## 4. Target pengguna dan manfaat
Segmen awal: penjual frozen food, toko makanan/minuman kemasan kecil, bakery rumahan, dan katering kecil. Persona awal adalah pemilik satu lokasi dengan satu sampai tiga staf dan puluhan hingga ratusan batch.

**Pemilik/pengelola:** melihat batch prioritas, estimasi modal berisiko, pengingat, rekomendasi tindakan, dan simulasi margin promo. **Staf:** lebih mudah mencatat tanggal, memeriksa batch, dan memperbarui kuantitas/status. **Pelanggan/mitra:** pengelolaan batch yang lebih tertib berpotensi membantu rotasi stok; manfaat tidak langsung ini perlu divalidasi.

## 5. Fitur aplikasi
- CRUD stok per batch dan detail batch.
- Scan kamera/galeri dengan ML Kit Text Recognition.
- Parser tanggal, konfirmasi wajib, koreksi/input manual.
- Dashboard prioritas, sorting tanggal terdekat, pencarian dan filter.
- Smart Reminder lokal.
- Status AVAILABLE, SOLD_OR_USED, DISCARDED.
- Expiry Loss Monitor: kuantitas tersisa × harga beli per unit.
- Smart Action Recommendation berbasis aturan.
- Simulasi harga promo dan margin kotor bila harga jual normal tersedia.

Expiry Loss Monitor menampilkan **estimasi nilai modal berisiko**, bukan kerugian aktual. Batch tanpa harga beli ditandai belum ada harga; batch nonaktif tidak dihitung sebagai stok tersedia.

## 6. Smart Action Recommendation
Ambang awal untuk diuji (bukan standar regulasi):
| Sisa hari | Prioritas | Contoh tindakan |
| >14 | Rotasi normal | FEFO dan pantau stok |
| 8–14 | Perhatian | Prioritaskan display, evaluasi restock |
| 4–7 | Promosi terencana | Diskon terbatas, bundling, tawarkan ke pelanggan |
| 1–3 | Mendesak | Pertimbangkan promosi segera jika label/penyimpanan dipatuhi |
| 0 | Hari H | Pemeriksaan ketat, patuhi label dan kebijakan |
| <0 | Lewat tanggal | Hentikan penjualan, keluarkan dari stok tersedia |

Saran kategori: frozen food—FEFO, display, bundling atau multi-unit; jangan sarankan thaw/refreeze. Bakery—promo hari berjalan, bundling, evaluasi jumlah produksi; pengolahan ulang bukan saran umum. Minuman kemasan—promo multi-unit/bundling, jangan membuka atau mengemas ulang. Makanan ringan—diskon terbatas, bundling, penawaran grosir. Jumlah stok besar dapat memicu saran bundling; ambang jumlah perlu divalidasi lewat pilot.

## 7. Simulasi harga promo
`harga promo = harga jual normal × (1 − diskon)`; `margin kotor/unit = harga promo − harga beli/unit`.

Contoh: harga beli Rp10.000, harga normal Rp15.000, diskon 10%, harga promo Rp13.500, margin kotor Rp3.500 sebelum biaya lain. Jika margin nol/negatif, tampilkan peringatan. Jika harga jual normal kosong, jangan mengarang harga promo. Margin kotor bukan laba bersih; aplikasi tidak mengubah harga otomatis.

## 8. Batas keamanan pangan
SegarStok bukan alat penentu keamanan pangan. Pengguna wajib mengikuti tanggal dan petunjuk penyimpanan pada label. Produk yang sudah melewati tanggal tidak direkomendasikan untuk dijual, dikonsumsi, atau diolah ulang; hentikan penjualan dan keluarkan dari stok tersedia. Jangan memperpanjang tanggal karena diskon, pemasakan, pembekuan, atau pengemasan ulang. Promosi sebelum tanggal hanya dipertimbangkan jika kondisi kemasan dan penyimpanan sesuai serta aturan yang berlaku dipenuhi. Rekomendasi tidak menggantikan pemeriksaan kondisi produk.

## 9. Teknologi ML Kit
ML Kit mengenali teks, bukan memastikan suatu angka adalah tanggal kedaluwarsa. Kualitas cetak, cahaya, pantulan, sudut foto, format, dan tanggal produksi memengaruhi hasil. Pipeline: gambar → InputImage → Text Recognition → teks → parser → kandidat → konfirmasi pengguna → Room. Uji label jelas, blur, miring, gelap, pantulan, dan label dengan beberapa tanggal. Jangan klaim akurasi 100%.

## 10. Model bisnis, distribusi, lisensi
**Pembelian satu kali:** seluruh fitur MVP terbuka setelah pembelian. Tidak ada subscription untuk fitur inti. Model ini sesuai produk aplikasi yang dijual pengembang/freelancer kepada UMKM. Offline-first menekan biaya server, tetapi pengembangan, pemasaran, dukungan, distribusi, dan pembaruan tetap memiliki biaya.

**Google Play Store:** pengguna menemukan dan membeli/menginstal aplikasi melalui mekanisme Play yang tersedia. Implementasi billing, entitlement, restore, refund, dan publikasi harus mengikuti kebijakan Google Play terbaru. Pembelian Play tidak otomatis dapat dipindahkan ke APK langsung.

**Penjualan langsung:** pengembang melakukan demo, menerima pembayaran, lalu memberikan APK resmi dan panduan instalasi. Catat bukti pembelian dan versi aplikasi. Kode aktivasi lokal dapat dipakai untuk administrasi, tetapi tanpa server tidak boleh diklaim sebagai anti-pembajakan kuat.

**Rekomendasi lisensi awal:** satu pembelian per usaha untuk satu perangkat utama. Perangkat tambahan dapat memerlukan lisensi tambahan atau paket khusus. Kebijakan ganti perangkat perlu ditulis. Untuk Play, ikuti entitlement platform; untuk penjualan langsung, catat usaha, transaksi, tanggal, versi, dan perangkat secara proporsional.

**Dukungan:** perbaikan bug dan bantuan penggunaan dasar selama versi didukung; pembaruan kompatibilitas yang wajar. Tidak menjanjikan dukungan seumur hidup. Fitur besar, cloud, multi-user, atau integrasi dapat menjadi pembelian terpisah. Jelaskan kebijakan sebelum transaksi.

## 11. Harga dan validasi
Harga final belum ditetapkan. Uji beberapa titik harga melalui wawancara dan pilot berbayar. Tanyakan kesediaan membayar sekali, manfaat yang dianggap bernilai, dan nilai stok yang sering terlupakan/dibuang. Pertimbangkan kemampuan bayar, waktu yang dihemat, nilai stok berisiko, biaya pengembangan, demo, dukungan, pembaruan, dan alternatif kompetitor. Harga versi langganan lama bukan harga final.

Metrik pilot: waktu mencatat batch; proporsi kandidat OCR benar sebelum koreksi; batch aktif dan kelengkapan tanggal; rekomendasi yang dibuka/ditindaklanjuti; batch terjual sebelum tanggal atau dibuang; kesediaan membayar; permintaan bantuan dan bug.

## 12. Pemasaran, risiko, roadmap
Mulai dari segmen yang mudah dijangkau, demo memakai stok nyata, tunjukkan scan-konfirmasi-dashboard-rekomendasi, dan gunakan studi kasus hanya dengan izin serta data terukur.

Risiko utama: OCR salah (konfirmasi/manual); salah membedakan tanggal produksi (pilihan eksplisit); diskon merugikan (simulasi margin); rekomendasi tidak sesuai kategori (pilot); salah tafsir keamanan pangan (guardrail); kehilangan data lokal (jelaskan batasan); tidak ada kemauan membayar (uji harga); dukungan membebani pengembang (batas dukungan tertulis).

Roadmap MVP 4–6 minggu: minggu 1 CRUD/Room/UI; minggu 2 OCR/parser/konfirmasi; minggu 3 sorting/status/monitor; minggu 4 reminder/rekomendasi/kalkulator promo; minggu 5 pilot dan perbaikan; minggu 6 regression, dokumentasi, demo. Pasca validasi: akun, cloud, multi-user, multi-cabang, POS, dan paket multi-perangkat.
