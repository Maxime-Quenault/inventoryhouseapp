package com.example.inventoryhouse.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.ui.theme.GreenBtn
import com.example.inventoryhouse.ui.theme.SoftBlack

@Composable
fun AuthHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val bg = GreenBtn.copy(alpha = 0.14f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(1.25f)
                .clip(RoundedCornerShape(22.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FoodyStock",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftBlack
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = SoftBlack,
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
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GreenBtn,
            contentColor = SoftBlack,
            disabledContainerColor = GreenBtn.copy(alpha = 0.45f),
            disabledContentColor = SoftBlack.copy(alpha = 0.45f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(text)
    }
}