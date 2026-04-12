package com.example.inventoryhouse.ui.screen.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.domain.repository.ProductRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executors

@Composable
fun ScannerRoute(
    repository: ProductRepository,
    onAddProductClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = viewModel(
        factory = ScannerViewModel.provideFactory(ApiClient.openFoodFactsApi, repository)
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

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Scanne un produit pour afficher le formulaire d'ajout.")
                        Button(
                            onClick = { onEvent(ScannerEvent.ShowAddForm) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ajouter un produit manuellement")
                        }
                    }
                }
            }
        }
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
private fun AddProductCard(
    state: ScannerState,
    onEvent: (ScannerEvent) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.barcode,
                onValueChange = {},
                label = { Text("Code-barres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true
            )

            OutlinedTextField(
                value = state.productName,
                onValueChange = { onEvent(ScannerEvent.ProductNameChanged(it)) },
                label = { Text("Nom du produit") },
                placeholder = { Text("ex: Lait d'avoine") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LocationDropdown(
                selectedLocation = state.location,
                onLocationSelected = { onEvent(ScannerEvent.LocationChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quantité", fontWeight = FontWeight.Bold)

                IconButton(onClick = { onEvent(ScannerEvent.DecreaseQuantity) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Réduire")
                }

                Text(state.quantity.toString())

                IconButton(onClick = { onEvent(ScannerEvent.IncreaseQuantity) }) {
                    Icon(Icons.Default.Add, contentDescription = "Augmenter")
                }
            }

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
                singleLine = true
            )

            Button(
                onClick = { onEvent(ScannerEvent.AddProduct) },
                enabled = state.canAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Text(" Enregistrer le produit")
            }

            TextButton(
                onClick = { onEvent(ScannerEvent.HideAddForm) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Masquer le formulaire")
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            state.successMessage?.let {
                Text(it, color = Color(0xFF22B627))
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1F2A2E)),
        contentAlignment = Alignment.Center
    ) {
        if (permissionGranted) {
            BarcodeCameraPreview(
                modifier = Modifier.fillMaxSize(),
                onBarcodeDetected = onBarcodeDetected
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Autorise l'accès à la caméra pour scanner un produit",
                    color = Color.White
                )
                Button(onClick = requestPermission) {
                    Text("Activer la caméra")
                }
            }
        }
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
            value = selectedLocation?.name?.replace("_", " ") ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Location") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.name.replace("_", " ")) },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun imageProxySafeClose(previewView: PreviewView) {
    // no-op helper pour garder un catch explicite sans laisser de TODO
}
