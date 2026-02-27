package com.example.inventoryhouse.ui.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventoryhouse.ui.component.PagerIndicator
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight

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
            Triple(
                "Bienvenue sur\nFoodyStock",
                "Gérez votre garde-manger\nintelligemment pour éviter le gaspillage\net gagner du temps.",
                "FoodyStock"
            ),
            Triple(
                "Ma Maison, MaFamille",
                "Créez votre foyer virtuel et invitez vos\nproches à gérer le stock ensemble.",
                "FoodyStock"
            ),
            Triple(
                "Stock en Temps Réel",
                "Consultez ce qu'il vous reste en un clin\nd'œil, où que vous soyez.",
                "FoodyStock"
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = pages[pagerState.currentPage].third) },
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
                            imageVector = if (pagerState.currentPage == 0) Icons.Filled.Close
                            else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val (title, desc) = pages[page].let { it.first to it.second }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HeroCard(page = page)

                    Spacer(Modifier.height(22.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        minLines = 2,
                        maxLines = 2
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        minLines = 3,
                        maxLines = 3
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            PagerIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )

            Spacer(Modifier.height(18.dp))

            val isLast = pagerState.currentPage == pages.lastIndex

            Button(
                onClick = {
                    scope.launch {
                        if (!isLast) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            completeAnd(onGoToRegister)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (!isLast) "Suivant" else "Créer un compte")
            }

            Spacer(Modifier.height(10.dp))

            if (!isLast) {
                TextButton(
                    onClick = { scope.launch { completeAnd(onGoToLogin) } }
                ) {
                    Text("Passer l'introduction")
                }
            } else {
                TextButton(
                    onClick = { scope.launch { completeAnd(onGoToLogin) } }
                ) {
                    Text("Se connecter")
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HeroCard(page: Int) {
    val bg = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

    Box(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(1.05f)
            .clip(RoundedCornerShape(22.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        when (page) {
            0 -> FeatureGrid()
            1 -> FamilyHomeHero()
            else -> RealtimeStockHero()
        }
    }
}

@Composable
private fun FeatureGrid() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiniFeature(icon = Icons.Outlined.Inventory2)
            MiniFeature(icon = Icons.Outlined.Kitchen)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiniFeature(icon = Icons.Outlined.Eco)
            MiniFeature(icon = Icons.Outlined.NotificationsNone)
        }
    }
}

@Composable
private fun MiniFeature(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.size(74.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun FamilyHomeHero() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(3) {
                Surface(
                    shape = RoundedCornerShape(50),
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .size(34.dp)
                        .padding(end = 6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RealtimeStockHero() {
    // “Image” type mock (comme ton screenshot) sans res/drawable obligatoire
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .aspectRatio(0.55f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}