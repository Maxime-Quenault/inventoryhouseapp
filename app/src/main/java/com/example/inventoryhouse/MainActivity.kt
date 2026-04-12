package com.example.inventoryhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.inventoryhouse.data.AppDatabase
import com.example.inventoryhouse.data.local.session.SessionStore
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.data.repository.HomeRepositoryImpl
import com.example.inventoryhouse.data.repository.InMemoryProductRepository
import com.example.inventoryhouse.data.repository.OnboardingRepositoryImpl
import com.example.inventoryhouse.data.repository.RemoteAuthRepository
import com.example.inventoryhouse.ui.navigation.AppDestinations
import com.example.inventoryhouse.ui.navigation.RootDestination
import com.example.inventoryhouse.ui.screen.auth.login.LoginScreen
import com.example.inventoryhouse.ui.screen.auth.login.LoginViewModel
import com.example.inventoryhouse.ui.screen.auth.login.LoginViewModelFactory
import com.example.inventoryhouse.ui.screen.auth.register.RegisterScreen
import com.example.inventoryhouse.ui.screen.auth.register.RegisterViewModel
import com.example.inventoryhouse.ui.screen.auth.register.RegisterViewModelFactory
import com.example.inventoryhouse.ui.screen.food.FoodScreen
import com.example.inventoryhouse.ui.screen.home.HomeScreen
import com.example.inventoryhouse.ui.screen.home.HomeViewModel
import com.example.inventoryhouse.ui.screen.onboarding.OnboardingScreen
import com.example.inventoryhouse.ui.screen.profile.ProfileRoute
import com.example.inventoryhouse.ui.screen.scanner.ScannerRoute
import com.example.inventoryhouse.ui.screen.scanner.manualadd.AddProductRoute
import com.example.inventoryhouse.ui.screen.settings.SettingsScreen
import com.example.inventoryhouse.ui.screen.stock.StockRoute
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

@Composable
fun InventoryHouseApp() {
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val onboardingRepo = remember { OnboardingRepositoryImpl(appContext) }
    val onboardingCompletedState = produceState<Boolean?>(initialValue = null) {
        value = onboardingRepo.onboardingCompleted.first()
    }

    val sessionStore = remember { SessionStore(appContext) }
    val authRepository = remember { RemoteAuthRepository(ApiClient.authApi, sessionStore) }

    val loginVm: LoginViewModel = viewModel(factory = LoginViewModelFactory(authRepository))
    val loginState by loginVm.state.collectAsState()

    val registerVm: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(authRepository))
    val registerState by registerVm.state.collectAsState()

    val homeRepository = remember { HomeRepositoryImpl() }
    val homeVm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(homeRepository))
    val homeState by homeVm.state.collectAsState()

    var root by rememberSaveable { mutableStateOf<RootDestination?>(null) }

    LaunchedEffect(onboardingCompletedState.value) {
        val completed = onboardingCompletedState.value ?: return@LaunchedEffect
        if (root == null) {
            root = if (completed) RootDestination.MAIN else RootDestination.ONBOARDING
        }
    }

    if (root == null) return

    when (root!!) {
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
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { AppDestinations.entries.size }
            )

            var showAddProductScreen by rememberSaveable { mutableStateOf(false) }
            var showSettingsScreen by rememberSaveable { mutableStateOf(false) }

            val database = remember {
                Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "stock_database"
                ).build()
            }

            val productRepository = remember { InMemoryProductRepository(database.productDao()) }

            BackHandler(enabled = showAddProductScreen || showSettingsScreen) {
                when {
                    showAddProductScreen -> showAddProductScreen = false
                    showSettingsScreen -> showSettingsScreen = false
                }
            }

            if (showAddProductScreen) {
                AddProductRoute(
                    leaveScreen = { showAddProductScreen = false },
                    onProductAdded = { scope.launch { productRepository.addProduct(it) } },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (showSettingsScreen) {
                SettingsScreen(modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 112.dp)
                    ) { page ->
                        when (AppDestinations.entries[page]) {
                            AppDestinations.HOME -> HomeScreen(
                                state = homeState,
                                onEvent = homeVm::onEvent,
                                onSettingsClick = { showSettingsScreen = true },
                                modifier = Modifier
                            )

                            AppDestinations.STOCK -> StockRoute(
                                repository = productRepository,
                                modifier = Modifier
                            )

                            AppDestinations.ADD_PRODUCT -> ScannerRoute(
                                repository = productRepository,
                                modifier = Modifier,
                                onAddProductClick = { showAddProductScreen = true }
                            )

                            AppDestinations.FOOD -> FoodScreen(modifier = Modifier)
                            AppDestinations.PROFILE -> ProfileRoute(
                                repository = productRepository,
                                modifier = Modifier
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
            shape = RoundedCornerShape(34.dp),
            color = Color(0xFFF0FAF0),
            shadowElevation = 8.dp,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .heightIn(min = 78.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = selectedIndex == index
                    val containerColor = if (selected) Color(0xFF1FA541) else Color(0xFFDFF5E3)
                    val contentColor = if (selected) Color.White else Color(0xFF1D7F35)

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(CircleShape)
                            .background(containerColor)
                            .clickable { onItemClick(index) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        /*if (selected) {
                            Text(
                                text = destination.label,
                                color = contentColor,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }*/
                    }
                }
            }
        }
    }
}
