package com.example.inventoryhouse.ui.screen.auth.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.R
import com.example.inventoryhouse.ui.component.FeedbackMessage
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.PrimaryActionButton
import com.example.inventoryhouse.ui.screen.auth.AuthHeroCard
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    onBack: (() -> Unit)? = null,
    onGoToRegister: () -> Unit
) {
    val context = LocalContext.current
    val googleServerClientId = stringResource(R.string.google_server_client_id)
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                onEvent(LoginEvent.GoogleSignInFailed("Jeton Google absent. Vérifiez le client id web."))
            } else {
                onEvent(LoginEvent.GoogleTokenReceived(idToken))
            }
        } catch (e: ApiException) {
            onEvent(LoginEvent.GoogleSignInFailed("Connexion Google annulée ou refusée (${e.statusCode})."))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (onBack != null) {
                CenterAlignedTopAppBar(
                    title = { Text("Connexion") },
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
                    title = "Ravi de vous revoir",
                    subtitle = "Connectez-vous pour retrouver votre stock, vos membres et vos listes."
                )

                Spacer(Modifier.height(22.dp))

                ModernCard {
                    Column {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { onEvent(LoginEvent.EmailChanged(it.trim())) },
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
                            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            label = { Text("Mot de passe") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onEvent(LoginEvent.TogglePasswordVisibility) },
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
                            enabled = !state.isLoading
                        )

                        if (!state.errorMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            FeedbackMessage(text = state.errorMessage, isError = true)
                        }

                        Spacer(Modifier.height(16.dp))

                        PrimaryActionButton(
                            text = "Se connecter",
                            enabled = state.canSubmit,
                            isLoading = state.isLoading,
                            onClick = { onEvent(LoginEvent.Submit) }
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                if (googleServerClientId.isBlank()) {
                                    onEvent(
                                        LoginEvent.GoogleSignInFailed(
                                            "GOOGLE_WEB_CLIENT_ID manquant dans gradle.properties."
                                        )
                                    )
                                    return@OutlinedButton
                                }

                                val signInOptions = GoogleSignInOptions
                                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(googleServerClientId)
                                    .requestEmail()
                                    .build()
                                googleLauncher.launch(
                                    GoogleSignIn.getClient(context, signInOptions).signInIntent
                                )
                            },
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Continuer avec Google")
                        }
                    }
                }

                TextButton(
                    onClick = onGoToRegister,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Créer un compte")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
