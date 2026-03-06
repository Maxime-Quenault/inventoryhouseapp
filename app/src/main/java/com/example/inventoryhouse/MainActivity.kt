package com.example.inventoryhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.example.inventoryhouse.data.AppDatabase
import com.example.inventoryhouse.data.repository.InMemoryProductRepository
import com.example.inventoryhouse.data.repository.OnboardingRepositoryImpl
import com.example.inventoryhouse.ui.navigation.AppDestinations
import com.example.inventoryhouse.ui.navigation.RootDestination
import com.example.inventoryhouse.ui.screen.auth.login.LoginScreen
import com.example.inventoryhouse.ui.screen.auth.register.RegisterScreen
import com.example.inventoryhouse.ui.screen.food.FoodScreen
import com.example.inventoryhouse.ui.screen.home.HomeScreen
import com.example.inventoryhouse.ui.screen.profile.ProfileScreen
import com.example.inventoryhouse.ui.screen.scanner.ScannerScreen
import com.example.inventoryhouse.ui.screen.settings.SettingsScreen
import com.example.inventoryhouse.ui.screen.scanner.manualadd.AddProductScreen
import com.example.inventoryhouse.ui.screen.stock.StockScreen
import com.example.inventoryhouse.ui.screen.onboarding.OnboardingScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.data.local.session.SessionStore
import com.example.inventoryhouse.data.remote.api.AuthApi
import com.example.inventoryhouse.data.remote.network.ApiClient
import com.example.inventoryhouse.data.repository.HomeRepositoryImpl
import com.example.inventoryhouse.data.repository.RemoteAuthRepository
import com.example.inventoryhouse.ui.screen.auth.login.LoginViewModel
import com.example.inventoryhouse.ui.screen.auth.login.LoginViewModelFactory
import com.example.inventoryhouse.ui.screen.auth.register.RegisterViewModel
import com.example.inventoryhouse.ui.screen.auth.register.RegisterViewModelFactory
import com.example.inventoryhouse.ui.screen.home.HomeViewModel
import com.example.inventoryhouse.ui.theme.InventoryHouseTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override
    fun onCreate(savedInstanceState: Bundle?) {
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

    // Repo onboarding (DataStore)
    val onboardingRepo = remember { OnboardingRepositoryImpl(appContext) }

    // On charge une seule fois la valeur "onboardingCompleted"
    val onboardingCompletedState = produceState<Boolean?>(initialValue = null) {
        value = onboardingRepo.onboardingCompleted.first()
    }

    val sessionStore = remember { SessionStore(appContext) }
    val authApi = remember { ApiClient.authApi }
    val authRepository = remember { RemoteAuthRepository(authApi, sessionStore) }

    val loginVm: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authRepository)
    )
    val loginState by loginVm.state.collectAsState()

    val registerVm: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(authRepository)
    )
    val registerState by registerVm.state.collectAsState()

    val homeRepository = remember { HomeRepositoryImpl() }
    val homeVm: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(homeRepository)
    )
    val homeState by homeVm.state.collectAsState()

    // Root destination (ONBOARDING / LOGIN / REGISTER / MAIN)
    var root by rememberSaveable { mutableStateOf<RootDestination?>(null) }

    LaunchedEffect(onboardingCompletedState.value) {
        val completed = onboardingCompletedState.value ?: return@LaunchedEffect
        if (root == null) {
            // ➜ choix par défaut après onboarding :
            // - si tu veux forcer auth : RootDestination.LOGIN
            // - si tu veux aller direct app : RootDestination.MAIN
            root = if (completed) RootDestination.MAIN else RootDestination.ONBOARDING
        }
    }

    // Petit écran vide tant que DataStore n'a pas répondu
    if (root == null) return

    when (root!!) {
        RootDestination.ONBOARDING -> {
            OnboardingScreen(
                onGoToRegister = {
                    // OnboardingScreen marque déjà "completed" via son ViewModel
                    root = RootDestination.REGISTER
                },
                onGoToLogin = {
                    root = RootDestination.LOGIN
                }
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

            val productRepository = remember {
                InMemoryProductRepository(database.productDao())
            }

            BackHandler(enabled = showAddProductScreen || showSettingsScreen) {
                when {
                    showAddProductScreen -> showAddProductScreen = false
                    showSettingsScreen -> showSettingsScreen = false
                }
            }

            if (showAddProductScreen) {
                AddProductScreen(
                    leaveScreen = { showAddProductScreen = false },
                    onProductAdded = {
                        scope.launch { productRepository.addProduct(it) }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (showSettingsScreen) {
                SettingsScreen(modifier = Modifier.fillMaxSize())
            } else {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        AppDestinations.entries.forEachIndexed { index, destination ->
                            item(
                                icon = {
                                    Icon(destination.icon, contentDescription = destination.label)
                                },
                                label = { Text(destination.label) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { page ->
                            when (AppDestinations.entries[page]) {
                                AppDestinations.HOME -> HomeScreen(
                                    state = homeState,
                                    onEvent = homeVm::onEvent,
                                    onSettingsClick = { showSettingsScreen = true },
                                    modifier = Modifier.padding(innerPadding)
                                )
                                AppDestinations.STOCK -> StockScreen(
                                    repository = productRepository,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                AppDestinations.ADD_PRODUCT -> ScannerScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onAddProductClick = { showAddProductScreen = true }
                                )
                                AppDestinations.FOOD -> FoodScreen(modifier = Modifier.padding(innerPadding))
                                AppDestinations.PROFILE -> ProfileScreen(modifier = Modifier.padding(innerPadding))
                            }
                        }
                    }
                }
            }
        }
    }
}
