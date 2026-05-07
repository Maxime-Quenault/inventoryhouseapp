package com.example.inventoryhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.local.session.SessionStore
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.data.repository.InventoryRepository
import com.example.inventoryhouse.data.repository.OnboardingRepositoryImpl
import com.example.inventoryhouse.data.repository.RemoteAuthRepository
import com.example.inventoryhouse.data.repository.RemoteProductRepository
import com.example.inventoryhouse.ui.navigation.AppDestinations
import com.example.inventoryhouse.ui.navigation.RootDestination
import com.example.inventoryhouse.ui.screen.auth.login.LoginScreen
import com.example.inventoryhouse.ui.screen.auth.login.LoginViewModel
import com.example.inventoryhouse.ui.screen.auth.login.LoginViewModelFactory
import com.example.inventoryhouse.ui.screen.auth.register.RegisterScreen
import com.example.inventoryhouse.ui.screen.auth.register.RegisterViewModel
import com.example.inventoryhouse.ui.screen.auth.register.RegisterViewModelFactory
import com.example.inventoryhouse.ui.screen.dashboard.DashboardRoute
import com.example.inventoryhouse.ui.screen.house.HouseScreen
import com.example.inventoryhouse.ui.screen.house.HouseSetupScreen
import com.example.inventoryhouse.ui.screen.house.HouseViewModel
import com.example.inventoryhouse.ui.screen.onboarding.OnboardingScreen
import com.example.inventoryhouse.ui.screen.scanner.ScannerRoute
import com.example.inventoryhouse.ui.screen.settings.SettingsScreen
import com.example.inventoryhouse.ui.screen.stock.StockRoute
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.theme.InventoryHouseTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryHouseTheme {
                InventoryHouseApp()
            }
        }
    }
}

private data class SessionSnapshot(val token: String?)

@Composable
fun InventoryHouseApp() {
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val onboardingRepo = remember { OnboardingRepositoryImpl(appContext) }
    val onboardingCompletedState = produceState<Boolean?>(initialValue = null) {
        value = onboardingRepo.onboardingCompleted.first()
    }

    val sessionStore = remember { SessionStore(appContext) }
    val sessionSnapshot = produceState<SessionSnapshot?>(initialValue = null) {
        value = SessionSnapshot(sessionStore.tokenFlow.first())
    }

    val authRepository = remember { RemoteAuthRepository(ApiClient.authApi, sessionStore) }
    val loginVm: LoginViewModel = viewModel(factory = LoginViewModelFactory(authRepository))
    val loginState by loginVm.state.collectAsState()
    val registerVm: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(authRepository))
    val registerState by registerVm.state.collectAsState()

    var root by rememberSaveable { mutableStateOf<RootDestination?>(null) }

    LaunchedEffect(onboardingCompletedState.value, sessionSnapshot.value) {
        val completed = onboardingCompletedState.value ?: return@LaunchedEffect
        val snapshot = sessionSnapshot.value ?: return@LaunchedEffect
        val token = snapshot.token
        if (root == null) {
            root = when {
                !completed -> RootDestination.ONBOARDING
                token.isNullOrBlank() -> RootDestination.LOGIN
                else -> RootDestination.MAIN
            }
        }
    }

    val currentRoot = root ?: return

    when (currentRoot) {
        RootDestination.ONBOARDING -> {
            OnboardingScreen(
                onGoToRegister = { root = RootDestination.REGISTER },
                onGoToLogin = { root = RootDestination.LOGIN }
            )
        }

        RootDestination.LOGIN -> {
            LoginScreen(
                state = loginState,
                onEvent = { event ->
                    loginVm.onEvent(event) {
                        root = RootDestination.MAIN
                    }
                },
                onGoToRegister = { root = RootDestination.REGISTER }
            )
        }

        RootDestination.REGISTER -> {
            RegisterScreen(
                state = registerState,
                onEvent = { event ->
                    registerVm.onEvent(event) {
                        root = RootDestination.MAIN
                    }
                },
                onGoToLogin = { root = RootDestination.LOGIN }
            )
        }

        RootDestination.MAIN -> {
            MainContent(
                sessionStore = sessionStore,
                authRepository = authRepository,
                onLoggedOut = { root = RootDestination.LOGIN }
            )
        }
    }
}

@Composable
private fun MainContent(
    sessionStore: SessionStore,
    authRepository: RemoteAuthRepository,
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val inventoryRepository = remember {
        InventoryRepository(ApiClient.inventoryApi(sessionStore))
    }
    val houseVm: HouseViewModel = viewModel(factory = HouseViewModel.provideFactory(inventoryRepository))
    val houseState by houseVm.state.collectAsState()

    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettingsScreen) {
        showSettingsScreen = false
    }

    when {
        houseState.needsHouse -> {
            HouseSetupScreen(
                state = houseState,
                onEvent = houseVm::onEvent,
                modifier = Modifier.statusBarsPadding()
            )
        }

        houseState.selectedHouse == null -> {
            LoadingScreen()
        }

        showSettingsScreen -> {
            SettingsScreen(
                onBack = { showSettingsScreen = false },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        onLoggedOut()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            val selectedHouse = houseState.selectedHouse ?: return
            val houseId = selectedHouse.id
            val productRepository = remember(houseId) {
                RemoteProductRepository(
                    inventoryRepository = inventoryRepository,
                    houseId = houseId
                )
            }
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { AppDestinations.entries.size }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) { page ->
                    when (AppDestinations.entries[page]) {
                        AppDestinations.HOME -> DashboardRoute(
                            houseState = houseState,
                            productRepository = productRepository,
                            onSettingsClick = { showSettingsScreen = true }
                        )

                        AppDestinations.STOCK -> StockRoute(
                            repository = productRepository,
                            viewModelKey = "stock-$houseId"
                        )

                        AppDestinations.ADD_PRODUCT -> ScannerRoute(
                            repository = productRepository,
                            viewModelKey = "scanner-$houseId"
                        )

                        AppDestinations.PROFILE -> HouseScreen(
                            state = houseState,
                            onEvent = houseVm::onEvent
                        )
                    }
                }

                FloatingBottomBar(
                    destinations = AppDestinations.entries,
                    selectedIndex = pagerState.currentPage,
                    onItemClick = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    InventoryBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(
                    text = "Chargement...",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomBar(
    destinations: List<AppDestinations>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .heightIn(min = 76.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = selectedIndex == index
                    val containerColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        label = "bottom-bar-item-container"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "bottom-bar-item-content"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(containerColor)
                            .clickable { onItemClick(index) }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
