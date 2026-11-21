package com.segarstok.feature.addbatch

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.usecase.AddBatchUseCase
import com.segarstok.ml.ocr.OcrResult
import com.segarstok.ml.ocr.OcrService
import com.segarstok.ml.parser.DateCandidate
import com.segarstok.ml.parser.DateParseResult
import com.segarstok.ml.parser.DateParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddBatchUiState(
    val productName: String = "",
    val selectedCategory: ProductCategory = ProductCategory.FROZEN_FOOD,
    val quantity: String = "",
    val unit: String = "pack",
    val expiryDateString: String = "",
    val purchasePrice: String = "",
    val sellingPrice: String = "",
    val notes: String = "",
    val dateEntryMethod: String = "MANUAL",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    val selectedImageUri: Any? = null,
    val isOcrLoading: Boolean = false,
    val ocrResult: OcrResult? = null,
    val ocrRawText: String? = null,
    val ocrErrorMessage: String? = null,
    val dateParseResult: DateParseResult = DateParseResult.NoCandidate,
    val selectedCandidate: DateCandidate? = null
)

class AddBatchViewModel(
    private val addBatchUseCase: AddBatchUseCase,
    private val ocrService: OcrService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBatchUiState())
    val uiState: StateFlow<AddBatchUiState> = _uiState.asStateFlow()

    fun onProductNameChange(name: String) {
        _uiState.value = _uiState.value.copy(productName = name, errorMessage = null)
    }

    fun onCategoryChange(category: ProductCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onQuantityChange(qty: String) {
        _uiState.value = _uiState.value.copy(quantity = qty, errorMessage = null)
    }

    fun onUnitChange(unit: String) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun onExpiryDateChange(dateStr: String) {
        val currentCandidate = _uiState.value.selectedCandidate
        val isMatchingCandidate = currentCandidate != null && currentCandidate.date.toString() == dateStr.trim()
        val newMethod = if (isMatchingCandidate) "OCR_CONFIRMED" else "MANUAL"

        _uiState.value = _uiState.value.copy(
            expiryDateString = dateStr,
            errorMessage = null,
            dateEntryMethod = newMethod,
            selectedCandidate = if (isMatchingCandidate) currentCandidate else null
        )
    }

    fun onSelectCandidate(candidate: DateCandidate) {
        _uiState.value = _uiState.value.copy(
            expiryDateString = candidate.date.toString(),
            selectedCandidate = candidate,
            dateEntryMethod = "OCR_CONFIRMED",
            errorMessage = null
        )
    }

    fun onPurchasePriceChange(price: String) {
        _uiState.value = _uiState.value.copy(purchasePrice = price)
    }

    fun onSellingPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(sellingPrice = price)
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun onImageSelected(imageUri: Any?, context: Context? = null) {
        if (imageUri == null) return
        _uiState.value = _uiState.value.copy(
            selectedImageUri = imageUri,
            isOcrLoading = true,
            ocrErrorMessage = null,
            dateEntryMethod = "OCR_SCANNED",
            dateParseResult = DateParseResult.NoCandidate,
            selectedCandidate = null
        )

        val service = ocrService
        if (service == null) {
            _uiState.value = _uiState.value.copy(
                isOcrLoading = false,
                ocrErrorMessage = "OCR Service tidak tersedia"
            )
            return
        }

        viewModelScope.launch {
            val result = service.processImage(imageUri, context)
            result.onSuccess { ocrRes ->
                val fullText = ocrRes.fullText.trim()
                if (fullText.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isOcrLoading = false,
                        ocrResult = ocrRes,
                        ocrRawText = null,
                        ocrErrorMessage = "Tidak ada teks terdeteksi pada gambar label",
                        dateParseResult = DateParseResult.NoCandidate
                    )
                } else {
                    val parseResult = DateParser.parse(fullText)
                    _uiState.value = _uiState.value.copy(
                        isOcrLoading = false,
                        ocrResult = ocrRes,
                        ocrRawText = fullText,
                        ocrErrorMessage = null,
                        dateParseResult = parseResult
                    )
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isOcrLoading = false,
                    ocrResult = null,
                    ocrRawText = null,
                    ocrErrorMessage = err.message ?: "Gagal mengekstrak teks dari gambar",
                    dateParseResult = DateParseResult.NoCandidate
                )
            }
        }
    }

    fun clearSelectedImage() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            isOcrLoading = false,
            ocrResult = null,
            ocrRawText = null,
            ocrErrorMessage = null,
            dateParseResult = DateParseResult.NoCandidate,
            selectedCandidate = null,
            dateEntryMethod = "MANUAL"
        )
    }

    fun saveBatch() {
        val currentState = _uiState.value
        val name = currentState.productName.trim()
        val qtyDouble = currentState.quantity.toDoubleOrNull()
        val expiryLocalDate = try {
            LocalDate.parse(currentState.expiryDateString.trim())
        } catch (e: Exception) {
            null
        }

        if (name.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Nama produk wajib diisi")
            return
        }

        if (qtyDouble == null || qtyDouble <= 0) {
            _uiState.value = currentState.copy(errorMessage = "Jumlah stok harus berupa angka lebih besar dari 0")
            return
        }

        if (expiryLocalDate == null) {
            _uiState.value = currentState.copy(errorMessage = "Format tanggal tidak valid. Gunakan YYYY-MM-DD (Contoh: 2026-12-25)")
            return
        }

        val purchaseLong = currentState.purchasePrice.toLongOrNull()
        val sellingLong = currentState.sellingPrice.toLongOrNull()

        val batchToSave = StockBatch(
            productName = name,
            category = currentState.selectedCategory,
            quantity = qtyDouble,
            unit = currentState.unit.ifBlank { "pack" },
            expiryDate = expiryLocalDate,
            purchasePricePerUnit = purchaseLong,
            normalSellingPricePerUnit = sellingLong,
            status = BatchStatus.AVAILABLE,
            dateEntryMethod = currentState.dateEntryMethod,
            notes = currentState.notes.ifBlank { null }
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = addBatchUseCase(batchToSave)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSavedSuccess = true)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err.message ?: "Gagal menyimpan batch")
            }
        }
    }
}
