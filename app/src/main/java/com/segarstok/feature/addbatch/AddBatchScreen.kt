package com.segarstok.feature.addbatch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.segarstok.core.design.theme.Spacing
import com.segarstok.core.ui.ViewModelFactory
import com.segarstok.domain.model.ProductCategory
import com.segarstok.ml.parser.CandidateConfidence
import com.segarstok.ml.parser.DateCandidate
import com.segarstok.ml.parser.DateContext
import com.segarstok.ml.parser.DateParseResult
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBatchScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddBatchViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(uri, context)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            viewModel.onImageSelected(tempCameraUri!!, context)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto label", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onNavigateBack()
        }
    }

    if (showDatePickerDialog) {
        val initialMillis = remember(uiState.expiryDateString) {
            try {
                if (uiState.expiryDateString.isNotBlank()) {
                    val localDate = LocalDate.parse(uiState.expiryDateString.trim())
                    localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                } else null
            } catch (e: Exception) {
                null
            }
        }

        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        viewModel.onExpiryDateChange(selectedLocalDate.toString())
                    }
                    showDatePickerDialog = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Batch Baru") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // OCR Scan Banner & Image Picker Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pemindaian Label dengan ML Kit OCR",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = "Ambil foto atau pilih gambar label produk untuk mengekstrak teks lokasi kedaluwarsa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Button(
                            onClick = {
                                val hasCameraPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasCameraPermission) {
                                    val uri = createTempImageUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📷 Kamera")
                        }

                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🖼️ Galeri")
                        }
                    }

                    // Display Image Preview if selected
                    if (uiState.selectedImageUri != null) {
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Foto Label Produk",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.small))
                        TextButton(onClick = { viewModel.clearSelectedImage() }) {
                            Text("Hapus Foto / Ulangi", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    // OCR Loading State
                    if (uiState.isOcrLoading) {
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Mengekstrak teks label dengan ML Kit...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // OCR Error Message
                    if (uiState.ocrErrorMessage != null) {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = uiState.ocrErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // OCR Raw Output Result Box
                    if (uiState.ocrRawText != null) {
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Spacing.medium)) {
                                Text(
                                    text = "Teks Mentah Hasil OCR:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                                Text(
                                    text = uiState.ocrRawText!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(Spacing.small))
                                Text(
                                    text = "ℹ️ Hasil OCR adalah bukti mentah. Pilih kandidat tanggal di bawah atau isi manual.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.medium))

                        // Candidate Date Section
                        OcrCandidateSection(
                            dateParseResult = uiState.dateParseResult,
                            selectedCandidate = uiState.selectedCandidate,
                            onCandidateSelected = { candidate ->
                                viewModel.onSelectCandidate(candidate)
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Form Input Batch", style = MaterialTheme.typography.titleMedium)

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = uiState.productName,
                onValueChange = { viewModel.onProductNameChange(it) },
                label = { Text("Nama Produk *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category dropdown / selector
            Text("Kategori Produk", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                ProductCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat.label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onQuantityChange(it) },
                    label = { Text("Jumlah Stok *") },
                    placeholder = { Text("10") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.unit,
                    onValueChange = { viewModel.onUnitChange(it) },
                    label = { Text("Satuan (pcs/pack/botol)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Expiry Date Field with Calendar Picker Icon & OCR status indicator
            OutlinedTextField(
                value = uiState.expiryDateString,
                onValueChange = { viewModel.onExpiryDateChange(it) },
                label = { Text("Tanggal Kedaluwarsa (YYYY-MM-DD) *") },
                placeholder = { Text("2026-12-25") },
                trailingIcon = {
                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih dari Kalender")
                    }
                },
                supportingText = {
                    val statusText = when (uiState.dateEntryMethod) {
                        "OCR_CONFIRMED" -> "✓ Dikonfirmasi dari OCR ML Kit"
                        "OCR_SCANNED" -> "📌 Diproses dari OCR (belum dikonfirmasi)"
                        else -> "✍️ Input manual"
                    }
                    Text(statusText, style = MaterialTheme.typography.labelSmall)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                OutlinedTextField(
                    value = uiState.purchasePrice,
                    onValueChange = { viewModel.onPurchasePriceChange(it) },
                    label = { Text("Harga Beli / Unit (Rp)") },
                    placeholder = { Text("10000") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.sellingPrice,
                    onValueChange = { viewModel.onSellingPriceChange(it) },
                    label = { Text("Harga Jual Normal (Rp)") },
                    placeholder = { Text("15000") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text("Catatan / Kondisi Penyimpanan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = { viewModel.saveBatch() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(Spacing.large), strokeWidth = Spacing.extraSmall)
                    } else {
                        Text("Simpan Batch")
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrCandidateSection(
    dateParseResult: DateParseResult,
    selectedCandidate: DateCandidate?,
    onCandidateSelected: (DateCandidate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        when (dateParseResult) {
            is DateParseResult.Candidates -> {
                Text(
                    text = "Kandidat Tanggal Terdeteksi Parser:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                dateParseResult.items.forEach { candidate ->
                    CandidateCard(
                        candidate = candidate,
                        isSelected = selectedCandidate == candidate,
                        onSelect = { onCandidateSelected(candidate) }
                    )
                }
            }
            is DateParseResult.Ambiguous -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Text(
                            text = "⚠️ Format Tanggal Ambigu",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateParseResult.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                if (dateParseResult.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih kandidat yang paling sesuai:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    dateParseResult.items.forEach { candidate ->
                        CandidateCard(
                            candidate = candidate,
                            isSelected = selectedCandidate == candidate,
                            onSelect = { onCandidateSelected(candidate) }
                        )
                    }
                }
            }
            is DateParseResult.NoCandidate -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ℹ️ Parser tidak menemukan kandidat tanggal pada label. Silakan pilih tanggal secara manual pada form.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: DateCandidate,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))
    val formattedDateStr = try { candidate.date.format(formatter) } catch (e: Exception) { candidate.date.toString() }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDateStr,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )

                // Context Badge
                val (contextLabel, contextColor) = when (candidate.context) {
                    DateContext.EXPIRY -> "Kedaluwarsa (EXP)" to MaterialTheme.colorScheme.primary
                    DateContext.PRODUCTION -> "Produksi (MFG)" to MaterialTheme.colorScheme.tertiary
                    DateContext.UNKNOWN -> "Konteks Tidak Diketahui" to MaterialTheme.colorScheme.outline
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = contextColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = contextLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = contextColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Teks mentah: \"${candidate.rawText}\" | Format: ${candidate.matchedFormat}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Confidence indicator
            val confidenceText = when (candidate.confidence) {
                CandidateConfidence.HIGH -> "Keyakinan: Tinggi"
                CandidateConfidence.MEDIUM -> "Keyakinan: Sedang"
                CandidateConfidence.LOW -> "Keyakinan: Rendah"
            }
            Text(
                text = confidenceText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            // Context warnings
            if (candidate.context == DateContext.PRODUCTION) {
                Text(
                    text = "⚠️ Ini adalah TANGGAL PRODUKSI, bukan tanggal kedaluwarsa.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            candidate.warnings.forEach { warning ->
                Text(
                    text = "⚠️ $warning",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isSelected) {
                FilledTonalButton(
                    onClick = onSelect,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("✓ Tanggal Terpilih")
                }
            } else {
                Button(
                    onClick = onSelect,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Gunakan Tanggal Ini")
                }
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "segarstok_ocr_${System.currentTimeMillis()}",
        ".jpg",
        context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
