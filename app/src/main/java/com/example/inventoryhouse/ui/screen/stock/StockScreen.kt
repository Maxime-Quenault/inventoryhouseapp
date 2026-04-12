package com.example.inventoryhouse.ui.screen.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.enums.Location
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
    val categoryOptions = listOf<Location?>(null) + Location.entries

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(StockEvent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher un aliment") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoryOptions) { category ->
                    val isSelected = category == state.selectedCategory
                    AssistChip(
                        onClick = { onEvent(StockEvent.SelectCategory(category)) },
                        label = { Text(category?.name ?: "Tous") },
                        enabled = !isSelected
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sort, contentDescription = null, tint = Color(0xFF33C93E))
                    Text("Trier par: Date d'expiration", color = Color(0xFF33C93E))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF33C93E))
                    Text("Filtrer", color = Color(0xFF33C93E))
                }
            }
        }

        items(items = state.displayItems, key = { it.id }) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFDFE6DF), shape = MaterialTheme.shapes.medium)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(item.details, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            item.expirationLabel,
                            color = when (item.expirationTone) {
                                ExpirationTone.SAFE -> Color(0xFF22B627)
                                ExpirationTone.WARNING -> Color(0xFFFF8A00)
                                ExpirationTone.DANGER -> Color(0xFFE63B31)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Column {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                        IconButton(
                            onClick = { item.sourceProduct?.let { onEvent(StockEvent.RemoveProduct(it)) } },
                            enabled = item.sourceProduct != null
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            }
        }
    }
}
