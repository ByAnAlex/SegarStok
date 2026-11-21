package com.segarstok.feature.dashboard

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.EmptyStateContainer
import com.segarstok.core.design.component.LoadingStateContainer
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.Spacing
import com.segarstok.core.ui.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddBatch: () -> Unit,
    onNavigateToBatchDetail: (Long) -> Unit,
    onNavigateToRecommendations: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SegarStok — Dashboard Stok") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingStateContainer(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Header Card - Expiry Risk Summary (Loss Monitor)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                        ) {
                            Text(
                                text = "Expiry Loss Monitor (Estimasi Modal Berisiko)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = currencyFormat.format(uiState.totalRiskValue),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Modal Berisiko Aktif:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = currencyFormat.format(uiState.activeRiskValue),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Modal Berisiko Lewat Tgl:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = currencyFormat.format(uiState.expiredRiskValue),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            if (uiState.missingPriceBatchesCount > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ℹ️ ${uiState.missingPriceBatchesCount} batch belum diisi harga beli per unit.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Urgency Breakdown Grid Section
                item {
                    Text(text = "Ringkasan Urgensi Kedaluwarsa", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            StatCard(
                                title = "Mendesak (1-3H)",
                                count = uiState.urgentCount,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Hari H (0H)",
                                count = uiState.dueTodayCount,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Lewat Tgl (<0H)",
                                count = uiState.expiredCount,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            StatCard(
                                title = "Mendekati (4-7H)",
                                count = uiState.nearCount,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Perhatian (8-14H)",
                                count = uiState.attentionCount,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Normal (>14H)",
                                count = uiState.normalCount,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Quick Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Button(
                            onClick = onNavigateToAddBatch,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Catat Batch / Scan")
                        }
                        OutlinedButton(
                            onClick = onNavigateToRecommendations,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Saran Tindakan")
                        }
                    }
                }

                // Urgent Batches Section Title
                item {
                    Text(
                        text = "Batch Prioritas (Kedaluwarsa Terdekat)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (uiState.priorityBatches.isEmpty()) {
                    item {
                        EmptyStateContainer(
                            title = "Belum Ada Batch Stok Tersedia",
                            message = "Mulai mencatat batch stok pertama Anda untuk memantau tanggal kedaluwarsa.",
                            actionLabel = "+ Tambah Batch",
                            onActionClick = onNavigateToAddBatch
                        )
                    }
                } else {
                    items(uiState.priorityBatches) { item ->
                        val daysLeft = item.daysLeft()
                        val daysLeftText = when {
                            daysLeft < 0 -> "Lewat ${-daysLeft} hari (${item.expiryDate})"
                            daysLeft == 0L -> "Kedaluwarsa HARI INI (${item.expiryDate})"
                            else -> "Sisa $daysLeft hari (${item.expiryDate})"
                        }

                        Card(
                            onClick = { onNavigateToBatchDetail(item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${item.category.label} • ${item.quantity} ${item.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = daysLeftText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                UrgencyBadge(urgencyLevel = item.urgencyLevel())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$count", style = MaterialTheme.typography.titleLarge, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall)
        }
    }
}
