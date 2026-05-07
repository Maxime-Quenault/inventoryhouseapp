package com.example.inventoryhouse.ui.screen.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.ui.component.FeedbackMessage
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.PrimaryActionButton
import com.example.inventoryhouse.ui.screen.auth.AuthHeroCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterState,
    onEvent: (RegisterEvent) -> Unit,
    onBack: (() -> Unit)? = null,
    onGoToLogin: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (onBack != null) {
                CenterAlignedTopAppBar(
                    title = { Text("Créer un compte") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                )
            }
        }
    ) { padding ->
        InventoryBackground(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                AuthHeroCard(
                    title = "Votre maison commence ici",
                    subtitle = "Créez votre compte et invitez ensuite les personnes du foyer."
                )

                Spacer(Modifier.height(22.dp))

                ModernCard {
                    Column {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { onEvent(RegisterEvent.NameChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            label = { Text("Nom") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            enabled = !state.isLoading
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { onEvent(RegisterEvent.EmailChanged(it.trim())) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !state.isLoading
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            label = { Text("Mot de passe") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onEvent(RegisterEvent.TogglePasswordVisibility) },
                                    enabled = !state.isLoading
                                ) {
                                    Icon(
                                        imageVector = if (state.showPassword) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (state.showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            supportingText = { Text("8 caractères minimum") },
                            enabled = !state.isLoading
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            label = { Text("Confirmer le mot de passe") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            visualTransformation = if (state.showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !state.isLoading
                        )

                        if (!state.errorMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            FeedbackMessage(text = state.errorMessage, isError = true)
                        }

                        Spacer(Modifier.height(16.dp))

                        PrimaryActionButton(
                            text = "Créer un compte",
                            enabled = state.canSubmit,
                            isLoading = state.isLoading,
                            onClick = { onEvent(RegisterEvent.Submit) }
                        )
                    }
                }

                TextButton(
                    onClick = onGoToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Se connecter")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
