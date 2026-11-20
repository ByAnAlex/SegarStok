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
                // Header Card - Expiry Risk Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.medium)) {
                            Text(
                                text = "Ringkasan Modal Berisiko",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(Spacing.extraSmall))
                            Text(
                                text = currencyFormat.format(uiState.totalRiskValue),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Modal dari batch berstatus AVAILABLE tersimpan di database",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Quick Stats Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.small),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${uiState.totalAvailableCount}", style = MaterialTheme.typography.titleLarge)
                                Text("Tersedia", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.small),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${uiState.urgentCount}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                                Text("Mendesak", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.small),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${uiState.expiredCount}", style = MaterialTheme.typography.titleLarge)
                                Text("Lewat Tgl", style = MaterialTheme.typography.labelMedium)
                            }
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
                        text = "Batch Prioritas Kedaluwarsa Terdekat",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (uiState.priorityBatches.isEmpty()) {
                    item {
                        EmptyStateContainer(
                            title = "Belum Ada Batch Stok",
                            message = "Mulai mencatat batch stok pertama Anda untuk memantau tanggal kedaluwarsa.",
                            actionLabel = "+ Tambah Batch",
                            onActionClick = onNavigateToAddBatch
                        )
                    }
                } else {
                    items(uiState.priorityBatches) { item ->
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
