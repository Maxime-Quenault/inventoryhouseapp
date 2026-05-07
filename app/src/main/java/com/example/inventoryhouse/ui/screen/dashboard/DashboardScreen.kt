package com.example.inventoryhouse.ui.screen.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.domain.repository.ProductRepository
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
    val userName = houseState.user?.name?.takeIf { it.isNotBlank() } ?: "Maison"
    val expiringSoon = products.filter { product ->
        val days = ChronoUnit.DAYS.between(LocalDate.now(), product.expiredDate)
        days in 0..3
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bonjour,", style = MaterialTheme.typography.bodyMedium)
                    Text(userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(houseState.selectedHouse?.name ?: "Aucune maison")
                }
                val initial = userName.firstOrNull()?.uppercase() ?: "?"
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Articles",
                    value = products.size.toString()
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Membres",
                    value = houseState.members.size.toString()
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Etat du stock", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (products.isEmpty()) {
                            "Aucun item pour le moment"
                        } else {
                            "${expiringSoon.size} item(s) a consommer rapidement"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        item {
            Text("A consommer rapidement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (expiringSoon.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        "Rien d'urgent. Les prochains produits scannes apparaitront ici.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            expiringSoon.take(4).forEach { product ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                Text("${product.quantity} ${product.quantityUnit} - ${product.location.name}")
                            }
                            Text(product.expiredDate.toString())
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
