package com.example.inventoryhouse.ui.screen.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.domain.repository.ProductRepository

@Composable
fun StockRoute(
    repository: ProductRepository,
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = viewModel(factory = StockViewModel.provideFactory(repository))
) {
    val state by viewModel.state.collectAsState()

    StockScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun StockScreen(
    state: StockState,
    onEvent: (StockEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationOptions = listOf<Location?>(null) + Location.entries

    LazyColumn(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = "Mon stock",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                locationOptions.forEach { location ->
                    val isSelected = state.selectedLocation == location
                    AssistChip(
                        onClick = { onEvent(StockEvent.SelectLocation(location)) },
                        label = {
                            Text(
                                if (location == null) "Tout" else location.label
                            )
                        },
                        modifier = Modifier,
                        enabled = !isSelected
                    )
                }
            }
        }

        items(state.filteredProducts) { product ->
            ProductItem(
                product = product,
                onDeleteClick = { onEvent(StockEvent.RemoveProduct(product)) }
            )
        }
    }
}

@Composable
private fun ProductItem(
    product: Product,
    onDeleteClick: () -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = product.location.label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = product.expiredDate.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer"
                )
            }
        }
    }
}

private val Location.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
