package com.segarstok.feature.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.Spacing
import com.segarstok.core.ui.ViewModelFactory
import com.segarstok.domain.recommendation.BatchRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onNavigateToBatchDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecommendationViewModel = viewModel(
        factory = ViewModelFactory.getInstance(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Food Safety Guardrail Banner
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
                                       "• Produk lewat tanggal WAJIB dihentikan penjualannya & tidak boleh didiskon.\n" +
                                       "• Aplikasi tidak mengubah harga atau stok secara otomatis tanpa persetujuan Anda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Discount Rate Selector Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                        Text(
                            text = "Simulasi Tingkat Diskon Promosi:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            val rates = listOf(0.10, 0.20, 0.30)
                            rates.forEach { rate ->
                                val percentText = "${(rate * 100).toInt()}%"
                                val isSelected = uiState.selectedDiscountRate == rate
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onDiscountRateChange(rate) },
                                    label = { Text("Diskon $percentText") }
                                )
                            }
                        }
                    }
                }

                if (uiState.recommendations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.large),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada stok aktif yang memerlukan rekomendasi.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.recommendations) { item ->
                        RecommendationCardItem(
                            item = item,
                            onClick = { onNavigateToBatchDetail(item.batchId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCardItem(
    item: BatchRecommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
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

            val daysText = if (item.daysLeft < 0) {
                "Kedaluwarsa (${-item.daysLeft} hari lalu)"
            } else if (item.daysLeft == 0L) {
                "Hari H Kedaluwarsa"
            } else {
                "Sisa ${item.daysLeft} Hari"
            }

            Text(
                text = "${item.category.label} • ${item.quantity} ${item.unit} • $daysText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text(
                text = "Arah Tindakan: ${item.actionDirection}",
                style = MaterialTheme.typography.labelMedium,
                color = if (item.isExpiredGuardrail) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Text(
                text = item.detailedAdvice,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "💡 Panduan Kategori (${item.category.label}): ${item.categoryAdvice}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            item.promoSimulation?.let { sim ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.small)) {
                        Text(
                            text = sim.formattedText,
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (sim.marginWarning != null) {
                            Spacer(modifier = Modifier.height(Spacing.extraSmall))
                            Text(
                                text = sim.marginWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
