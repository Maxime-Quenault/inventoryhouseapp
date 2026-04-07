package com.example.inventoryhouse.ui.screen.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.domain.repository.ProductRepository

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    state: ScannerState,
    onEvent: (ScannerEvent) -> Unit,
    onAddProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Scanner un produit", style = MaterialTheme.typography.headlineSmall)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    enabled = state.canSearch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isLoading) "Recherche..." else "Rechercher via Open Food Facts")
                }

                if (state.errorMessage != null) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                }

                if (state.productName.isNotBlank()) {
                    Text("Produit détecté: ${state.productName}")
                    if (state.brand.isNotBlank()) {
                        Text("Marque: ${state.brand}")
                    }

                    OutlinedTextField(
                        value = state.expirationDate,
                        onValueChange = { onEvent(ScannerEvent.ExpirationDateChanged(it)) },
                        label = { Text("Date d'expiration (AAAA-MM-JJ)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = state.selectedLocation.label,
                            onValueChange = {},
                            label = { Text("Emplacement") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Location.entries.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location.label) },
                                    onClick = {
                                        expanded = false
                                        onEvent(ScannerEvent.LocationChanged(location))
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAddProductClick, modifier = Modifier.weight(1f)) {
                            Text("Ajout manuel")
                        }
                        Button(
                            onClick = { onEvent(ScannerEvent.AddProduct) },
                            enabled = state.canAdd,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ajouter")
                        }
                    }
                }

                if (state.successMessage != null) {
                    Text(state.successMessage, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        OutlinedButton(onClick = onAddProductClick, modifier = Modifier.fillMaxWidth()) {
            Text("Ajouter manuellement")
        }
    }
}

private val Location.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
