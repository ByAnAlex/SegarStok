package com.segarstok.feature.batchdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    batchId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatchDetailViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current, batchId))
) {
    val uiState by viewModel.uiState.collectAsState()
    val batch = uiState.batch

    var showEditDialog by remember { mutableStateOf(false) }
    var showReduceDialog by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    if (showEditDialog && batch != null) {
        EditBatchDialog(
            batch = batch,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                viewModel.updateBatch(updated)
                showEditDialog = false
            }
        )
    }

    if (showReduceDialog && batch != null) {
        ReduceQuantityDialog(
            batch = batch,
            onDismiss = { showReduceDialog = false },
            onConfirm = { reduceAmount ->
                viewModel.reduceQuantity(reduceAmount)
                showReduceDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Batch #${batchId}") },
                actions = {
                    if (batch != null) {
                        TextButton(onClick = { showEditDialog = true }) {
                            Text("Edit Batch")
                        }
                    }
                },
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
        } else if (batch == null) {
            EmptyStateContainer(
                title = "Batch Tidak Ditemukan",
                message = "Catatan batch stok dengan ID #$batchId tidak ada atau telah dihapus.",
                actionLabel = "Kembali ke Daftar Stok",
                onActionClick = onNavigateBack,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.medium)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Main Product Info Card
                Card(modifier = Modifier.fillMaxWidth()) {
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
                                text = batch.productName,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            BatchStatusBadge(status = batch.status)
                        }

                        val daysLeft = batch.daysLeft()
                        val daysText = when {
                            daysLeft < 0 -> "Lewat ${-daysLeft} hari"
                            daysLeft == 0L -> "Hari Ini"
                            else -> "Sisa $daysLeft hari"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sisa Waktu: $daysText", style = MaterialTheme.typography.labelLarge)
                            UrgencyBadge(urgencyLevel = batch.urgencyLevel())
                        }

                        HorizontalDivider()

                        Text("Kategori: ${batch.category.label}", style = MaterialTheme.typography.bodyLarge)
                        Text("Sisa Kuantitas: ${batch.quantity} ${batch.unit}", style = MaterialTheme.typography.bodyLarge)
                        Text("Tanggal Kedaluwarsa: ${batch.expiryDate}", style = MaterialTheme.typography.bodyLarge)
                        Text("Metode Input Tanggal: ${batch.dateEntryMethod ?: "MANUAL"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (!batch.notes.isNullOrBlank()) {
                            Text("Catatan / Penyimpanan: ${batch.notes}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Expiry Risk Calculator Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Text(
                            text = "Expiry Loss Monitor",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = "Harga Beli per Unit: ${batch.purchasePricePerUnit?.let { currencyFormat.format(it) } ?: "Belum diisi"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Harga Jual Normal: ${batch.normalSellingPricePerUnit?.let { currencyFormat.format(it) } ?: "Belum diisi"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "Estimasi Modal Berisiko: ${batch.remainingRiskValue()?.let { currencyFormat.format(it) } ?: "Tidak dihitung (Status ${batch.status.label})"}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(Hanya dihitung untuk batch berstatus AVAILABLE)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Batch Status Actions
                Text("Tindakan Pengelolaan Stok", style = MaterialTheme.typography.titleMedium)

                if (batch.status == BatchStatus.AVAILABLE && batch.quantity > 0) {
                    Button(
                        onClick = { showReduceDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🛒 Catat Pemakaian / Penjualan Sebagian")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Button(
                        onClick = { viewModel.updateStatus(BatchStatus.SOLD_OR_USED) },
                        enabled = batch.status != BatchStatus.SOLD_OR_USED,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tandai Terjual Semua")
                    }
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(BatchStatus.DISCARDED) },
                        enabled = batch.status != BatchStatus.DISCARDED,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tandai Dibuang")
                    }
                }

                if (batch.status != BatchStatus.AVAILABLE) {
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(BatchStatus.AVAILABLE) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembalikan Status ke AVAILABLE (Tersedia)")
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.deleteBatch(onSuccess = onNavigateBack) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hapus Batch Ini")
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kembali ke Daftar Stok")
                }
            }
        }
    }
}

