package com.example.inventoryhouse.ui.screen.house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.data.remote.dto.HouseDto
import com.example.inventoryhouse.data.remote.dto.MemberDto
import com.example.inventoryhouse.ui.component.EmptyState
import com.example.inventoryhouse.ui.component.FeedbackMessage
import com.example.inventoryhouse.ui.component.IconBubble
import com.example.inventoryhouse.ui.component.InventoryBackground
import com.example.inventoryhouse.ui.component.ModernCard
import com.example.inventoryhouse.ui.component.PrimaryActionButton
import com.example.inventoryhouse.ui.component.SectionHeader
import com.example.inventoryhouse.ui.component.StatCard
import com.example.inventoryhouse.ui.component.StatusPill

@Composable
fun HouseSetupScreen(
    state: HouseState,
    onEvent: (HouseEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    InventoryBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ModernCard(contentPadding = PaddingValues(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconBubble(
                        icon = Icons.Outlined.Home,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Créez votre maison",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "Une maison regroupe les membres et le stock partagé.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = state.houseNameInput,
                        onValueChange = { onEvent(HouseEvent.HouseNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom de la maison") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        enabled = !state.isLoading
                    )

                    PrimaryActionButton(
                        text = "Créer la maison",
                        enabled = state.canCreateHouse,
                        isLoading = state.isLoading,
                        icon = Icons.Default.Add,
                        onClick = { onEvent(HouseEvent.CreateHouse) }
                    )

                    Feedback(state)
                    if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun HouseScreen(
    state: HouseState,
    onEvent: (HouseEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    InventoryBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 126.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HouseHeader(
                    state = state,
                    onRefresh = { onEvent(HouseEvent.Refresh) }
                )
            }

            if (state.houses.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.houses, key = { it.id }) { house ->
                            FilterChip(
                                selected = state.selectedHouse?.id == house.id,
                                onClick = { onEvent(HouseEvent.SelectHouse(house)) },
                                label = { Text(house.name) }
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Membres",
                        value = state.members.size.toString(),
                        icon = Icons.Outlined.Groups,
                        accent = MaterialTheme.colorScheme.primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Rôle",
                        value = state.selectedHouse?.role?.roleLabel ?: "Owner",
                        icon = Icons.Outlined.Person,
                        accent = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                AddMemberCard(state = state, onEvent = onEvent)
            }

            item {
                SectionHeader(title = "Membres")
            }

            if (state.members.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Groups,
                        title = "Aucun membre",
                        message = "Les personnes ajoutées à la maison apparaîtront ici."
                    )
                }
            }

            items(state.members, key = { it.userId }) { member ->
                MemberRow(member = member, onRemove = { onEvent(HouseEvent.RemoveMember(member)) })
            }

            item {
                Feedback(state)
                if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun HouseHeader(
    state: HouseState,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Maison",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = state.selectedHouse?.name ?: "Aucune maison",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
        }
    }
}

@Composable
private fun AddMemberCard(
    state: HouseState,
    onEvent: (HouseEvent) -> Unit
) {
    ModernCard(containerColor = MaterialTheme.colorScheme.surface) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Outlined.Mail,
                    tint = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Ajouter un membre",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Invitez un utilisateur existant par email.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = state.memberEmailInput,
                onValueChange = { onEvent(HouseEvent.MemberEmailChanged(it.trim())) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email du membre") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("member", "admin").forEach { role ->
                    FilterChip(
                        selected = state.memberRoleInput == role,
                        onClick = { onEvent(HouseEvent.MemberRoleChanged(role)) },
                        label = { Text(role.roleLabel) },
                        enabled = !state.isLoading
                    )
                }
            }

            PrimaryActionButton(
                text = "Ajouter",
                enabled = state.canAddMember,
                isLoading = state.isLoading,
                icon = Icons.Default.Add,
                onClick = { onEvent(HouseEvent.AddMember) }
            )
        }
    }
}

@Composable
private fun MemberRow(
    member: MemberDto,
    onRemove: () -> Unit
) {
    val roleName = member.role?.name ?: "member"

    ModernCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(
                icon = Icons.Outlined.Person,
                tint = if (roleName == "owner") {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                containerColor = if (roleName == "owner") {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.user?.name ?: member.user?.email ?: "Membre",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    member.user?.email.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                StatusPill(text = roleName.roleLabel)
            }
            if (roleName != "owner") {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Retirer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun Feedback(state: HouseState) {
    state.errorMessage?.let {
        FeedbackMessage(text = it, isError = true)
    }
    state.successMessage?.let {
        FeedbackMessage(text = it, isError = false)
    }
}

private val String.roleLabel: String
    get() = when (this.lowercase()) {
        "owner" -> "Propriétaire"
        "admin" -> "Admin"
        else -> "Membre"
    }
