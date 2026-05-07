package com.example.inventoryhouse.ui.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.ui.component.IconBubble
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.PagerIndicator
import com.example.inventoryhouse.ui.component.PrimaryActionButton
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onGoToRegister: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory(context))

    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Votre cuisine, enfin claire",
                description = "Visualisez frigo, placard et congélateur depuis un seul espace partagé.",
                icon = Icons.Outlined.Inventory2,
                accent = Color(0xFF1B7F4C)
            ),
            OnboardingPage(
                title = "Une maison synchronisée",
                description = "Invitez le foyer, attribuez les rôles et gardez le stock à jour ensemble.",
                icon = Icons.Outlined.Home,
                accent = Color(0xFF2662D9)
            ),
            OnboardingPage(
                title = "Moins de gaspillage",
                description = "Scannez les produits et repérez rapidement ce qui doit être consommé.",
                icon = Icons.Default.QrCodeScanner,
                accent = Color(0xFFF4B63F)
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    suspend fun completeAnd(action: () -> Unit) {
        vm.completeOnboarding()
        action()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "InventoryHouse",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage == 0) {
                                scope.launch { completeAnd(onGoToLogin) }
                            } else {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (pagerState.currentPage == 0) {
                                Icons.Filled.Close
                            } else {
                                Icons.AutoMirrored.Filled.ArrowBack
                            },
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { scope.launch { completeAnd(onGoToLogin) } }) {
                        Text("Passer")
                    }
                }
            )
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
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val item = pages[page]
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OnboardingHero(page = page, item = item)

                        Spacer(Modifier.height(28.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PagerIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage
                )

                Spacer(Modifier.height(22.dp))

                val isLast = pagerState.currentPage == pages.lastIndex
                PrimaryActionButton(
                    text = if (isLast) "Créer mon compte" else "Continuer",
                    enabled = true,
                    onClick = {
                        scope.launch {
                            if (isLast) {
                                completeAnd(onGoToRegister)
                            } else {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )

                TextButton(
                    onClick = { scope.launch { completeAnd(onGoToLogin) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("J'ai déjà un compte")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OnboardingHero(
    page: Int,
    item: OnboardingPage
) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.08f),
        containerColor = item.accent.copy(alpha = 0.12f),
        contentPadding = PaddingValues(18.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (page) {
                0 -> KitchenOverview(item)
                1 -> HomeMembers(item)
                else -> ScanAndSave(item)
            }
        }
    }
}

@Composable
private fun KitchenOverview(item: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconBubble(
            icon = item.icon,
            modifier = Modifier.size(68.dp),
            tint = item.accent,
            containerColor = MaterialTheme.colorScheme.surface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBubble(Icons.Outlined.Kitchen, tint = Color(0xFF1B7F4C), containerColor = MaterialTheme.colorScheme.surface)
            IconBubble(Icons.Outlined.Restaurant, tint = Color(0xFFF4B63F), containerColor = MaterialTheme.colorScheme.surface)
            IconBubble(Icons.Outlined.Eco, tint = Color(0xFF2662D9), containerColor = MaterialTheme.colorScheme.surface)
        }
        StatusLine("12 produits suivis", item.accent)
        StatusLine("3 zones de rangement", MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun HomeMembers(item: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconBubble(
            icon = item.icon,
            modifier = Modifier.size(70.dp),
            tint = item.accent,
            containerColor = MaterialTheme.colorScheme.surface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                IconBubble(
                    icon = Icons.Outlined.Person,
                    modifier = Modifier.size(42.dp),
                    tint = item.accent,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
        StatusLine("Maison Martin", item.accent)
        StatusLine("Rôles membre et admin", MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun ScanAndSave(item: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconBubble(
            icon = item.icon,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onTertiary,
            containerColor = item.accent
        )
        StatusLine("Scan en quelques secondes", MaterialTheme.colorScheme.secondary)
        StatusLine("Alertes de péremption", MaterialTheme.colorScheme.error)
        IconBubble(
            icon = Icons.Outlined.NotificationsNone,
            tint = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun StatusLine(
    text: String,
    accent: Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = MaterialTheme.shapes.extraSmall,
                color = accent
            ) {}
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