@Composable
private fun ReduceQuantityDialog(
    batch: StockBatch,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var reduceAmountStr by remember { mutableStateOf("1") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val reduceAmount = reduceAmountStr.toDoubleOrNull() ?: 0.0
    val remainingAfter = batch.quantity - reduceAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pemakaian Stok") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(
                    text = "Produk: ${batch.productName} (Sisa saat ini: ${batch.quantity} ${batch.unit})",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = reduceAmountStr,
                    onValueChange = { reduceAmountStr = it },
                    label = { Text("Jumlah Terjual / Dipakai (${batch.unit})") },
                    singleLine = true
                )

                if (reduceAmount > 0 && reduceAmount <= batch.quantity) {
                    Text(
                        text = "Sisa stok setelah pemakaian: $remainingAfter ${batch.unit}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (remainingAfter == 0.0) {
                        Text(
                            text = "ℹ️ Stok akan habis (0 ${batch.unit}). Status otomatis menjadi 'Terjual / Dipakai'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reduceAmount <= 0) {
                        errorText = "Jumlah pemakaian harus lebih besar dari 0"
                        return@Button
                    }
                    if (reduceAmount > batch.quantity) {
                        errorText = "Jumlah pemakaian melebihi sisa stok (${batch.quantity} ${batch.unit})"
                        return@Button
                    }
                    onConfirm(reduceAmount)
                }
            ) {
                Text("Konfirmasi Pemakaian")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun EditBatchDialog(
    batch: StockBatch,
    onDismiss: () -> Unit,
    onConfirm: (StockBatch) -> Unit
) {
    var name by remember { mutableStateOf(batch.productName) }
    var selectedCategory by remember { mutableStateOf(batch.category) }
    var quantityStr by remember { mutableStateOf(batch.quantity.toString()) }
    var unit by remember { mutableStateOf(batch.unit) }
    var expiryDateStr by remember { mutableStateOf(batch.expiryDate.toString()) }
    var purchasePriceStr by remember { mutableStateOf(batch.purchasePricePerUnit?.toString() ?: "") }
    var sellingPriceStr by remember { mutableStateOf(batch.normalSellingPricePerUnit?.toString() ?: "") }
    var notes by remember { mutableStateOf(batch.notes ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Batch Stok #${batch.id}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk *") },
                    singleLine = true
                )

                Text("Kategori", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProductCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Jumlah Stok *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Satuan") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = expiryDateStr,
                    onValueChange = { expiryDateStr = it },
                    label = { Text("Tanggal Kedaluwarsa (YYYY-MM-DD) *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("Harga Beli (Rp)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("Harga Jual Normal (Rp)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Penyimpanan") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtyDouble = quantityStr.toDoubleOrNull()
                    val parsedExpiry = try { LocalDate.parse(expiryDateStr.trim()) } catch (e: Exception) { null }

                    if (name.isBlank()) {
                        errorText = "Nama produk wajib diisi"
                        return@Button
                    }
                    if (qtyDouble == null || qtyDouble <= 0) {
                        errorText = "Jumlah stok harus lebih besar dari 0"
                        return@Button
                    }
                    if (parsedExpiry == null) {
                        errorText = "Tanggal kedaluwarsa tidak valid. Gunakan YYYY-MM-DD"
                        return@Button
                    }

                    val updated = batch.copy(
                        productName = name.trim(),
                        category = selectedCategory,
                        quantity = qtyDouble,
                        unit = unit.ifBlank { "pack" },
                        expiryDate = parsedExpiry,
                        purchasePricePerUnit = purchasePriceStr.toLongOrNull(),
                        normalSellingPricePerUnit = sellingPriceStr.toLongOrNull(),
                        notes = notes.ifBlank { null }
                    )
                    onConfirm(updated)
                }
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
