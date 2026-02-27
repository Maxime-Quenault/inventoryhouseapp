package com.example.inventoryhouse.ui.screen.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            onClick = onRegisterSuccess
        ) { Text("Créer un compte (stub)") }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onGoToLogin) { Text("Se connecter") }
    }
}