package com.segarstok.feature.batchdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.segarstok.domain.model.BatchStatus
import java.text.NumberFormat
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

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Batch #${batchId}") },
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

                        UrgencyBadge(urgencyLevel = batch.urgencyLevel())

                        HorizontalDivider()

                        Text("Kategori: ${batch.category.label}", style = MaterialTheme.typography.bodyLarge)
                        Text("Sisa Kuantitas: ${batch.quantity} ${batch.unit}", style = MaterialTheme.typography.bodyLarge)
                        Text("Tanggal Kedaluwarsa: ${batch.expiryDate}", style = MaterialTheme.typography.bodyLarge)
                        Text("Metode Input Tanggal: ${batch.dateEntryMethod ?: "MANUAL"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (!batch.notes.isNullOrBlank()) {
                            Text("Catatan: ${batch.notes}", style = MaterialTheme.typography.bodyMedium)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Button(
                        onClick = { viewModel.updateStatus(BatchStatus.SOLD_OR_USED) },
                        enabled = batch.status != BatchStatus.SOLD_OR_USED,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tandai Terjual / Dipakai")
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
