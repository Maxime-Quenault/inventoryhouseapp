package com.example.inventoryhouse.ui.screen.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.data.remote.api.OpenFoodFactsApi
import com.example.inventoryhouse.data.remote.dto.OpenFoodFactsProductDto
import com.example.inventoryhouse.domain.repository.ProductRepository
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

private const val PDF_RENDER_TARGET_WIDTH = 1800

class ScannerViewModel(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val productRepository: ProductRepository,
    private val applicationContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state.asStateFlow()
    private var nextReceiptDraftId = 1L

    fun onEvent(event: ScannerEvent) {
        when (event) {
            is ScannerEvent.BarcodeDetected -> onBarcodeDetected(event.barcode)
            is ScannerEvent.ReceiptImageCaptured -> recognizeReceiptImage(event.uri)
            is ScannerEvent.ReceiptPdfSelected -> recognizeReceiptPdf(event.uri)
            ScannerEvent.ReceiptCapturePermissionDenied -> _state.update {
                it.copy(errorMessage = "Autorisation camera requise pour photographier un ticket")
            }
            is ScannerEvent.ReceiptCaptureFailed -> _state.update {
                it.copy(errorMessage = event.message)
            }
            is ScannerEvent.ReceiptDraftNameChanged -> updateReceiptDraft(event.id) { draft ->
                draft.copy(name = event.value)
            }
            is ScannerEvent.ReceiptDraftPackageFormatChanged -> updateReceiptDraft(event.id) { draft ->
                draft.copy(packageFormat = event.value)
            }
            is ScannerEvent.ReceiptDraftSelectedChanged -> updateReceiptDraft(event.id) { draft ->
                draft.copy(selected = event.selected)
            }
            is ScannerEvent.ReceiptDraftIncreaseCount -> updateReceiptDraft(event.id) { draft ->
                draft.copy(count = draft.count + 1)
            }
            is ScannerEvent.ReceiptDraftDecreaseCount -> updateReceiptDraft(event.id) { draft ->
                draft.copy(count = (draft.count - 1).coerceAtLeast(1))
            }
            is ScannerEvent.RemoveReceiptDraft -> _state.update { current ->
                current.copy(receiptDrafts = current.receiptDrafts.filterNot { it.id == event.id })
            }
            is ScannerEvent.ReceiptLocationChanged -> _state.update { it.copy(receiptLocation = event.location) }
            is ScannerEvent.ReceiptExpirationDateChanged -> _state.update {
                it.copy(receiptExpirationDate = event.value, errorMessage = null)
            }
            is ScannerEvent.ProductNameChanged -> _state.update {
                it.copy(productName = event.value, errorMessage = null)
            }
            is ScannerEvent.QuantityUnitChanged -> _state.update {
                it.copy(quantityUnit = event.value, errorMessage = null)
            }
            is ScannerEvent.LocationChanged -> _state.update { it.copy(location = event.location) }
            is ScannerEvent.ExpirationDateChanged -> _state.update {
                it.copy(expirationDate = event.value, errorMessage = null)
            }
            ScannerEvent.IncreaseQuantity -> _state.update { it.copy(quantity = it.quantity + 1) }
            ScannerEvent.DecreaseQuantity -> _state.update {
                it.copy(quantity = (it.quantity - 1).coerceAtLeast(1))
            }
            ScannerEvent.ShowAddForm -> _state.update {
                it.copy(isAddFormVisible = true, errorMessage = null, successMessage = null)
            }
            ScannerEvent.HideAddForm -> _state.update {
                it.copy(isAddFormVisible = false, errorMessage = null, successMessage = null)
            }
            ScannerEvent.ClearFeedback -> _state.update {
                it.copy(errorMessage = null, successMessage = null)
            }
            ScannerEvent.AddProduct -> addProduct()
            ScannerEvent.AddReceiptProducts -> addReceiptProducts()
            ScannerEvent.ClearReceiptDrafts -> _state.update {
                it.copy(receiptDrafts = emptyList(), errorMessage = null, successMessage = null)
            }
        }
    }

    private fun onBarcodeDetected(rawBarcode: String) {
        val barcode = rawBarcode.filter(Char::isDigit).take(14)
        if (barcode.length < 8) return

        val current = state.value
        if (current.barcode == barcode && current.hasDetectedBarcode) return

        _state.update {
            it.copy(
                barcode = barcode,
                hasDetectedBarcode = true,
                isAddFormVisible = true,
                isLoading = true,
                errorMessage = null,
                successMessage = "Code-barres detecte : $barcode"
            )
        }

        viewModelScope.launch {
            try {
                val resDto: OpenFoodFactsProductDto? =
                    openFoodFactsApi.getProductByBarcode(barcode).product

                if (resDto == null) {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Aucun produit trouve")
                    }
                    return@launch
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        productName = resDto.productName.orEmpty(),
                        quantity = 1,
                        quantityUnit = resDto.toPackageFormat(),
                        imageUrl = resDto.imageUrl.orEmpty(),
                        errorMessage = null,
                        successMessage = "Produit trouve : ${resDto.productName.orEmpty()}"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur OpenFoodFacts : ${e.message}"
                    )
                }
            }
        }
    }

    private fun recognizeReceiptImage(uri: Uri) {
        _state.update {
            it.copy(
                isReceiptProcessing = true,
                errorMessage = null,
                successMessage = null,
                receiptDrafts = emptyList(),
                isAddFormVisible = false
            )
        }

        val image = try {
            InputImage.fromFilePath(applicationContext, uri)
        } catch (error: Exception) {
            _state.update {
                it.copy(
                    isReceiptProcessing = false,
                    errorMessage = error.message ?: "Impossible d'ouvrir cette photo"
                )
            }
            return
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { recognizedText ->
                applyRecognizedReceiptText(recognizedText.text)
            }
            .addOnFailureListener { error ->
                _state.update {
                    it.copy(
                        isReceiptProcessing = false,
                        errorMessage = error.message ?: "Impossible de lire ce ticket"
                    )
                }
            }
            .addOnCompleteListener {
                recognizer.close()
            }
    }

    private fun recognizeReceiptPdf(uri: Uri) {
        _state.update {
            it.copy(
                isReceiptProcessing = true,
                errorMessage = null,
                successMessage = null,
                receiptDrafts = emptyList(),
                isAddFormVisible = false
            )
        }

        viewModelScope.launch {
            try {
                val recognizedText = withContext(Dispatchers.IO) {
                    recognizePdfText(uri)
                }
                applyRecognizedReceiptText(recognizedText)
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isReceiptProcessing = false,
                        errorMessage = error.message ?: "Impossible de lire ce PDF"
                    )
                }
            }
        }
    }

    private fun applyRecognizedReceiptText(rawText: String) {
        val drafts = ReceiptTextParser.parse(rawText).map { parsed ->
            ReceiptItemDraft(
                id = nextReceiptDraftId++,
                name = parsed.name,
                packageFormat = parsed.packageFormat,
                count = parsed.count
            )
        }

        _state.update {
            if (drafts.isEmpty()) {
                it.copy(
                    isReceiptProcessing = false,
                    errorMessage = "Aucun article detecte sur ce ticket"
                )
            } else {
                it.copy(
                    isReceiptProcessing = false,
                    receiptDrafts = drafts,
                    successMessage = "${drafts.size} article(s) detecte(s)"
                )
            }
        }
    }

    private fun recognizePdfText(uri: Uri): String {
        val descriptor = applicationContext.contentResolver.openFileDescriptor(uri, "r")
            ?: throw RuntimeException("Impossible d'ouvrir ce PDF")

        return descriptor.use { parcelFileDescriptor ->
            PdfRenderer(parcelFileDescriptor).use { renderer ->
                if (renderer.pageCount == 0) {
                    throw RuntimeException("Ce PDF ne contient aucune page")
                }

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                try {
                    buildString {
                        repeat(renderer.pageCount) { pageIndex ->
                            renderer.openPage(pageIndex).use { page ->
                                val bitmap = page.renderForOcr()
                                try {
                                    val image = InputImage.fromBitmap(bitmap, 0)
                                    appendLine(Tasks.await(recognizer.process(image)).text)
                                } finally {
                                    bitmap.recycle()
                                }
                            }
                        }
                    }
                } finally {
                    recognizer.close()
                }
            }
        }
    }

    private fun PdfRenderer.Page.renderForOcr(): Bitmap {
        val scale = (PDF_RENDER_TARGET_WIDTH / width.toFloat()).coerceAtLeast(1f)
        val targetWidth = (width * scale).roundToInt()
        val targetHeight = (height * scale).roundToInt()
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

        Canvas(bitmap).drawColor(Color.WHITE)
        render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }

    private fun updateReceiptDraft(
        id: Long,
        transform: (ReceiptItemDraft) -> ReceiptItemDraft
    ) {
        _state.update { current ->
            current.copy(
                receiptDrafts = current.receiptDrafts.map { draft ->
                    if (draft.id == id) transform(draft) else draft
                }
            )
        }
    }

    private fun addProduct() {
        val current = state.value
        val expirationDate = try {
            LocalDate.parse(current.expirationDate)
        } catch (_: DateTimeParseException) {
            _state.update { it.copy(errorMessage = "Date invalide (format AAAA-MM-JJ)") }
            return
        }

        if (current.productName.isBlank()) {
            _state.update { it.copy(errorMessage = "Nom du produit obligatoire") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                productRepository.addProduct(
                    Product(
                        id = System.currentTimeMillis(),
                        name = current.productName,
                        expiredDate = expirationDate,
                        location = current.location,
                        imageUrl = current.imageUrl,
                        quantity = current.quantity.coerceAtLeast(1),
                        quantityUnit = current.quantityUnit.ifBlank { "piece" }
                    )
                )

                _state.update {
                    it.copy(
                        barcode = "",
                        productName = "",
                        expirationDate = "",
                        quantity = 1,
                        quantityUnit = "piece",
                        imageUrl = "",
                        hasDetectedBarcode = false,
                        isAddFormVisible = false,
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Produit enregistre"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible d'enregistrer le produit"
                    )
                }
            }
        }
    }

    private fun addReceiptProducts() {
        val current = state.value
        val expirationDate = try {
            LocalDate.parse(current.receiptExpirationDate)
        } catch (_: DateTimeParseException) {
            _state.update { it.copy(errorMessage = "Date invalide (format AAAA-MM-JJ)") }
            return
        }

        val drafts = current.selectedReceiptDrafts
            .filter { it.name.isNotBlank() }

        if (drafts.isEmpty()) {
            _state.update { it.copy(errorMessage = "Aucun article selectionne") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                productRepository.addProducts(
                    drafts.map { draft ->
                        Product(
                            id = System.currentTimeMillis() + draft.id,
                            name = draft.name.trim(),
                            expiredDate = expirationDate,
                            location = current.receiptLocation,
                            imageUrl = "",
                            quantity = draft.count.coerceAtLeast(1),
                            quantityUnit = draft.packageFormat.trim().ifBlank { "piece" }
                        )
                    }
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        receiptDrafts = emptyList(),
                        errorMessage = null,
                        successMessage = "${drafts.size} article(s) ajoute(s)"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible d'ajouter les articles du ticket"
                    )
                }
            }
        }
    }

    private fun OpenFoodFactsProductDto.toPackageFormat(): String {
        val amount = quantity?.takeIf { it > 0 }?.toString().orEmpty()
        val unit = quantityUnit.orEmpty().trim()

        return when {
            amount.isNotBlank() && unit.isNotBlank() -> "$amount$unit"
            amount.isNotBlank() -> amount
            unit.isNotBlank() -> unit
            else -> "piece"
        }
    }

    companion object {
        fun provideFactory(
            openFoodFactsApi: OpenFoodFactsApi,
            productRepository: ProductRepository,
            applicationContext: Context
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ScannerViewModel(openFoodFactsApi, productRepository, applicationContext)
            }
        }
    }
}
