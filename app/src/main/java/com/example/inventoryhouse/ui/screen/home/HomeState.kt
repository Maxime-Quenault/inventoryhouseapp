package com.example.inventoryhouse.ui.screen.home

data class HomeState(
    val greetingTitle: String = "Bonjour,",
    val username: String = "Maxime",
    val totalArticles: Int = 124,
    val totalCategories: Int = 8,
    val globalStockHealth: Int = 85,
    val periodLabel: String = "Cette semaine",
    val quickConsumeItems: List<QuickConsumeItem> = listOf(
        QuickConsumeItem(name = "Lait Entier", expiresInLabel = "EXPIRE AUJOURD'HUI"),
        QuickConsumeItem(name = "Salade Mixte", expiresInLabel = "DANS 2 JOURS"),
        QuickConsumeItem(name = "Poulet", expiresInLabel = "DANS 3 JOURS")
    ),
    val recentMovements: List<RecentMovement> = listOf(
        RecentMovement(name = "Pâtes Fusilli", quantity = 3, note = "Ajouté par Thomas • Aujourd'hui, 10:30", delta = "+3"),
        RecentMovement(name = "Yaourt Nature", quantity = 2, note = "Consommé par Marie • Hier, 19:45", delta = "-2"),
        RecentMovement(name = "Jus d'Orange", quantity = 1, note = "Ajouté par Thomas • Hier, 14:20", delta = "+1")
    )
)

data class QuickConsumeItem(
    val name: String,
    val expiresInLabel: String
)

data class RecentMovement(
    val name: String,
    val quantity: Int,
    val note: String,
    val delta: String
)
