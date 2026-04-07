package com.example.inventoryhouse.ui.screen.scanner.manualadd

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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.inventoryhouse.data.model.Product

@Composable
fun AddProductRoute(
    modifier: Modifier = Modifier,
    leaveScreen: () -> Unit,
    onProductAdded: (Product) -> Unit,
    viewModel: AddProductViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    AddProductScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onDateChange = viewModel::onDateChange,
        onLocationChange = viewModel::onLocationChange,
        onCancelClick = leaveScreen,
        onSaveClick = {
            viewModel.getProduct()?.let { product ->
                onProductAdded(product)
                leaveScreen()
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    state: AddProductState,
    onNameChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onLocationChange: (Location) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ajout manuel", style = MaterialTheme.typography.headlineSmall)

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text("Nom du produit") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.expirationDate,
                        onValueChange = onDateChange,
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            readOnly = true,
                            value = state.location.label,
                            onValueChange = {},
                            label = { Text("Emplacement") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Location.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        onLocationChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (state.errorMessage != null) {
                        Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f)) {
                    Text("Annuler")
                }
                Button(
                    onClick = onSaveClick,
                    enabled = state.isValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ajouter")
                }
            }
        }
    }
}

private val Location.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
