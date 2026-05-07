package com.example.inventoryhouse.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.ui.component.IconBubble
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.PrimaryActionButton

@Composable
fun AuthHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 184.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "InventoryHouse",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBubble(
                        icon = Icons.Outlined.Inventory2,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    IconBubble(
                        icon = Icons.Outlined.Kitchen,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    IconBubble(
                        icon = Icons.Outlined.Groups,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    IconBubble(
                        icon = Icons.Outlined.Eco,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Un stock partagé, lisible et toujours à jour.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 3
        )
    }
}

@Composable
fun PrimaryGreenButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryActionButton(
        text = text,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}
