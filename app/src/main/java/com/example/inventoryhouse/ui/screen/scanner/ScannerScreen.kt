package com.example.inventoryhouse.ui.screen.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScannerScreen(modifier: Modifier = Modifier, onAddProductClick: () -> Unit) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scan ton produit !",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            onClick = onAddProductClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Ajouter manuellement")
        }
    }
}