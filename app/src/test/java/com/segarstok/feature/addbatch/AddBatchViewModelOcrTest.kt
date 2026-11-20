package com.segarstok.feature.addbatch

import android.content.Context
import android.net.Uri
import com.segarstok.domain.usecase.AddBatchUseCase
import com.segarstok.domain.usecase.FakeStockRepository
import com.segarstok.ml.ocr.OcrResult
import com.segarstok.ml.ocr.OcrService
import com.segarstok.ml.ocr.OcrTextBlock
import com.segarstok.ml.ocr.OcrTextLine
import com.segarstok.ml.parser.DateContext
import com.segarstok.ml.parser.DateParseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddBatchViewModelOcrTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeStockRepository
    private lateinit var addBatchUseCase: AddBatchUseCase
    private lateinit var fakeOcrService: FakeOcrService
    private lateinit var viewModel: AddBatchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStockRepository()
        addBatchUseCase = AddBatchUseCase(fakeRepository)
        fakeOcrService = FakeOcrService()
        viewModel = AddBatchViewModel(addBatchUseCase, fakeOcrService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectImage_ocrSuccess_updatesUiStateWithRawText() = runTest {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)

        fakeOcrService.resultToReturn = Result.success(
            OcrResult(
                fullText = "EXP 25/12/2026\nLOT 98213",
                blocks = listOf(
                    OcrTextBlock(
                        text = "EXP 25/12/2026",
                        lines = listOf(OcrTextLine("EXP 25/12/2026"))
                    )
                )
            )
        )

        viewModel.onImageSelected(mockUri, mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(mockUri, state.selectedImageUri)
        assertFalse(state.isOcrLoading)
        assertEquals("EXP 25/12/2026\nLOT 98213", state.ocrRawText)
        assertEquals("OCR_SCANNED", state.dateEntryMethod)
        assertNull(state.ocrErrorMessage)

        assertTrue(state.dateParseResult is DateParseResult.Candidates)
        val candidates = (state.dateParseResult as DateParseResult.Candidates).items
        assertEquals(1, candidates.size)
        assertEquals(LocalDate.of(2026, 12, 25), candidates[0].date)
        assertEquals(DateContext.EXPIRY, candidates[0].context)
    }

    @Test
    fun selectCandidate_updatesExpiryDateStringAndMethodToOcrConfirmed() = runTest {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)

        fakeOcrService.resultToReturn = Result.success(
            OcrResult(fullText = "EXP 25/12/2026")
        )

        viewModel.onImageSelected(mockUri, mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val parseResult = viewModel.uiState.value.dateParseResult
        assertTrue(parseResult is DateParseResult.Candidates)
        val candidate = (parseResult as DateParseResult.Candidates).items[0]

        viewModel.onSelectCandidate(candidate)

        val state = viewModel.uiState.value
        assertEquals("2026-12-25", state.expiryDateString)
        assertEquals("OCR_CONFIRMED", state.dateEntryMethod)
        assertEquals(candidate, state.selectedCandidate)
    }

    @Test
    fun expiryDateChange_manuallyEdited_resetsMethodToManual() = runTest {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)

        fakeOcrService.resultToReturn = Result.success(
            OcrResult(fullText = "EXP 25/12/2026")
        )

        viewModel.onImageSelected(mockUri, mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val parseResult = viewModel.uiState.value.dateParseResult
        val candidate = (parseResult as DateParseResult.Candidates).items[0]
        viewModel.onSelectCandidate(candidate)
        assertEquals("OCR_CONFIRMED", viewModel.uiState.value.dateEntryMethod)

        // User manually edits date string
        viewModel.onExpiryDateChange("2026-12-30")

        val state = viewModel.uiState.value
        assertEquals("2026-12-30", state.expiryDateString)
        assertEquals("MANUAL", state.dateEntryMethod)
        assertNull(state.selectedCandidate)
    }

    @Test
    fun selectImage_ocrEmptyText_setsErrorMessage() = runTest {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)

        fakeOcrService.resultToReturn = Result.success(OcrResult(fullText = "   "))

        viewModel.onImageSelected(mockUri, mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOcrLoading)
        assertNull(state.ocrRawText)
        assertEquals("Tidak ada teks terdeteksi pada gambar label", state.ocrErrorMessage)
        assertTrue(state.dateParseResult is DateParseResult.NoCandidate)
    }

    @Test
    fun selectImage_ocrFailure_setsOcrErrorMessage() = runTest {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)

        fakeOcrService.resultToReturn = Result.failure(Exception("Gagal membaca file gambar"))

        viewModel.onImageSelected(mockUri, mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOcrLoading)
        assertNull(state.ocrRawText)
        assertEquals("Gagal membaca file gambar", state.ocrErrorMessage)
    }

    @Test
    fun clearSelectedImage_resetsOcrStateAndEntryMethodToManual() = runTest {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)

        fakeOcrService.resultToReturn = Result.success(OcrResult(fullText = "EXP 25/12/2026"))
        viewModel.onImageSelected(mockUri, mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearSelectedImage()

        val state = viewModel.uiState.value
        assertNull(state.selectedImageUri)
        assertNull(state.ocrRawText)
        assertEquals("MANUAL", state.dateEntryMethod)
        assertNull(state.selectedCandidate)
        assertTrue(state.dateParseResult is DateParseResult.NoCandidate)
    }
}

class FakeOcrService : OcrService {
    var resultToReturn: Result<OcrResult> = Result.success(OcrResult(""))

    override suspend fun processImage(imageUri: Uri, context: Context): Result<OcrResult> {
        return resultToReturn
    }
}
