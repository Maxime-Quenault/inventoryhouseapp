package com.example.inventoryhouse.ui.screen.scanner

import com.example.inventoryhouse.data.enums.Location
import java.time.LocalDate

data class ReceiptItemDraft(
    val id: Long,
    val name: String,
    val packageFormat: String = "piece",
    val count: Int = 1,
    val selected: Boolean = true
)

data class ScannerState(
    val barcode: String = "",
    val expirationDate: String = "",
    val quantity: Int = 1,
    val quantityUnit: String = "piece",
    val imageUrl: String = "",
    val location: Location = Location.DRY,
    val productName: String = "",
    val isLoading: Boolean = false,
    val isReceiptProcessing: Boolean = false,
    val receiptDrafts: List<ReceiptItemDraft> = emptyList(),
    val receiptLocation: Location = Location.DRY,
    val receiptExpirationDate: String = LocalDate.now().plusYears(1).toString(),
    val isAddFormVisible: Boolean = false,
    val hasDetectedBarcode: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canAdd: Boolean
        get() = productName.isNotBlank() && expirationDate.isNotBlank() && !isLoading

    val selectedReceiptDrafts: List<ReceiptItemDraft>
        get() = receiptDrafts.filter { it.selected && it.name.isNotBlank() }

    val canAddReceipt: Boolean
        get() = selectedReceiptDrafts.isNotEmpty() &&
            receiptExpirationDate.isNotBlank() &&
            !isLoading &&
            !isReceiptProcessing
}
