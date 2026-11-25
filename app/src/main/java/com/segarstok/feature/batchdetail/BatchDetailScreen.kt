package com.segarstok.feature.batchdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        EditBatchBottomSheet(
            batch = batch,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated: StockBatch ->
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
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Slate900
                        )
                    }
                },
                title = {
                    Text(
                        text = "Detail Batch #$batchId",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Slate900
                        )
                    )
                },
                actions = {
                    if (batch != null) {
                        TextButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Batch",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = PrimaryTeal
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasBackground,
                    titleContentColor = Slate900
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
            val daysLeft = batch.daysLeft()
            val daysText = when {
                daysLeft < 0 -> "Lewat ${-daysLeft} hari"
                daysLeft == 0L -> "Hari Ini"
                else -> "Sisa $daysLeft hari"
            }
            val isExpired = daysLeft < 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                if (uiState.errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ExpiredBg,
                        border = BorderStroke(1.dp, ExpiredBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = ExpiredText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ── Card 1: Main Product Info ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Product name + status badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = batch.productName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Slate900
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BatchStatusBadge(status = batch.status)
                                UrgencyBadge(urgencyLevel = batch.urgencyLevel())
                            }
                        }

                        // Countdown banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isExpired) ExpiredBg else NormalBg)
                                .then(
                                    Modifier // border via padding trick via Surface below
                                )
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isExpired) ExpiredBg else NormalBg,
                                border = BorderStroke(1.dp, if (isExpired) ExpiredBorder else NormalBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = if (isExpired) ExpiredText else NormalText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sisa Waktu: $daysText",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isExpired) ExpiredText else NormalText
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = DividerColor)

                        // 2x2 Info Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoField(
                                icon = Icons.Outlined.Info,
                                label = "Kategori",
                                value = batch.category.label,
                                modifier = Modifier.weight(1f)
                            )
                            InfoField(
                                icon = Icons.Outlined.Info,
                                label = "Sisa Kuantitas",
                                value = "${formatQty(batch.quantity)} ${batch.unit}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoField(
                                icon = Icons.Outlined.Info,
                                label = "Tanggal Kedaluwarsa",
                                value = batch.expiryDate.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Metode Input Tanggal",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Text(
                                        text = batch.dateEntryMethod ?: "MANUAL",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Slate700
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (!batch.notes.isNullOrBlank()) {
                            HorizontalDivider(color = DividerColor)
                            Text(
                                text = "Catatan: ${batch.notes}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = Slate500
                                )
                            )
                        }
                    }
                }

                // ── Card 2: Expiry Loss Monitor ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expiry Loss Monitor",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Slate900
                                )
                            )
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Price row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Harga Beli per Unit",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = batch.purchasePricePerUnit?.let {
                                        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                                            .apply { maximumFractionDigits = 0 }.format(it)
                                    } ?: "Belum diisi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Slate900
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Harga Jual Normal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = batch.normalSellingPricePerUnit?.let {
                                        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                                            .apply { maximumFractionDigits = 0 }.format(it)
                                    } ?: "Belum diisi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Slate900
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = DividerColor)

                        // Risk value box
                        val riskValue = batch.remainingRiskValue()
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (riskValue != null && riskValue > 0) ExpiredBg else NormalBg,
                            border = BorderStroke(1.dp, if (riskValue != null && riskValue > 0) ExpiredBorder else NormalBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Estimasi Modal Berisiko",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.sp,
                                        color = if (riskValue != null && riskValue > 0) ExpiredText else NormalText
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = riskValue?.let { currencyFormat.format(it) }
                                        ?: "Tidak dihitung",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = if (riskValue != null && riskValue > 0) ExpiredText else NormalText,
                                        fontFeatureSettings = "tnum"
                                    )
                                )
                                Text(
                                    text = "(Hanya dihitung untuk batch berstatus AVAILABLE)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = if (riskValue != null && riskValue > 0) ExpiredText else NormalText
                                    )
                                )
                            }
                        }
                    }
                }

                // ── Section: Tindakan Pengelolaan Stok ──
                Text(
                    text = "Tindakan Pengelolaan Stok",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                )

                // Catat Pemakaian button (full width, teal)
                if (batch.status == BatchStatus.AVAILABLE && batch.quantity > 0) {
                    Button(
                        onClick = { showReduceDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catat Pemakaian / Penjualan Sebagian",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Tandai Terjual + Tandai Dibuang row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.updateStatus(BatchStatus.SOLD_OR_USED) },
                        enabled = batch.status != BatchStatus.SOLD_OR_USED,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = Color.White,
                            disabledContainerColor = PrimaryTealContainer,
                            disabledContentColor = PrimaryTealDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tandai Terjual Semua",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(BatchStatus.DISCARDED) },
                        enabled = batch.status != BatchStatus.DISCARDED,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurface,
                            contentColor = Slate700
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tandai Dibuang",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Kembalikan status (jika tidak available)
                if (batch.status != BatchStatus.AVAILABLE) {
                    OutlinedButton(
                        onClick = { viewModel.updateStatus(BatchStatus.AVAILABLE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryTeal),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurface,
                            contentColor = PrimaryTeal
                        )
                    ) {
                        Text(
                            text = "Kembalikan Status ke Tersedia",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PrimaryTeal
                            )
                        )
                    }
                }

                // Hapus Batch button (merah outline)
                OutlinedButton(
                    onClick = { viewModel.deleteBatch(onSuccess = onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ExpiredBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ExpiredBg,
                        contentColor = ExpiredText
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = ExpiredText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Batch Ini",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = ExpiredText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = Slate500
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )
        )
    }
}

private fun formatQty(quantity: Double): String {
    val intPart = quantity.toLong()
    return if (quantity == intPart.toDouble()) {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(intPart)
    } else {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(quantity)
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        color = PrimaryTeal
                    )
                    if (remainingAfter == 0.0) {
                        Text(
                            text = "Stok akan habis (0 ${batch.unit}). Status otomatis menjadi 'Terjual / Dipakai'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningText
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditBatchBottomSheet(
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Batch Stok #${batch.id}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Slate900
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Slate500
                    )
                }
            }

            // Status Badge / Info Pill matching screenshot:
            // "Batch #5 • Kedaluwarsa: 2017-09-04 • Tersedia"
            val (bgColor, borderColor, textColor) = when (batch.status) {
                BatchStatus.AVAILABLE -> Triple(NormalBg, NormalBorder, NormalText)
                BatchStatus.SOLD_OR_USED -> Triple(CardSurface, CardBorder, Slate500)
                BatchStatus.DISCARDED -> Triple(ExpiredBg, ExpiredBorder, ExpiredText)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Batch #${batch.id} • Kedaluwarsa: ${batch.expiryDate} • ${batch.status.label}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = textColor
                )
            }

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Nama Produk *
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Produk *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Kategori
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        fontSize = 14.sp
                    )
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductCategory.entries.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                    Text(
                                        text = cat.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = CanvasBackground,
                                labelColor = Slate700,
                                selectedContainerColor = PrimaryTeal,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CardBorder,
                                selectedBorderColor = PrimaryTeal
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Jumlah Stok *
            OutlinedTextField(
                value = quantityStr,
                onValueChange = { quantityStr = it },
                label = { Text("Jumlah Stok *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Text(
                        text = unit.ifBlank { "pack" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Satuan
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Satuan") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih Satuan",
                        tint = Slate500
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Tanggal Kedaluwarsa & Harga / Catatan
            OutlinedTextField(
                value = expiryDateStr,
                onValueChange = { expiryDateStr = it },
                label = { Text("Tanggal Kedaluwarsa (YYYY-MM-DD) *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = CardBorder
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("Harga Beli (Rp)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = CardBorder
                    )
                )
                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("Harga Jual (Rp)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan / Penyimpanan") },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Info Card matching screenshot:
            // "Perubahan jumlah stok akan langsung memperbarui estimasi Modal Berisiko pada dashboard."
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryTealLight)
                    .border(1.dp, PrimaryTealContainer, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = "Perubahan jumlah stok akan langsung memperbarui estimasi Modal Berisiko pada dashboard.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                ) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }

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
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(
                        text = "Simpan Perubahan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}


