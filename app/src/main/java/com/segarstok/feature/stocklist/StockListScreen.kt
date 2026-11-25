package com.segarstok.feature.stocklist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.BatchStatusBadge
import com.segarstok.core.design.component.EmptyStateContainer
import com.segarstok.core.design.component.LoadingStateContainer
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.*
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
    val statuses = listOf("Semua", "Tersedia", "Terjual")

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Stok Batch",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Slate900
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasBackground,
                    titleContentColor = Slate900
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddBatch,
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Batch",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        text = "Cari nama produk batch...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate400,
                            fontSize = 14.sp
                        )
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = PrimaryTeal
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { cat ->
                    val selected = uiState.selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White,
                            containerColor = CardSurface,
                            labelColor = Slate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = PrimaryTeal,
                            borderColor = CardBorder,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(statuses) { st ->
                    val selected = uiState.selectedStatus == st ||
                            (st == "Terjual" && uiState.selectedStatus == "Terjual / Dipakai")
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val mapped = if (st == "Terjual") "Terjual / Dipakai" else st
                            viewModel.onStatusChange(mapped)
                        },
                        label = {
                            Text(
                                text = st,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White,
                            containerColor = CardSurface,
                            labelColor = Slate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = PrimaryTeal,
                            borderColor = CardBorder,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                LoadingStateContainer()
            } else if (uiState.batches.isEmpty()) {
                val hasFilter = uiState.searchQuery.isNotBlank() ||
                        uiState.selectedCategory != "Semua" ||
                        uiState.selectedStatus != "Semua"
                EmptyStateContainer(
                    title = if (hasFilter) "Tidak Ada Batch Cocok" else "Belum Ada Batch Stok",
                    message = if (hasFilter) "Tidak ada batch yang sesuai dengan kombinasi pencarian dan filter saat ini."
                    else "Belum ada catatan batch stok tersimpan di database.",
                    actionLabel = "+ Tambah Batch Baru",
                    onActionClick = onNavigateToAddBatch
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(uiState.batches) { item ->
                        val daysLeft = item.daysLeft()
                        val expText = when {
                            daysLeft < 0 -> "Exp ${item.expiryDate} · lewat ${-daysLeft} hari"
                            daysLeft == 0L -> "Exp ${item.expiryDate} · Hari Ini"
                            else -> "Exp ${item.expiryDate} · sisa $daysLeft hari"
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToBatchDetail(item.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Row 1: Product name + status badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Slate900
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BatchStatusBadge(status = item.status)
                                }

                                // Row 2: Category · quantity
                                Text(
                                    text = "${item.category.label} · ${formatQuantity(item.quantity, item.unit)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        color = Slate500
                                    )
                                )

                                // Row 3: Expiry date + urgency badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = expText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Slate500
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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

private fun formatQuantity(quantity: Double, unit: String): String {
    val integerPart = quantity.toLong()
    return if (quantity == integerPart.toDouble()) {
        "$integerPart $unit"
    } else {
        "$quantity $unit"
    }
}
