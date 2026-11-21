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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.BatchStatusBadge
import com.segarstok.core.design.component.EmptyStateContainer
import com.segarstok.core.design.component.LoadingStateContainer
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.Spacing
import com.segarstok.core.ui.ViewModelFactory
import com.segarstok.domain.model.SortOption

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
    val statuses = listOf("Semua", "Tersedia", "Terjual / Dipakai", "Dibuang")
    val urgencies = listOf("Semua", "Mendesak", "Hari H", "Lewat Tanggal", "Mendekati", "Perhatian", "Normal")

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Stok Batch") },
                actions = {
                    Box {
                        TextButton(onClick = { showSortMenu = true }) {
                            Text("Urutkan: ${uiState.selectedSortOption.label}")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        viewModel.onSortOptionChange(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
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

            // Filter Chips Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Category Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.onCategoryChange(cat) },
                            label = { Text(cat, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                // Status & Urgency Filter Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    item {
                        Text("Status: ", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 10.dp))
                    }
                    items(statuses) { st ->
                        FilterChip(
                            selected = uiState.selectedStatus == st,
                            onClick = { viewModel.onStatusChange(st) },
                            label = { Text(st, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text("Urgensi: ", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 10.dp))
                    }
                    items(urgencies) { urg ->
                        FilterChip(
                            selected = uiState.selectedUrgency == urg,
                            onClick = { viewModel.onUrgencyChange(urg) },
                            label = { Text(urg, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            if (uiState.isLoading) {
                LoadingStateContainer()
            } else if (uiState.batches.isEmpty()) {
                val hasFilter = uiState.searchQuery.isNotBlank() || uiState.selectedCategory != "Semua" || uiState.selectedStatus != "Semua" || uiState.selectedUrgency != "Semua"
                EmptyStateContainer(
                    title = if (hasFilter) "Tidak Ada Batch Cocok" else "Belum Ada Batch Stok",
                    message = if (hasFilter) "Tidak ada batch yang sesuai dengan kombinasi pencarian dan filter saat ini." else "Belum ada catatan batch stok tersimpan di database.",
                    actionLabel = "+ Tambah Batch Baru",
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
                        val daysLeft = item.daysLeft()
                        val daysText = when {
                            daysLeft < 0 -> "Lewat ${-daysLeft} hari"
                            daysLeft == 0L -> "Hari Ini"
                            else -> "Sisa $daysLeft hari"
                        }

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
                                        text = "Kedaluwarsa: ${item.expiryDate} ($daysText)",
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
