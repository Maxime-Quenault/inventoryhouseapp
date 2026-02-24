package com.example.inventoryhouse.ui.screen.scanner.manualadd

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.data.enums.Location
import java.time.LocalDate

@Composable
fun AddProductScreen(
    modifier: Modifier = Modifier, 
    leaveScreen: () -> Unit, 
    onProductAdded: (Product) -> Unit,
    viewModel: AddProductViewModel = viewModel()
){


    val state by viewModel.state
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(
            onClick = leaveScreen
        ) {
            Text("Annuler")
        }
        TextField(
            value = state.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = {Text("Nom du produit")}
        )
        TextField(
            value = state.expirationDate,
            onValueChange = { viewModel.onDateChange(it) },
            label = {Text("Date (AAAA-MM-JJ)")}
        )
        Row() {
            Text("Emplacement")
            Box {
                OutlinedButton(
                    onClick = { expanded = true }
                ) {
                    Text(state.location.name.lowercase().replaceFirstChar { it.uppercase() })
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Location.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.onLocationChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        // Dans AddProductScreen.kt
        Button(modifier = Modifier.padding(top = 16.dp),
            // 1. On grise le bouton si le formulaire n'est pas valide
            enabled = state.isValid,
            onClick = {
                // 2. On demande au ViewModel de fabriquer l'objet Product proprement
                viewModel.getProduct()?.let { product ->
                    onProductAdded(product) // Transmet le produit à la MainActivity
                    leaveScreen() // Ferme l'écran
                }
            }
        ) {
            Text("Ajouter")
        }
    }
}