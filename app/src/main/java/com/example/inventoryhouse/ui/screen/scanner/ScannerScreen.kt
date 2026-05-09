package com.example.inventoryhouse.ui.screen.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.domain.repository.ProductRepository
import com.example.inventoryhouse.ui.component.FeedbackMessage
import com.example.inventoryhouse.ui.component.IconBubble
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.PrimaryActionButton
import com.example.inventoryhouse.ui.component.SectionHeader
import com.example.inventoryhouse.ui.component.StatusPill
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executors

@Composable
fun ScannerRoute(
    repository: ProductRepository,
    onAddProductClick: () -> Unit = {},
    viewModelKey: String? = null,
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = viewModel(
        key = viewModelKey,
        factory = ScannerViewModel.provideFactory(
            openFoodFactsApi = ApiClient.openFoodFactsApi,
            productRepository = repository,
            applicationContext = LocalContext.current.applicationContext
        )
    )
) {
    val state by viewModel.state.collectAsState()

    ScannerScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onAddProductClick = onAddProductClick,
        modifier = modifier
    )
}

@Composable
fun ScannerScreen(
    state: ScannerState,
    onEvent: (ScannerEvent) -> Unit,
    onAddProductClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showReceiptDatePicker by remember { mutableStateOf(false) }
    var pendingReceiptCapture by remember { mutableStateOf(false) }
    var receiptCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val receiptCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = receiptCaptureUri
        pendingReceiptCapture = false
        if (success && capturedUri != null) {
            onEvent(ScannerEvent.ReceiptImageCaptured(capturedUri))
        }
    }
    fun launchReceiptCamera() {
        runCatching { createReceiptImageUri(context) }
            .onSuccess { uri ->
                receiptCaptureUri = uri
                receiptCaptureLauncher.launch(uri)
            }
            .onFailure { error ->
                pendingReceiptCapture = false
                onEvent(
                    ScannerEvent.ReceiptCaptureFailed(
                        error.message ?: "Impossible de preparer la photo du ticket"
                    )
                )
            }
    }
    val receiptPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onEvent(ScannerEvent.ReceiptPdfSelected(uri))
        }
    }
    val receiptPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingReceiptCapture) {
            launchReceiptCamera()
        } else {
            pendingReceiptCapture = false
            onEvent(ScannerEvent.ReceiptCapturePermissionDenied)
        }
    }
    val captureReceipt: () -> Unit = {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            launchReceiptCamera()
        } else {
            pendingReceiptCapture = true
            receiptPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    InventoryBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 126.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ScannerHeader()
            }

            item {
                ReceiptCaptureCard(
                    isProcessing = state.isReceiptProcessing,
                    onCapture = captureReceipt,
                    onImportPdf = { receiptPdfLauncher.launch("application/pdf") }
                )
            }

            if (state.receiptDrafts.isNotEmpty()) {
                item {
                    ReceiptReviewCard(
                        state = state,
                        onEvent = onEvent,
                        onOpenDatePicker = { showReceiptDatePicker = true }
                    )
                }
            }

            if (!state.isAddFormVisible) {
                state.errorMessage?.let { message ->
                    item {
                        FeedbackMessage(text = message, isError = true)
                    }
                }
                state.successMessage?.let { message ->
                    item {
                        FeedbackMessage(text = message, isError = false)
                    }
                }
            }

            item {
                ScannerCameraHeader(
                    onBarcodeDetected = { onEvent(ScannerEvent.BarcodeDetected(it)) }
                )
            }

            item {
                if (state.isAddFormVisible) {
                    AddProductCard(
                        state = state,
                        onEvent = onEvent,
                        onOpenDatePicker = { showDatePicker = true }
                    )
                } else {
                    ManualAddCard(
                        onShowForm = {
                            onAddProductClick()
                            onEvent(ScannerEvent.ShowAddForm)
                        }
                    )
                }
            }
        }
    }

    if (showReceiptDatePicker) {
        ProductDatePicker(
            onDismiss = { showReceiptDatePicker = false },
            onDatePicked = {
                onEvent(ScannerEvent.ReceiptExpirationDateChanged(it))
                showReceiptDatePicker = false
            }
        )
    }

    if (showDatePicker) {
        ProductDatePicker(
            onDismiss = { showDatePicker = false },
            onDatePicked = {
                onEvent(ScannerEvent.ExpirationDateChanged(it))
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun ScannerHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Ajouter",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Scannez un code-barres ou saisissez un produit manuellement.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReceiptCaptureCard(
    isProcessing: Boolean,
    onCapture: () -> Unit,
    onImportPdf: () -> Unit
) {
    ModernCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Ticket de caisse", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Photo ou PDF puis validation des articles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    onClick = onImportPdf,
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Importer un ticket PDF"
                    )
                }
                IconButton(
                    onClick = onCapture,
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Photographier un ticket"
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptReviewCard(
    state: ScannerState,
    onEvent: (ScannerEvent) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    ModernCard(containerColor = MaterialTheme.colorScheme.surface) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Ticket",
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = "${state.selectedReceiptDrafts.size}/${state.receiptDrafts.size}",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            LocationDropdown(
                selectedLocation = state.receiptLocation,
                onLocationSelected = { onEvent(ScannerEvent.ReceiptLocationChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.receiptExpirationDate,
                onValueChange = { onEvent(ScannerEvent.ReceiptExpirationDateChanged(it)) },
                label = { Text("Date d'expiration") },
                placeholder = { Text("yyyy-mm-dd") },
                trailingIcon = {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Ouvrir le calendrier"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            )

            state.receiptDrafts.forEachIndexed { index, draft ->
                ReceiptDraftRow(
                    draft = draft,
                    enabled = !state.isLoading,
                    onEvent = onEvent
                )
                if (index < state.receiptDrafts.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            PrimaryActionButton(
                text = "Ajouter les articles",
                enabled = state.canAddReceipt,
                isLoading = state.isLoading,
                icon = Icons.Default.Add,
                onClick = { onEvent(ScannerEvent.AddReceiptProducts) }
            )

            TextButton(
                onClick = { onEvent(ScannerEvent.ClearReceiptDrafts) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Annuler")
            }
        }
    }
}

@Composable
private fun ReceiptDraftRow(
    draft: ReceiptItemDraft,
    enabled: Boolean,
    onEvent: (ScannerEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = draft.selected,
                onCheckedChange = {
                    onEvent(ScannerEvent.ReceiptDraftSelectedChanged(draft.id, it))
                },
                enabled = enabled
            )
            OutlinedTextField(
                value = draft.name,
                onValueChange = {
                    onEvent(ScannerEvent.ReceiptDraftNameChanged(draft.id, it))
                },
                label = { Text("Article") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = enabled
            )
            IconButton(
                onClick = { onEvent(ScannerEvent.RemoveReceiptDraft(draft.id)) },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Retirer"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft.packageFormat,
                onValueChange = {
                    onEvent(ScannerEvent.ReceiptDraftPackageFormatChanged(draft.id, it))
                },
                label = { Text("Format") },
                placeholder = { Text("500g") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = enabled
            )
            CompactCountSelector(
                count = draft.count,
                enabled = enabled,
                onDecrease = { onEvent(ScannerEvent.ReceiptDraftDecreaseCount(draft.id)) },
                onIncrease = { onEvent(ScannerEvent.ReceiptDraftIncreaseCount(draft.id)) }
            )
        }
    }
}

@Composable
private fun ManualAddCard(onShowForm: () -> Unit) {
    ModernCard(containerColor = MaterialTheme.colorScheme.surface) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBubble(
                icon = Icons.Outlined.Inventory2,
                tint = MaterialTheme.colorScheme.secondary,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ajout manuel", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Pratique pour les produits sans code-barres ou mal reconnus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PrimaryActionButton(
                text = "Ajouter manuellement",
                enabled = true,
                icon = Icons.Default.Add,
                onClick = onShowForm
            )
        }
    }
}

@Composable
private fun AddProductCard(
    state: ScannerState,
    onEvent: (ScannerEvent) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    ModernCard(containerColor = MaterialTheme.colorScheme.surface) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Produit",
                    modifier = Modifier.weight(1f)
                )
                if (state.hasDetectedBarcode) {
                    StatusPill(
                        text = "Scan détecté",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = state.barcode,
                onValueChange = {},
                label = { Text("Code-barres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.productName,
                onValueChange = { onEvent(ScannerEvent.ProductNameChanged(it)) },
                label = { Text("Nom du produit") },
                placeholder = { Text("ex : Lait d'avoine") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            )

            LocationDropdown(
                selectedLocation = state.location,
                onLocationSelected = { onEvent(ScannerEvent.LocationChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.quantityUnit,
                onValueChange = { onEvent(ScannerEvent.QuantityUnitChanged(it)) },
                label = { Text("Format") },
                placeholder = { Text("ex : 500g, 1L, piece") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            )

            QuantitySelector(
                quantity = state.quantity,
                onDecrease = { onEvent(ScannerEvent.DecreaseQuantity) },
                onIncrease = { onEvent(ScannerEvent.IncreaseQuantity) }
            )

            OutlinedTextField(
                value = state.expirationDate,
                onValueChange = { onEvent(ScannerEvent.ExpirationDateChanged(it)) },
                label = { Text("Date d'expiration") },
                placeholder = { Text("yyyy-mm-dd") },
                trailingIcon = {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Ouvrir le calendrier"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            )

            PrimaryActionButton(
                text = "Enregistrer le produit",
                enabled = state.canAdd,
                isLoading = state.isLoading,
                icon = Icons.Default.CameraAlt,
                onClick = { onEvent(ScannerEvent.AddProduct) }
            )

            TextButton(
                onClick = { onEvent(ScannerEvent.HideAddForm) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Masquer le formulaire")
            }

            state.errorMessage?.let {
                FeedbackMessage(text = it, isError = true)
            }

            state.successMessage?.let {
                FeedbackMessage(text = it, isError = false)
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    ModernCard(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Quantité", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Nombre d'unités à stocker",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Default.Remove, contentDescription = "Réduire")
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, contentDescription = "Augmenter")
                }
            }
        }
    }
}

@Composable
private fun CompactCountSelector(
    count: Int,
    enabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                enabled = enabled
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Reduire")
            }
            Text(
                text = "x$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(
                onClick = onIncrease,
                enabled = enabled
            ) {
                Icon(Icons.Default.Add, contentDescription = "Augmenter")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDatePicker(
    onDismiss: () -> Unit,
    onDatePicked: (String) -> Unit
) {
    val pickerState = androidx.compose.material3.rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val pickedMillis = pickerState.selectedDateMillis ?: return@TextButton
                val localDate = Instant.ofEpochMilli(pickedMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                onDatePicked(localDate.toString())
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
fun rememberCameraPermissionState(
    onPermissionResult: (Boolean) -> Unit
): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )

    val isGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    return isGranted to { launcher.launch(Manifest.permission.CAMERA) }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun ScannerCameraHeader(
    onBarcodeDetected: (String) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    val (isGrantedNow, requestPermission) = rememberCameraPermissionState {
        permissionGranted = it
    }

    LaunchedEffect(isGrantedNow) {
        permissionGranted = isGrantedNow
    }

    ModernCard(
        containerColor = Color(0xFF101817),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(292.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF101817)),
            contentAlignment = Alignment.Center
        ) {
            if (permissionGranted) {
                BarcodeCameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeDetected = onBarcodeDetected
                )
                ScannerFrame()
            } else {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    IconBubble(
                        icon = Icons.Outlined.QrCodeScanner,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Text(
                        "Autorisez la caméra pour scanner un produit.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = requestPermission) {
                        Text("Activer la caméra")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerFrame() {
    Box(
        modifier = Modifier
            .size(width = 220.dp, height = 132.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary
        ) {}
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun BarcodeCameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(FORMAT_EAN_13, FORMAT_EAN_8, FORMAT_UPC_A, FORMAT_UPC_E)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(scanner) {
        onDispose { scanner.close() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraExecutor = Executors.newSingleThreadExecutor()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes
                                .firstOrNull { it.rawValue?.isNotBlank() == true }
                                ?.rawValue
                                ?.let(onBarcodeDetected)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    imageProxySafeClose(previewView)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val locations = Location.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLocation?.displayLabel.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Emplacement") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.displayLabel) },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}

private val Location.displayLabel: String
    get() = when (this) {
        Location.FRESH -> "Frais"
        Location.DRY -> "Placard"
        Location.FROZEN -> "Congelé"
    }

private fun imageProxySafeClose(previewView: PreviewView) {
    // Keeps the camera binding catch explicit while avoiding a noisy crash path.
}

private fun createReceiptImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "receipt_images").apply { mkdirs() }
    val imageFile = File.createTempFile("receipt_", ".jpg", imageDir)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
