package com.example.inventoryhouse.ui.screen.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.domain.repository.ProductRepository
import com.example.inventoryhouse.ui.component.EmptyState
import com.example.inventoryhouse.ui.component.FeedbackMessage
import com.example.inventoryhouse.ui.component.IconBubble
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.SectionHeader
import com.example.inventoryhouse.ui.component.StatusPill

@Composable
fun StockRoute(
    repository: ProductRepository,
    viewModelKey: String? = null,
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = viewModel(
        key = viewModelKey,
        factory = StockViewModel.provideFactory(repository)
    )
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

    InventoryBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 126.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StockHeader(
                    totalItems = state.products.size,
                    visibleItems = state.displayItems.size,
                    onRefresh = { onEvent(StockEvent.Refresh) }
                )
            }

            item {
                ModernCard(contentPadding = PaddingValues(12.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onEvent(StockEvent.SearchChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Rechercher un aliment") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryOptions) { category ->
                        val isSelected = category == state.selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { onEvent(StockEvent.SelectCategory(category)) },
                            label = { Text(category?.displayLabel ?: "Tous") },
                            leadingIcon = category?.let {
                                {
                                    Icon(
                                        imageVector = it.icon,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = "Tri : expiration",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            state.errorMessage?.let { message ->
                item {
                    FeedbackMessage(text = message, isError = true)
                }
            }

            if (state.displayItems.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = "Stock vide",
                        message = "Scannez ou ajoutez un produit pour remplir cette maison."
                    )
                }
            }

            if (state.displayItems.isNotEmpty()) {
                item {
                    SectionHeader(title = "Produits")
                }
            }

            items(items = state.displayItems, key = { it.id }) { item ->
                StockItemRow(
                    item = item,
                    onRemove = {
                        item.sourceProduct?.let { onEvent(StockEvent.RemoveProduct(it)) }
                    }
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StockHeader(
    totalItems: Int,
    visibleItems: Int,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Stock",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$visibleItems affiché(s) sur $totalItems article(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
        }
    }
}

@Composable
private fun StockItemRow(
    item: StockItemUi,
    onRemove: () -> Unit
) {
    val tone = when (item.expirationTone) {
        ExpirationTone.SAFE -> MaterialTheme.colorScheme.primary
        ExpirationTone.WARNING -> MaterialTheme.colorScheme.tertiary
        ExpirationTone.DANGER -> MaterialTheme.colorScheme.error
    }

    ModernCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(
                icon = item.category.icon,
                tint = tone,
                containerColor = tone.copy(alpha = 0.12f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.details.replace(item.category.name, item.category.displayLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                StatusPill(
                    text = item.expirationLabel,
                    containerColor = tone.copy(alpha = 0.12f),
                    contentColor = tone
                )
            }
            IconButton(
                onClick = onRemove,
                enabled = item.sourceProduct != null
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = if (item.sourceProduct != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
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

private val Location.icon: ImageVector
    get() = when (this) {
        Location.FRESH -> Icons.Outlined.Kitchen
        Location.DRY -> Icons.Outlined.Inventory2
        Location.FROZEN -> Icons.Outlined.AcUnit
    }
