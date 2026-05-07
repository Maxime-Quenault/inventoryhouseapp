package com.example.inventoryhouse.ui.screen.house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventoryhouse.data.remote.dto.HouseDto
import com.example.inventoryhouse.data.remote.dto.MemberDto

@Composable
fun HouseSetupScreen(
    state: HouseState,
    onEvent: (HouseEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Text("Creez votre maison", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Une maison regroupe les membres et le stock partage.")

                OutlinedTextField(
                    value = state.houseNameInput,
                    onValueChange = { onEvent(HouseEvent.HouseNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nom de la maison") },
                    singleLine = true
                )

                Button(
                    onClick = { onEvent(HouseEvent.CreateHouse) },
                    enabled = state.canCreateHouse,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Creer la maison")
                }

                Feedback(state)
                if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Maison", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(state.selectedHouse?.name ?: "Aucune maison")
                }
                IconButton(onClick = { onEvent(HouseEvent.Refresh) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                }
            }
        }

        if (state.houses.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.houses, key = { it.id }) { house ->
                        AssistChip(
                            onClick = { onEvent(HouseEvent.SelectHouse(house)) },
                            label = { Text(house.name) },
                            enabled = state.selectedHouse?.id != house.id
                        )
                    }
                }
            }
        }

        item {
            HouseSummaryCard(state.selectedHouse, state.members.size)
        }

        item {
            AddMemberCard(state = state, onEvent = onEvent)
        }

        item {
            Text("Membres", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (state.members.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        "Aucun membre pour le moment.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
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

@Composable
private fun HouseSummaryCard(house: HouseDto?, memberCount: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Groups, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(house?.name ?: "Maison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Role : ${house?.role ?: "owner"}")
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(memberCount.toString(), fontWeight = FontWeight.Bold)
                Text("membres")
            }
        }
    }
}

@Composable
private fun AddMemberCard(
    state: HouseState,
    onEvent: (HouseEvent) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Ajouter un membre existant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.memberEmailInput,
                onValueChange = { onEvent(HouseEvent.MemberEmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email du membre") },
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("member", "admin").forEach { role ->
                    FilterChip(
                        selected = state.memberRoleInput == role,
                        onClick = { onEvent(HouseEvent.MemberRoleChanged(role)) },
                        label = { Text(if (role == "admin") "Admin" else "Membre") }
                    )
                }
            }

            Button(
                onClick = { onEvent(HouseEvent.AddMember) },
                enabled = state.canAddMember,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Ajouter")
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: MemberDto,
    onRemove: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(member.user?.name ?: member.user?.email ?: "Membre", fontWeight = FontWeight.SemiBold)
                Text(member.user?.email.orEmpty())
                Text("Role : ${member.role?.name ?: "member"}")
            }
            if (member.role?.name != "owner") {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Retirer")
                }
            }
        }
    }
}

@Composable
private fun Feedback(state: HouseState) {
    state.errorMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.error)
    }
    state.successMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.primary)
    }
}
