package com.example.inventoryhouse.ui.screen.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.domain.repository.ProductRepository
import com.example.inventoryhouse.ui.component.EmptyState
import com.example.inventoryhouse.ui.component.IconBubble
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.SectionHeader
import com.example.inventoryhouse.ui.component.StatCard
import com.example.inventoryhouse.ui.component.StatusPill
import com.example.inventoryhouse.ui.screen.house.HouseState
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun DashboardRoute(
    houseState: HouseState,
    productRepository: ProductRepository,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by productRepository.getProductsStream().collectAsState(initial = emptyList())

    LaunchedEffect(productRepository) {
        runCatching { productRepository.refresh() }
    }

    DashboardScreen(
        houseState = houseState,
        products = products,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    houseState: HouseState,
    products: List<Product>,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val userName = houseState.user?.name?.takeIf { it.isNotBlank() } ?: "Maison"
    val houseName = houseState.selectedHouse?.name ?: "Aucune maison"
    val expiringSoon = products
        .filter { ChronoUnit.DAYS.between(today, it.expiredDate) in 0..3 }
        .sortedBy { it.expiredDate }
    val expiredCount = products.count { it.expiredDate.isBefore(today) }
    val healthyCount = (products.size - expiringSoon.size - expiredCount).coerceAtLeast(0)
    val healthPercent = if (products.isEmpty()) 100 else healthyCount * 100 / products.size

    InventoryBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 126.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DashboardHeader(
                    userName = userName,
                    houseName = houseName,
                    onSettingsClick = onSettingsClick
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Articles",
                        value = products.size.toString(),
                        icon = Icons.Outlined.Inventory2,
                        accent = MaterialTheme.colorScheme.primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Membres",
                        value = houseState.members.size.toString(),
                        icon = Icons.Outlined.Groups,
                        accent = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                StockHealthCard(
                    healthPercent = healthPercent,
                    expiringCount = expiringSoon.size,
                    expiredCount = expiredCount
                )
            }

            item {
                SectionHeader(title = "À consommer rapidement")
            }

            if (expiringSoon.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = "Rien d'urgent",
                        message = "Les produits proches de leur date apparaîtront ici automatiquement."
                    )
                }
            } else {
                expiringSoon.take(5).forEach { product ->
                    item {
                        UrgentProductRow(product = product, today = today)
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    houseName: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bonjour",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            StatusPill(
                text = houseName,
                modifier = Modifier.padding(top = 8.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Surface(
            modifier = Modifier
                .size(50.dp)
                .clickable(onClick = onSettingsClick),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Paramètres",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StockHealthCard(
    healthPercent: Int,
    expiringCount: Int,
    expiredCount: Int
) {
    val tone = when {
        expiredCount > 0 -> MaterialTheme.colorScheme.error
        expiringCount > 0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    ModernCard(
        containerColor = tone.copy(alpha = 0.12f),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Santé du stock",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$healthPercent%",
                        style = MaterialTheme.typography.displaySmall,
                        color = tone
                    )
                }
                IconBubble(
                    icon = if (expiredCount > 0) Icons.Outlined.WarningAmber else Icons.Outlined.Kitchen,
                    tint = tone,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = "$expiringCount bientôt",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                StatusPill(
                    text = "$expiredCount expiré",
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun UrgentProductRow(
    product: Product,
    today: LocalDate
) {
    val days = ChronoUnit.DAYS.between(today, product.expiredDate).toInt()
    val tone = when {
        days <= 0 -> MaterialTheme.colorScheme.error
        days <= 1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
    val label = when {
        days < 0 -> "Expiré"
        days == 0 -> "Aujourd'hui"
        days == 1 -> "Demain"
        else -> "Dans $days jours"
    }

    ModernCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBubble(
                icon = Icons.Outlined.CalendarMonth,
                tint = tone,
                containerColor = tone.copy(alpha = 0.12f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.quantity} ${product.quantityUnit} · ${product.location.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusPill(
                text = label,
                containerColor = tone.copy(alpha = 0.12f),
                contentColor = tone
            )
        }
    }
}
