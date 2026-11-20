package com.segarstok.feature.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.Spacing
import com.segarstok.domain.model.UrgencyLevel

data class DummyRecommendationItem(
    val id: Long,
    val productName: String,
    val category: String,
    val quantity: String,
    val daysLeftText: String,
    val urgencyLevel: UrgencyLevel,
    val recommendationText: String,
    val promoSimText: String?,
    val marginWarning: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onNavigateToBatchDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dummyRecommendations = listOf(
        DummyRecommendationItem(
            id = 1,
            productName = "Nugget Ayam 500g",
            category = "Frozen Food",
            quantity = "12 pack",
            daysLeftText = "Sisa 5 Hari",
            urgencyLevel = UrgencyLevel.NEAR,
            recommendationText = "Diskon Terbatas 10% / Bundling. Tawarkan ke pelanggan prioritas. Pertahankan penyimpanan beku.",
            promoSimText = "Harga Promo: Rp 13.500 | Est. Margin Kotor: Rp 3.500/unit",
            marginWarning = null
        ),
        DummyRecommendationItem(
            id = 2,
            productName = "Roti Isi Cokelat",
            category = "Bakery",
            quantity = "5 pcs",
            daysLeftText = "Sisa 1 Hari",
            urgencyLevel = UrgencyLevel.URGENT,
            recommendationText = "Promosi Segera / Paket Hemat Hari Berjalan. Evaluasi jumlah produksi berikutnya.",
            promoSimText = "Harga Promo: Rp 9.000 | Est. Margin Kotor: Rp 1.000/unit",
            marginWarning = null
        ),
        DummyRecommendationItem(
            id = 3,
            productName = "Sosis Sapi 1kg",
            category = "Frozen Food",
            quantity = "8 pack",
            daysLeftText = "Sudah Lewat Tanggal",
            urgencyLevel = UrgencyLevel.EXPIRED,
            recommendationText = "⛔ Hentikan penjualan segera & keluarkan dari stok tersedia. Jangan dijual, didiskon, atau dibekukan ulang.",
            promoSimText = null,
            marginWarning = "Produk telah melewati tanggal kedaluwarsa label."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Action Recommendation") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Food Safety & Rule Transparency Guardrail Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Text(
                            text = "💡 Prinsip Rekomendasi & Keamanan Pangan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = "• Rekomendasi bersifat saran operasional berbasis aturan transparan.\n" +
                                   "• Produk lewat tanggal WAJIB dihentikan penjualannya.\n" +
                                   "• Aplikasi tidak mengubah harga atau stok secara otomatis tanpa persetujuan Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Recommendations List Items
            items(dummyRecommendations) { item ->
                Card(
                    onClick = { onNavigateToBatchDetail(item.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.productName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            UrgencyBadge(urgencyLevel = item.urgencyLevel)
                        }

                        Text(
                            text = "${item.category} • ${item.quantity} • ${item.daysLeftText}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Text(
                            text = "Saran Tindakan:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.recommendationText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (item.promoSimText != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.promoSimText,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(Spacing.small)
                                )
                            }
                        }

                        if (item.marginWarning != null) {
                            Text(
                                text = item.marginWarning,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
