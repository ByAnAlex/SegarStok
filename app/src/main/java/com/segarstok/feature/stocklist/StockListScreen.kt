package com.segarstok.feature.stocklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.BatchStatusBadge
import com.segarstok.core.design.component.EmptyStateContainer
import com.segarstok.core.design.component.LoadingStateContainer
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.Spacing
import com.segarstok.core.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    onNavigateToAddBatch: () -> Unit,
    onNavigateToBatchDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockListViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    val categories = listOf("Semua", "Frozen Food", "Roti / Bakery", "Minuman Kemasan", "Makanan Ringan / Snack", "Lainnya")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Stok Batch") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddBatch) {
                Text("+ Batch", modifier = Modifier.padding(horizontal = Spacing.medium))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                placeholder = { Text("Cari nama produk batch...") },
                singleLine = true
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingStateContainer()
            } else if (uiState.batches.isEmpty()) {
                EmptyStateContainer(
                    title = "Tidak Ada Batch Ditemukan",
                    message = if (uiState.searchQuery.isNotBlank()) "Tidak ada batch stok yang sesuai pencarian '${uiState.searchQuery}'" else "Belum ada catatan batch stok tersimpan di database.",
                    actionLabel = "+ Tambah Batch baru",
                    onActionClick = onNavigateToAddBatch
                )
            } else {
                // Batch List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    contentPadding = PaddingValues(bottom = Spacing.extraLarge)
                ) {
                    items(uiState.batches) { item ->
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
                                    BatchStatusBadge(status = item.status)
                                }
                                Text(
                                    text = "Kategori: ${item.category.label} • Jumlah: ${item.quantity} ${item.unit}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kedaluwarsa: ${item.expiryDate}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    UrgencyBadge(urgencyLevel = item.urgencyLevel())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
