package com.segarstok.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segarstok.core.design.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Informasi Aplikasi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // App Info Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    Text(text = "SegarStok", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Aplikasi Pencatat Stok Batch & Pemantau Kedaluwarsa UMKM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(text = "Versi MVP: 1.0.0", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Entitlement & License Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    Text(
                        text = "Status Pembelian Satu Kali (One-Time Purchase)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = "Status: Mode Demo / Aktif (Seluruh fitur MVP terbuka)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "Catatan: Pembelian satu kali berlaku per usaha untuk satu perangkat utama.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Data Privacy & Offline Notice
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    Text(text = "🔒 Privasi Data & Mode Offline", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "SegarStok bekerja secara 100% offline-first. Data stok dan pemindaian foto label diproses secara lokal di perangkat Anda. Tidak ada data bisnis atau foto label yang diunggah ke server cloud.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Support & Disclaimer Notice
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    Text(text = "ℹ️ Batasan & Dukungan Layanan", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "• Aplikasi ini membantu mencatat tanggal dan memberikan saran keputusan operasional.\n" +
                               "• Pengguna wajib memeriksa secara fisik kondisi label dan produk sebelum dijual.\n" +
                               "• Bantuan teknis & perbaikan bug diberikan selama versi didukung.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
