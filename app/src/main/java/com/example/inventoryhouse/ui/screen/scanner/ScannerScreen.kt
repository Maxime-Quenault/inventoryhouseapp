package com.example.inventoryhouse.ui.screen.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.domain.repository.ProductRepository
import java.time.Instant
import java.time.ZoneId

@Composable
fun ScannerRoute(
    repository: ProductRepository,
    onAddProductClick: () -> Unit,
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
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1F2A2E))
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAction(Icons.Default.Image)
                CircleAction(Icons.Default.Search, isPrimary = true)
                CircleAction(Icons.Default.Warning)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEEF1F4))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ModeTab(label = "Scanner", selected = !state.isManualMode) { onEvent(ScannerEvent.ToggleManualMode) }
            ModeTab(label = "Manuel", selected = state.isManualMode) { onEvent(ScannerEvent.ToggleManualMode) }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!state.isManualMode) {
                    OutlinedTextField(
                        value = state.barcode,
                        onValueChange = { onEvent(ScannerEvent.BarcodeChanged(it)) },
                        label = { Text("Code-barres") },
                        placeholder = { Text("Ex: 3017624010701") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { onEvent(ScannerEvent.SearchByBarcode) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canSearch
                    ) {
                        Text(if (state.isLoading) "Recherche..." else "Scanner le produit")
                    }
                }

                OutlinedTextField(
                    value = state.productName,
                    onValueChange = { onEvent(ScannerEvent.ProductNameChanged(it)) },
                    label = { Text("Nom du produit") },
                    placeholder = { Text("ex: Lait d'avoine") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.category,
                    onValueChange = { onEvent(ScannerEvent.CategoryChanged(it)) },
                    label = { Text("Catégorie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Ouvrir le calendrier")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(onClick = { onEvent(ScannerEvent.AddProduct) }, enabled = state.canAdd, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text(" Enregistrer le produit")
                }

                if (state.errorMessage != null) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                }
                if (state.successMessage != null) {
                    Text(state.successMessage, color = Color(0xFF22B627))
                }

                TextButton(onClick = onAddProductClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Ouvrir l'écran d'ajout manuel complet")
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
private fun CircleAction(icon: androidx.compose.ui.graphics.vector.ImageVector, isPrimary: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (isPrimary) 64.dp else 44.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(if (isPrimary) Color(0xFF16EE18) else Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (isPrimary) Color.Black else Color.White)
    }
}

@Composable
private fun RowScope.ModeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.White else Color.Transparent,
            contentColor = Color(0xFF5A6772)
        )
    ) {
        Text(label)
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
                val localDate = Instant.ofEpochMilli(pickedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                onDatePicked(localDate.toString())
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
