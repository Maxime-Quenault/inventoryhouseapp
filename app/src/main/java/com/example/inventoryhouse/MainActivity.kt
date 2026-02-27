package com.example.inventoryhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.inventoryhouse.ui.theme.InventoryHouseTheme

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
                onLoginSuccess = { root = RootDestination.MAIN },
                onGoToRegister = { root = RootDestination.REGISTER }
            )
        }

        RootDestination.REGISTER -> {
            RegisterScreen(
                onRegisterSuccess = { root = RootDestination.MAIN },
                onGoToLogin = { root = RootDestination.LOGIN }
            )
        }

        RootDestination.MAIN -> {
            // -----------------------------
            // TON CODE EXISTANT (MAIN)
            // -----------------------------
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { AppDestinations.entries.size }
            )

            var showAddProductScreen by rememberSaveable { mutableStateOf(false) }

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

            if (showAddProductScreen) {
                AddProductScreen(
                    leaveScreen = { showAddProductScreen = false },
                    onProductAdded = {
                        scope.launch { productRepository.addProduct(it) }
                    },
                    modifier = Modifier.fillMaxSize()
                )
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
                                AppDestinations.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
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